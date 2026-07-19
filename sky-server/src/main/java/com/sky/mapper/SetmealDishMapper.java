package com.sky.mapper;

import com.sky.entity.SetmealDish;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Primary;

@Mapper
public interface SetmealDishMapper {
  /**
   * 新增菜品口味
   * @param setmealDishes
   */
  void saveBatch(@Param("setmealDishs") List<SetmealDish> setmealDishes);

  /**
   * 查询套餐中的菜品id
   * @param dishIds
   * @return
   */
  List<Long> getSetmealIdsByDishIds(@Param("dishIds") List<Long> dishIds);

  /**
   * 根据套餐id删除套餐和菜品的关联关系
   */
  void deleteBySetmealIds(@Param("setmealIds") List<Long> setmealIds);

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

  /**
   * 根据菜品id查找起售的套餐id
   * @param dishIdList
   * @return
   */
  List<Long> getEnabledSetmealIdsByDishIds(@Param("dishIdList") List<Long> dishIdList);
}
