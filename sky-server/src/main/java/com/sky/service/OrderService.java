package com.sky.service;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

  /**
   * 用户订单提交
   * @param ordersSubmitDTO
   * @return
   */
  OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) throws Exception;

  /**
   * 订单支付
   * @param ordersPaymentDTO
   * @return
   */
  OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

  /**
   * 支付成功，修改订单状态
   * @param outTradeNo
   */
  void paySuccess(String outTradeNo);

  /**
   * 分页查询历史订单
   * @param ordersPageQueryDTO
   * @return
   */
  PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

  /**
   * 获取订单详情
   * @param id
   * @return
   */
  OrderVO getOrderDetailById(Long id);

  /**
   * 取消订单
   * @param id
   */
  void cancel(Long id) throws Exception;

  /**
   * 再来一单
   * @param id
   */
  void reorder(Long id);

  /**
   * 管理端订单查询
   * @param ordersPageQueryDTO
   * @return
   */
  PageResult pageConditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

  /**
   * 各个状态的订单数量统计
   * @return
   */
  OrderStatisticsVO statistics();

  /**
   * 接单
   * @param ordersConfirmDTO
   */
  void confirm(OrdersConfirmDTO ordersConfirmDTO);

  /**
   * 订单拒接
   * @param ordersRejectionDTO
   */
  void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

  /**
   * 管理端取消订单
   * @param ordersCancelDTO
   */
  void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) throws Exception;

  /**
   * 派送订单
   * @param id
   */
  void delivery(Long id);

  /**
   * 完成订单
   * @param id
   */
  void complete(Long id);

  /**
   * 客户催单
   * @param id
   */
  void reminder(Long id);
}
