package com.sky.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
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
import com.sky.properties.BaiduMapProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.BaiduMapUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
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
  @Autowired
  private BaiduMapProperties baiduMapProperties;

  /**
   * 用户下单
   */
  @Override
  @Transactional
  public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) throws Exception {
    // 1. 获取当前用户id和地址簿id
    Long userId = BaseContext.getCurrentId();
    Long addressBookId = ordersSubmitDTO.getAddressBookId();

    // 2. 判断地址簿是否为空
    AddressBook addressBook = addressBookMapper.getById(addressBookId);
    if (addressBook == null) {
      throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
    }

    // 3. 校验配送距离（百度地图 API）
    String userAddress = addressBook.getProvinceName() + addressBook.getCityName()
        + addressBook.getDistrictName() + addressBook.getDetail();

    // 1. 获取店铺和用户的经纬度
    Map<String, Double> shopLocation = BaiduMapUtil.getLocation(baiduMapProperties.getAddress(), baiduMapProperties.getAK());
    Map<String, Double> userLocation = BaiduMapUtil.getLocation(userAddress, baiduMapProperties.getAK());

    if (userLocation == null) {
      throw new OrderBusinessException("收货地址无效或过于模糊，请输入详细地址");
    }

// 2. 计算骑行距离
    Integer distance = BaiduMapUtil.getDistance(shopLocation, userLocation, baiduMapProperties.getAK());

// 3. 判断是否超出 5000 米配送范围
    if (distance == null || distance > 5000) {
      throw new OrderBusinessException(MessageConstant.DISTANCE_TOO_FAR);
    }

    // 4. 判断购物车是否为空
    ShoppingCart shoppingCart = new ShoppingCart();
    shoppingCart.setUserId(userId);
    List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
    if (shoppingCartList == null || shoppingCartList.isEmpty()) {
      throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
    }

    // 5. 向订单表插入一条数据
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
    orderMapper.insert(order);

    // 6. 向订单明细表插入 n 条数据
    List<OrderDetail> orderDetailList = new ArrayList<>();
    for (ShoppingCart cart : shoppingCartList) {
      OrderDetail orderDetail = new OrderDetail();
      BeanUtils.copyProperties(cart, orderDetail);
      orderDetail.setOrderId(order.getId());
      orderDetailList.add(orderDetail);
    }
    orderDetailMapper.insertBatch(orderDetailList);

    // 7. 封装 OrderSubmitVO 对象
    OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
        .id(order.getId())
        .orderNumber(order.getNumber())
        .orderAmount(order.getAmount())
        .orderTime(order.getOrderTime())
        .build();

    // 8. 清空购物车
    shoppingCartMapper.deleteByUserId(userId);

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
    Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
    BigDecimal amount = orders.getAmount();
    //调用微信支付接口，生成预支付交易单
    JSONObject jsonObject = weChatPayUtil.pay(
        ordersPaymentDTO.getOrderNumber(), //商户订单号
        amount, //支付金额，单位 元
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
   * @param ordersPageQueryDTO
   * @return
   */
  @Override
  public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {

    PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
    ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
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
    String orderDishes = orderDetailList.stream()
        .map(orderDetail -> orderDetail.getName() + "*" + orderDetail.getNumber())
        .collect(Collectors.joining(","));
    orderVO.setOrderDishes(orderDishes);
    orderVO.setOrderDetailList(orderDetailList);
    return orderVO;
  }

  /**
   * 订单取消
   * @param id
   */
  @Override
  public void cancel(Long id) throws Exception {
    // 获取当前订单
    Orders order = orderMapper.getById(id);
    if (order == null) {
      throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
    }
    // 查看订单状态
    Integer status = order.getStatus();
    // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
    // 商家已接单状态下或派送中状态下，用户取消订单需电话沟通商家
    if (status > 2) {
      throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
    }
    // 待支付和待接单状态下，用户可直接取消订单
    // 如果在待接单状态下取消订单，需要给用户退款
    Orders orderUpdate = new Orders();
    orderUpdate.setId(id);
    BigDecimal amount = order.getAmount();
    if (status == Orders.TO_BE_CONFIRMED) {
      if (order.getPayStatus() == Orders.PAID) {
        // 已支付状态下，用户取消订单，需要给用户退款
        // 获取微信支付工具类
        //调用微信支付退款接口
        weChatPayUtil.refund(
            order.getNumber(), //商户订单号
            order.getNumber(), //商户退款单号
            amount,//退款金额，单位 元
            amount);//原订单金额
        //支付状态修改为 退款
        orderUpdate.setPayStatus(Orders.REFUND);
      }
    }
    // 更新订单状态、取消原因、取消时间
    orderUpdate.setCancelTime(LocalDateTime.now());
    orderUpdate.setStatus(Orders.CANCELLED);
    orderUpdate.setCancelReason("用户取消");
    orderMapper.update(orderUpdate);
  }

  /**
   * 再来一单
   * @param id
   */
  @Override
  public void reorder(Long id) {
    // 1. 获取当前登录用户 ID
    Long userId = BaseContext.getCurrentId();

    // 2. 根据订单 id 查询对应的订单明细列表
    List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

    // 3. 将订单明细转化为购物车对象列表
    if (orderDetailList != null && !orderDetailList.isEmpty()) {
      List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
        ShoppingCart shoppingCart = new ShoppingCart();

        // 拷贝属性（name, image, dishId, setmealId, dishFlavor, amount, number 等）
        BeanUtils.copyProperties(orderDetail, shoppingCart, "id");

        // 补全购物车特有属性
        shoppingCart.setUserId(userId);
        shoppingCart.setCreateTime(LocalDateTime.now());

        return shoppingCart;
      }).collect(Collectors.toList());

      // 4. 批量插入到购物车表（或者循环调用 shoppingCartMapper.insert）
      shoppingCartMapper.insertBatch(shoppingCartList);
    }
  }

  /**
   * 管理端订单查询
   * @param ordersPageQueryDTO
   * @return
   */
  @Override
  public PageResult pageConditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
    PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
    Page<Orders> page = orderMapper.page(ordersPageQueryDTO);

    if (page == null || page.isEmpty()) {
      return new PageResult(0, Collections.emptyList());
    }

    // 1. 批量获取订单 ID
    List<Long> ids = page.stream().map(Orders::getId).collect(Collectors.toList());

    // 2. 批量查询订单明细
    List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderIds(ids);

    // 3. 内存匹配并组装 orderDishes
    List<OrderVO> orderVOList = page.stream().map(order -> {
      OrderVO orderVO = new OrderVO();
      BeanUtils.copyProperties(order, orderVO);
      Long orderId = order.getId();

      // 过滤出当前订单的明细
      List<OrderDetail> currentDetails = orderDetailList.stream()
          .filter(detail -> detail.getOrderId().equals(orderId))
          .collect(Collectors.toList());

      // 拼接后台需要的菜品简述字符串
      String orderDishes = currentDetails.stream()
          .map(detail -> detail.getName() + "*" + detail.getNumber())
          .collect(Collectors.joining(","));

      orderVO.setOrderDishes(orderDishes);
      return orderVO;
    }).collect(Collectors.toList());
    return new PageResult(page.getTotal(), orderVOList);
  }

  /**
   * 各个状态的订单数量统计
   * @return
   */
  @Override
  public OrderStatisticsVO statistics() {
    Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
    Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
    Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
    OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
        .toBeConfirmed(toBeConfirmed)
        .confirmed(confirmed)
        .deliveryInProgress(deliveryInProgress)
        .build();
    return orderStatisticsVO;
  }

  /**
   * 接单
   * @param ordersConfirmDTO
   */
  @Override
  public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
    Orders order = new Orders();
    order.setId(ordersConfirmDTO.getId());
    order.setStatus(Orders.CONFIRMED);
    orderMapper.update(order);
  }

  /**
   * 拒单
   * @param ordersRejectionDTO
   */
  @Override
  public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
    // 根据id查询订单
    Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

    // 订单只有存在且状态为2（待接单）才可以拒单
    if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
      throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
    }

    //支付状态
    Integer payStatus = ordersDB.getPayStatus();
    if (payStatus == Orders.PAID) {
      //用户已支付，需要退款
      String refund = weChatPayUtil.refund(
          ordersDB.getNumber(),
          ordersDB.getNumber(),
          new BigDecimal(0.01),
          new BigDecimal(0.01));
      log.info("申请退款：{}", refund);
    }

    // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
    Orders orders = new Orders();
    orders.setId(ordersDB.getId());
    orders.setStatus(Orders.CANCELLED);
    orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
    orders.setCancelTime(LocalDateTime.now());

    orderMapper.update(orders);
  }

  /**
   * 管理端取消订单 rejection
   * @param ordersCancelDTO
   */
  @Override
  public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) throws Exception {
    // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
    // 获取当前订单
    Long orderId = ordersCancelDTO.getId();
    Orders order = orderMapper.getById(orderId);
    // 判断订单是否存在
    if (order == null) {
      throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
    }
    // 获取当前订单状态
    Integer orderStatus = order.getStatus();
    // 判断当前订单是否可取消：
    // 若已完成或已取消则不可取消
    if (orderStatus == Orders.COMPLETED || orderStatus == Orders.CANCELLED) {
      throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
    }
    // 若已接单或派送中且已付款，则取消后需要退款
    if (orderStatus == Orders.CONFIRMED || orderStatus == Orders.DELIVERY_IN_PROGRESS) {
      if (order.getPayStatus() == Orders.PAID) {
        // 获取微信支付工具类
        //调用微信支付退款接口
        String refund = weChatPayUtil.refund(
            order.getNumber(), //商户订单号
            order.getNumber(), //商户退款单号
            order.getAmount(),//退款金额，单位 元
            order.getAmount());//原订单金额
        //支付状态修改为 退款
        order.setPayStatus(Orders.REFUND);
        log.info("申请退款：{}", refund);
      }
    }
    // 若待付款或待接单，可直接取消
    order.setStatus(Orders.CANCELLED);
    order.setCancelReason(ordersCancelDTO.getCancelReason());
    order.setCancelTime(LocalDateTime.now());
    orderMapper.update(order);
  }

  /**
   * 派送订单
   * @param id
   */
  @Override
  public void delivery(Long id) {
    Orders order = orderMapper.getById(id);
    if (order == null) {
      throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
    }
    if (order.getStatus() != Orders.CONFIRMED) {
      throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
    }
    order.setStatus(Orders.DELIVERY_IN_PROGRESS);
    orderMapper.update(order);
  }

  /**
   * 完成订单
   * @param id
   */
  @Override
  public void complete(Long id) {
    Orders order = orderMapper.getById(id);
    if (order == null) {
      throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
    }
    if (order.getStatus() != Orders.DELIVERY_IN_PROGRESS) {
      throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
    }
    order.setStatus(Orders.COMPLETED);
    order.setDeliveryTime(LocalDateTime.now());
    orderMapper.update(order);
  }


}
