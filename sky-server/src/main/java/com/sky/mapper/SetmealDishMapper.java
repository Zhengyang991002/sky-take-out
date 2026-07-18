package com.sky.mapper;

import com.sky.entity.SetmealDish;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SetmealDishMapper {
  /**
   * 新增菜品口味
   * @param setmealDishes
   */
  void saveBatch(List<SetmealDish> setmealDishes);

  /**
   * 查询套餐中的菜品id
   * @param dishIds
   * @return
   */
  List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

  /**
   * 根据套餐id删除套餐和菜品的关联关系
   */
  void deleteBySetmealIds(List<Long> setmealIds);

  /**
   * 根据套餐id查询套餐中的菜品
   * @param setmealId
   * @return
   */
  List<SetmealDish> getBysetmealId(Long setmealId);

  /**
   * 根据id删除置顶套餐中的所有菜品
   * @param setmealId
   */
  void deleteBySetmealId(Long setmealId);
}
