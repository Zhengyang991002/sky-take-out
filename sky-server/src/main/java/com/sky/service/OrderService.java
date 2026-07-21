package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {

  /**
   * 用户订单提交
   * @param ordersSubmitDTO
   * @return
   */
  OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
}
