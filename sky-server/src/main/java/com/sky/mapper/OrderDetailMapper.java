package com.sky.mapper;

import com.sky.entity.OrderDetail;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDetailMapper {
  /**
   * 批量插入订单明细数据
   * @param orderDetailList
   */
  void insertBatch(List<OrderDetail> orderDetailList);

  /**
   * 根据多个id查询菜品详情
   * @param ids
   * @return
   */
  List<OrderDetail> listByOrderIds(List<Long> orderIds);
}
