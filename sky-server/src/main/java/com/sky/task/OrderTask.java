package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderTask {

  @Autowired
  private OrderMapper orderMapper;
  /**
   * 定时处理超时订单
   */
  @Scheduled(cron = "0 * * * * ?")
  public void processTimeOutOrder() {
    log.info("处理超时订单");
    // 获得超时订单
    LocalDateTime orderTimeLimit = LocalDateTime.now().plusMinutes(-15);
    Integer status = Orders.PENDING_PAYMENT;
    List<Orders> timeoutOrders = orderMapper.getTimeOutOrders(status, orderTimeLimit);
    if (timeoutOrders != null && timeoutOrders.size() > 0) {
      for (Orders order : timeoutOrders) {
        log.info("处理订单：{}", order.getId());
        order.setCancelTime(LocalDateTime.now());
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("支付超时，订单自动取消");
        orderMapper.update(order);
      }
    }
  }

  /**
   * 定时处理处于派送中的订单
   */
  @Scheduled(cron = "0 0 1 * * ?")
  public void processCompletedOrder() {
    log.info("处理超时派送订单");
    // 获得派送中的订单
    LocalDateTime confirmTimeLimit = LocalDateTime.now().plusHours(-1);
    Integer status = Orders.DELIVERY_IN_PROGRESS;
    List<Orders> timeoutOrders = orderMapper.getTimeOutOrders(status, confirmTimeLimit);
    if (timeoutOrders != null && timeoutOrders.size() > 0) {
      for (Orders order : timeoutOrders) {
        log.info("处理订单：{}", order.getId());
        order.setStatus(Orders.COMPLETED);
        orderMapper.update(order);
      }
    }
  }


}
