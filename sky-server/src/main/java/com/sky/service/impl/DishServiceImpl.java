package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.DishEnableFailedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.Collections;
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
  @Autowired
  private SetmealDishMapper setmealDishMapper;
  @Autowired
  private SetmealMapper setmealMapper;

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

  /**
   * 分页查询菜品
   * @param dishPageQueryDTO
   * @return
   */
  @Override
  public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
    PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
    Page<DishVO> page = dishMapper.page(dishPageQueryDTO);
    return new PageResult(page.getTotal(), page.getResult());
  }

  /**
   * 批量删除菜品
   * @param ids
   */
  @Transactional
  @Override
  public void deleteBatch(List<Long> ids) {
    // 验证是否有菜品被停售
    List<Dish> dishList = dishMapper.getBatchByIds(ids);
    dishList.forEach(dish -> {
      if (dish.getStatus() == 1) {
        throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
      }
    });

    // 验证是否有菜品包含在套餐内
    List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
    if (setmealIds != null && setmealIds.size() > 0) {
      //当前菜品被套餐关联了，不能删除
      throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
    }

    // 删除菜品
    dishMapper.deleteBatchByIds(ids);

    // 删除菜品口味
    dishFlavorMapper.deleteBatchByDishIds(ids);
  }

  /**
   * 根据id查询菜品信息和对应口味信息
   * @param id
   * @return
   */
  @Override
  public DishVO getByWithFlavorId(Long id) {
    // 根据id查询菜品信息
    Dish dish = dishMapper.getById(id);
    // 根据菜品id查询对应口味信息
    List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
    // 封装成DishVO对象并返回
    DishVO dishVO = new DishVO();
    BeanUtils.copyProperties(dish, dishVO);
    dishVO.setFlavors(dishFlavors);
    return dishVO;
  }

  /**
   * 修改菜品信息
   * @param dishDTO
   */
  @Transactional
  @Override
  public void updateWithFlavor(DishDTO dishDTO) {
    // 更新基本菜品信息
    Dish dish = new Dish();
    BeanUtils.copyProperties(dishDTO, dish);
    dishMapper.update(dish);
    // 删除口味信息
    Long dishId = dish.getId();
    dishFlavorMapper.deleteBatchByDishId(dishId);
    // 添加更新后的口味信息
    List<DishFlavor> dishFlavors = dishDTO.getFlavors();
    if (dishFlavors != null && dishFlavors.size() != 0) {
      dishFlavors.forEach(flavor -> {
        flavor.setDishId(dishId);
      });
      dishFlavorMapper.saveBatch(dishFlavors);
    }

  }

  /**
   * 根据分类id查询菜品
   * @param dish
   * @return
   */
  @Override
  public List<Dish> getByCategoryIdAndName(Dish dish) {
    List<Dish> dishList = dishMapper.list(dish);
    return dishList;
  }

  /**
   * 起售或停售菜品
   * @param status
   * @param id
   */
  @Override
  public void startAndStop(Integer status, Long id) {
    Dish dish = new Dish();
    if (status == StatusConstant.DISABLE) {
      // 查看菜品是否已在在售套餐内
      List<Long> dishIdList = Collections.singletonList(id);
      List<Long> setmealIds = setmealDishMapper.getEnabledSetmealIdsByDishIds(dishIdList);
      // 若菜品已在在售套餐内，则先停售套餐，再停售菜品
      if  (setmealIds != null && setmealIds.size() > 0) {
        for (Long setmealId : setmealIds) {
          Setmeal setmeal = new Setmeal();
          setmeal.setId(setmealId);
          setmeal.setStatus(StatusConstant.DISABLE);
          setmealMapper.update(setmeal);
        }
        dish.setStatus(status);
        dish.setId(id);
        dishMapper.update(dish);
      }
    }
    dish.setStatus(status);
    dish.setId(id);
    dishMapper.update(dish);
  }

  /**
   * 条件查询菜品和口味
   * @param dish
   * @return
   */
  public List<DishVO> listWithFlavor(Dish dish) {
    List<Dish> dishList = dishMapper.list(dish);

    List<DishVO> dishVOList = new ArrayList<>();

    for (Dish d : dishList) {
      DishVO dishVO = new DishVO();
      BeanUtils.copyProperties(d,dishVO);

      //根据菜品id查询对应的口味
      List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

      dishVO.setFlavors(flavors);
      dishVOList.add(dishVO);
    }

    return dishVOList;
  }
}
