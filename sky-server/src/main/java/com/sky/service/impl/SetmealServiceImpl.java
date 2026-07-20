package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
  @Autowired
  private SetmealMapper setmealMapper;
  @Autowired
  private SetmealDishMapper setmealDishMapper;
  @Autowired
  private DishMapper dishMapper;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void save(SetmealDTO setmealDTO) {
    Setmeal setmeal = new Setmeal();
    BeanUtils.copyProperties(setmealDTO, setmeal);
    setmealMapper.save(setmeal);
    Long setmealId = setmeal.getId();
    List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
    setmealDishes.forEach(setmealDish -> {
      setmealDish.setSetmealId(setmealId);
    });
    setmealDishMapper.saveBatch(setmealDishes);
  }

  @Override
  public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
    PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
    Page<SetmealVO> page = setmealMapper.page(setmealPageQueryDTO);
    return new PageResult(page.getTotal(), page.getResult());
  }

  /**
   * 批量删除套餐
   * @param ids
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteBatch(List<Long> ids) {
    // 判断套餐是否起售
    List<Setmeal> setmeals = setmealMapper.getByIds(ids);
    setmeals.forEach(setmeal -> {
      if (setmeal.getStatus() == 1) {
        throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
      }
    });
    // 删除套餐
    setmealMapper.deleteBatch(ids);
    // 根据套餐id删除套餐和菜品的关联关系
    setmealDishMapper.deleteBySetmealIds(ids);
  }

  /**
   * 根据id查询套餐
   * @param id
   * @return
   */
  @Override
  public SetmealVO getById(Long id) {
    // 查询套餐基本信息
    Setmeal setmeal = setmealMapper.getById(id);
    // 查询套餐菜品信息
    List<SetmealDish> setmealDishs = setmealDishMapper.getBysetmealId(id);
    // 组装成完成SetmealVO返回
    SetmealVO setmealVO = new SetmealVO();
    BeanUtils.copyProperties(setmeal, setmealVO);
    setmealVO.setSetmealDishes(setmealDishs);
    return setmealVO;
  }

  /**
   * 修改套餐
   * @param setmealDTO
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(SetmealDTO setmealDTO) {
    // 修改套餐基本信息
    Setmeal setmeal = new Setmeal();
    BeanUtils.copyProperties(setmealDTO, setmeal);
    setmealMapper.update(setmeal);
    // 删除套餐原有菜品
    Long setmealId = setmeal.getId();
    setmealDishMapper.deleteBySetmealId(setmealId);
    // 套餐更新后的菜品
    List<SetmealDish> dishList = setmealDTO.getSetmealDishes();
    if (dishList != null && !dishList.isEmpty()) {
      dishList.forEach(setmealDish -> {
        setmealDish.setSetmealId(setmealId);
      });
      // 添加菜单新菜品
      setmealDishMapper.saveBatch(dishList);
    }
  }

  /**
   * 启用禁用套餐
   * @param status
   * @param id
   */
  @Override
  public void startAndStop(Integer status, Long id) {
    // 若套餐包含停售的菜品，则不能起售
    if (status == StatusConstant.ENABLE) {
      List<SetmealDish> setMealDishList = setmealDishMapper.getBysetmealId(id);
      List<Long> dishIds = setMealDishList.stream()
          .map(dish -> dish.getDishId())
          .collect(Collectors.toList());
      List<Dish> dishList = dishMapper.getBatchByIds(dishIds);
      dishList.forEach(dish -> {
        if (dish.getStatus() == StatusConstant.DISABLE) {
          throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
        }
      });
    }
    // 起售或停售套餐
    Setmeal setmeal = new Setmeal();
    setmeal.setId(id);
    setmeal.setStatus(status);
    setmealMapper.update(setmeal);
  }

  /**
   * 条件查询
   * @param setmeal
   * @return
   */
  public List<Setmeal> list(Setmeal setmeal) {
    List<Setmeal> list = setmealMapper.list(setmeal);
    return list;
  }

  /**
   * 根据id查询菜品选项
   * @param id
   * @return
   */
  public List<DishItemVO> getDishItemById(Long id) {
    return setmealMapper.getDishItemBySetmealId(id);
  }
}
