package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
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

  void deleteBatch(List<Long> ids);
}
