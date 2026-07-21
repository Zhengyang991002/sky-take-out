package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShoppingCartMapper {
  /**
   * 查看购物车中是否有该菜品或套餐
   * @param shoppingCart
   * @return
   */
  List<ShoppingCart> list(ShoppingCart shoppingCart);

  /**
   * 修改购物车菜品数量
   * @param shoppingCart
   */
  void updateNumber(ShoppingCart shoppingCart);

  /**
   * 添加购物车
   * @param shoppingCart
   */
  void insert(ShoppingCart shoppingCart);
}
