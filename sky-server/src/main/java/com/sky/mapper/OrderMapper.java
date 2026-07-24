package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper {
  /**
   * 插入订单数据
   * @param order
   */
  void insert(Orders order);

  /**
   * 根据订单号查询订单
   * @param orderNumber
   */
  @Select("select * from orders where number = #{orderNumber}")
  Orders getByNumber(String orderNumber);

  /**
   * 修改订单信息
   * @param orders
   */
  void update(Orders orders);

  /**
   * 订单分页查询
   * @param ordersPageQueryDTO
   * @return
   */
  Page<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);

  /**
   * 根据id获取订单
   * @param id
   * @return
   */
  OrderVO getOrderById(Long id);

  /**
   * 根据id查询订单
   * @param id
   * @return
   */
  @Select("select * from orders where id = #{id}")
  Orders getById(Long id);

  /**
   * 统计各状态订单
   * @param toBeConfirmed
   * @return
   */
  @Select("select count(id) from orders where status = #{status}")
  Integer countStatus(Integer toBeConfirmed);

  /**
   * 获取超时订单
   * @param status
   * @param orderTimeLimit
   * @return
   */
  @Select("select * from orders where status = #{status} and order_time < #{orderTimeLimit}")
  List<Orders> getTimeOutOrders(Integer status, LocalDateTime orderTimeLimit);
}
