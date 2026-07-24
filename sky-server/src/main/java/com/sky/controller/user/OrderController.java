package com.sky.controller.user;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端订单接口")
public class OrderController {

  @Autowired
  private OrderService orderService;

  /**
   * 用户下单
   * @param ordersSubmitDTO
   * @return
   */
  @PostMapping("/submit")
  @ApiOperation("用户下单")
  public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO)
      throws Exception {
    log.info("用户下单提交: {}", ordersSubmitDTO);
    OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
    return Result.success(orderSubmitVO);
  }

  /**
   * 订单支付
   *
   * @param ordersPaymentDTO
   * @return
   */
  @PutMapping("/payment")
  @ApiOperation("订单支付")
  public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
    log.info("订单支付：{}", ordersPaymentDTO);
    OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
    log.info("生成预支付交易单：{}", orderPaymentVO);
    return Result.success(orderPaymentVO);
  }

  /**
   * 用户历史订单分页查询
   * @param ordersPageQueryDTO
   * @return
   */
  @GetMapping("/historyOrders")
  @ApiOperation("历史订单分页查询")
  public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
    log.info("历史订单分页查询: {}", ordersPageQueryDTO);
    PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
    return Result.success(pageResult);
  }

  /**
   * 查看订单详情
   * @param id
   * @return
   */
  @GetMapping("/orderDetail/{id}")
  @ApiOperation("查询订单详情")
  public Result<OrderVO> getOrderDetailById(@PathVariable Long id) {
    log.info("查询订单详情，订单id为:{}", id);
    OrderVO orderVO = orderService.getOrderDetailById(id);
    return Result.success(orderVO);
  }

  /**
   * 取消订单
   * @param id
   * @return
   */
  @PutMapping("/cancel/{id}")
  @ApiOperation("取消订单")
  public Result cancel(@PathVariable Long id) throws Exception {
    log.info("取消订单id:{}", id);
    orderService.cancel(id);
    return Result.success();
  }

  /**
   * 再来一单
   * @param id
   * @return
   */
  @ApiOperation("再来一单")
  @PostMapping("/repetition/{id}")
  public Result reorder(@PathVariable Long id) {
    log.info("再来一单id:{}", id);
    orderService.reorder(id);
    return Result.success();
  }

  /**
   * 订单催单
   * @param id
   * @return
   */
  @GetMapping("/reminder/{id}")
  @ApiOperation("客户催单")
  public Result reminder(@PathVariable Long id) {
    log.info("催单id:{}", id);
    orderService.reminder(id);
    return Result.success();
  }
}
