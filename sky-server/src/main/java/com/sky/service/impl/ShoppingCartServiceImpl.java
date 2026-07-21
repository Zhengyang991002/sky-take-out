package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
  @Autowired
  private ShoppingCartMapper shoppingCartMapper;
  @Autowired
  private DishMapper dishMapper;
  @Autowired
  private SetmealMapper setmealMapper;

  /**
   * 添加购物车
   * @param shoppingCartDTO
   */
  @Override
  public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
    // 1. 构建查询条件对象
    ShoppingCart shoppingCart = new ShoppingCart();
    BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
    Long userId = BaseContext.getCurrentId();
    shoppingCart.setUserId(userId);

    // 2. 查询当前用户的购物车中是否已存在该菜品/套餐（需注意 XML 中匹配 dishFlavor）
    List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

    // 3. 若已经存在，则数量加 1
    if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
      ShoppingCart cartItem = shoppingCartList.get(0);
      cartItem.setNumber(cartItem.getNumber() + 1);
      // 更新数据库中的数量
      shoppingCartMapper.updateNumber(cartItem);
    } else {
      // 4. 若不存在，补全菜品/套餐的具体信息（名称、图片、单价），然后插入一条新记录
      Long dishId = shoppingCartDTO.getDishId();
      if (dishId != null) {
        // 添加的是菜品
        Dish dish = dishMapper.getById(dishId);
        shoppingCart.setAmount(dish.getPrice());
        shoppingCart.setName(dish.getName());
        shoppingCart.setImage(dish.getImage());
      } else {
        // 添加的是套餐
        Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
        shoppingCart.setAmount(setmeal.getPrice());
        shoppingCart.setName(setmeal.getName());
        shoppingCart.setImage(setmeal.getImage());
      }

      // 补全默认属性
      shoppingCart.setNumber(1);
      shoppingCart.setCreateTime(LocalDateTime.now());

      // 执行插入
      shoppingCartMapper.insert(shoppingCart);
    }
  }

  /**
   * 查看购物车
   * @return
   */
  @Override
  public List<ShoppingCart> list() {
    // 1. 获取当前用户
    Long userId = BaseContext.getCurrentId();
    // 2. 查询当前用户购物车中所有数据
    ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
    return shoppingCartMapper.list(shoppingCart);
  }

  /**
   * 清空购物车
   */
  @Override
  public void clean() {
    Long userId = BaseContext.getCurrentId();
    shoppingCartMapper.deleteByUserId(userId);
  }

  /**
   * 删除购物车中一个商品
   * @param shoppingCartDTO
   */
  @Override
  public void sub(ShoppingCartDTO shoppingCartDTO) {
    // 1. 获取当前用户
    Long userId = BaseContext.getCurrentId();
    // 2. 查询当前用户的购物车中要被删除的菜品或套餐
    ShoppingCart shoppingCart = new ShoppingCart();
    BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
    shoppingCart.setUserId(userId);
    List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
    // 4. 判断菜品或套餐的数量是否大于1
    if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
      ShoppingCart cartItem = shoppingCartList.get(0);
      Integer number = cartItem.getNumber();
      if (number != null && number > 1) {
        // 菜品或套餐数量大于1，则number减1
        cartItem.setNumber(cartItem.getNumber() - 1);
        // 执行更新
        shoppingCartMapper.updateNumber(cartItem);
      } else {
        // 菜品或套餐数量等于1，则从购物车中删除该菜品或套餐
        Long id = cartItem.getId();
        shoppingCartMapper.deleteById(id);
      }
    }
  }

}
