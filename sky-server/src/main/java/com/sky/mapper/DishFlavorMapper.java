package com.sky.mapper;

import com.sky.entity.DishFlavor;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishFlavorMapper {
  /**
   * 保存菜品口味信息
   * @param dishFlavors
   */
  void saveBatch(List<DishFlavor> dishFlavors);

  /**
   * 根据菜品id删除所有菜品口味信息
   * @param dishIds
   */
  void deleteBatchByDishIds(List<Long> dishIds);

  /**
   * 根据菜品id查询菜品所有口味信息
   * @param dishId
   * @return
   */
  @Select("SELECT * FROM dish_flavor WHERE dish_id = #{dishId}")
  List<DishFlavor> getByDishId(Long dishId);

  /**
   * 根据菜品id删除菜品口味信息
   * @param dishId
   */
  @Delete("DELETE FROM dish_flavor WHERE dish_id = #{dishId}")
  void deleteBatchByDishId(Long dishId);
}
