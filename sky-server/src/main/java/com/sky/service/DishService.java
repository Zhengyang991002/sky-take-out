package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import java.util.List;

public interface DishService {

  /**
   * 新增菜品
   * @param dishDTO
   */
  void saveWithFlavor(DishDTO dishDTO);

  /**
   * 分页查询菜品
   * @param DishPageQueryDTO
   * @return
   */
  PageResult page(DishPageQueryDTO dishPageQueryDTO);

  /**
   * 批量删除菜品
   * @param ids
   */
  void deleteBatch(List<Long> ids);

  /**
   * 根据id查询菜品信息和对应口味信息
   * @param id
   * @return
   */
  DishVO getByWithFlavorId(Long id);

  /**
   * 修改菜品信息
   * @param dishDTO
   */
  void updateWithFlavor(DishDTO dishDTO);

  /**
   * 根据分类查询菜品
   * @param categoryId
   * @return
   */
  List<Dish> getByCategoryId(Long categoryId);

  /**
   * 起售和停售菜品
   * @param status
   * @param id
   */
  void startAndStop(Integer status, Long id);
}
