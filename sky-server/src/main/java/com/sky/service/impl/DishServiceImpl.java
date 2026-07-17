package com.sky.service.impl;


import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
  @Autowired
  private DishMapper dishMapper;
  @Autowired
  private DishFlavorMapper dishFlavorMapper;

  /**
   * 新增菜品
   * @param dishDTO
   */
  @Transactional
  @Override
  public void saveWithFlavor(DishDTO dishDTO) {
    Dish dish = new Dish();
    BeanUtils.copyProperties(dishDTO, dish);
    dishMapper.insert(dish);
    Long dishId = dish.getId();
    List<DishFlavor> dishFlavors = dishDTO.getFlavors();
    if (dishFlavors != null && dishFlavors.size() != 0) {
      dishFlavors.forEach(flavor -> {
        flavor.setDishId(dishId);
      });
      dishFlavorMapper.saveBatch(dishFlavors);
    }
  }
}
