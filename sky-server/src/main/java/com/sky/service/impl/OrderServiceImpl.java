package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.DishVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

  @Autowired
  private AddressBookMapper addressBookMapper;
  @Autowired
  private ShoppingCartMapper shoppingCartMapper;
  @Autowired
  private OrderMapper orderMapper;
  @Autowired
  private OrderDetailMapper orderDetailMapper;
  @Autowired
  private UserMapper userMapper;
  @Autowired
  private WeChatPayUtil weChatPayUtil;

  /**
   * 用户下单
   * @param ordersSubmitDTO
   * @return
   */
  @Override
  @Transactional
  public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
    // 获取当前用户id和地址簿id
    Long userId = BaseContext.getCurrentId();
    Long addressBookId = ordersSubmitDTO.getAddressBookId();
    // 判断地址簿是否为空，若为空则抛出异常
    AddressBook addressBook = addressBookMapper.getById(addressBookId);
    if (addressBook == null) {
      throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
    }
    // 判断购物车是否为空，若为空则抛出异常
    ShoppingCart shoppingCart = new ShoppingCart();
    shoppingCart.setUserId(userId);
    List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
    if (shoppingCartList == null || shoppingCartList.size() == 0) {
      throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
    }
    // 向订单表插入一条数据
    Orders order = new Orders();
    BeanUtils.copyProperties(ordersSubmitDTO, order);
    order.setUserId(userId);
    order.setOrderTime(LocalDateTime.now());
    order.setPayStatus(Orders.UN_PAID);
    order.setStatus(Orders.PENDING_PAYMENT);
    order.setConsignee(addressBook.getConsignee());
    order.setPhone(addressBook.getPhone());
    order.setAddress(addressBook.getDetail());
    order.setNumber(String.valueOf(System.currentTimeMillis()));
    orderMapper.insert(order); // 使用主键获取id
    // 向订单明细表插入n条数据
    List<OrderDetail> orderDetailList = new ArrayList<>();
    for (ShoppingCart cart : shoppingCartList) {
      OrderDetail orderDetail = new OrderDetail();
      BeanUtils.copyProperties(cart, orderDetail);
      orderDetail.setOrderId(order.getId());
      orderDetailList.add(orderDetail);
    }
    orderDetailMapper.insertBatch(orderDetailList);
    // 封装OrderSubmitVO对象
    OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
        .id(order.getId())
        .orderNumber(order.getNumber())
        .orderAmount(order.getAmount())
        .orderTime(order.getOrderTime())
        .build();
    // 清空购物车
    shoppingCartMapper.deleteByUserId(userId);
    // 返回订单提交结果
    return orderSubmitVO;
  }

  /**
   * 订单支付
   *
   * @param ordersPaymentDTO
   * @return
   */
  public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
    // 当前登录用户id
    Long userId = BaseContext.getCurrentId();
    User user = userMapper.getById(userId);

    //调用微信支付接口，生成预支付交易单
    JSONObject jsonObject = weChatPayUtil.pay(
        ordersPaymentDTO.getOrderNumber(), //商户订单号
        new BigDecimal(0.01), //支付金额，单位 元
        "苍穹外卖订单", //商品描述
        user.getOpenid() //微信用户的openid
    );

    if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
      throw new OrderBusinessException("该订单已支付");
    }

    OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
    vo.setPackageStr(jsonObject.getString("package"));

    return vo;
  }

  /**
   * 支付成功，修改订单状态
   *
   * @param outTradeNo
   */
  public void paySuccess(String outTradeNo) {

    // 根据订单号查询订单
    Orders ordersDB = orderMapper.getByNumber(outTradeNo);

    // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
    Orders orders = Orders.builder()
        .id(ordersDB.getId())
        .status(Orders.TO_BE_CONFIRMED)
        .payStatus(Orders.PAID)
        .checkoutTime(LocalDateTime.now())
        .build();

    orderMapper.update(orders);
  }

  /**
   * 用户分页查询历史订单
   *
   * @param page
   * @param pageSize
   * @param status
   * @return
   */
  @Override
  public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
    PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
    Page<Orders> page = orderMapper.page(ordersPageQueryDTO);
    // 若没有订单，则返回空
    if (page == null || page.isEmpty()) {
      return new PageResult(0, Collections.emptyList());
    }
    // 1. 批量拿 ID
    List<Long> ids = page.stream().map(Orders::getId).collect(Collectors.toList());
    // 2. 批量查订单明细
    List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderIds(ids);
    // 3. 内存组装 VO（只塞 orderDetailList 即可）
    List<OrderVO> orderVOList = page.stream().map(order -> {
      OrderVO orderVO = new OrderVO();
      BeanUtils.copyProperties(order, orderVO);
      Long orderId = order.getId();
      // 从批量数据中过滤出当前订单的明细，并收集为 List 塞给 VO
      List<OrderDetail> orderDetailsById = orderDetailList.stream()
          .filter(orderDetail -> orderDetail.getOrderId().equals(orderId))
          .collect(Collectors.toList());
      orderVO.setOrderDetailList(orderDetailsById);
      return orderVO;
    }).collect(Collectors.toList());
    return new PageResult(page.getTotal(), orderVOList);
  }

  /**
   * 根据id查询订单详情
   * @param id
   * @return
   */
  @Override
  public OrderVO getOrderDetailById(Long id) {
    OrderVO orderVO = orderMapper.getOrderById(id);
    List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
    orderVO.setOrderDetailList(orderDetailList);
    return orderVO;
  }
}
