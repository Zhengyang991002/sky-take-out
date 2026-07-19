package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Api(tags = "菜品管理")
@RequestMapping("/admin/dish")
public class DishController {
  @Autowired
  private DishService dishService;
  @Autowired
  private DishMapper dishMapper;

  /**
   * 新增菜品
   * @param dishDTO
   * @return
   */
  @ApiOperation(value = "新增菜品")
  @PostMapping
  public Result save(@RequestBody DishDTO dishDTO) {
    log.info("新增菜品: {}", dishDTO);
    dishService.saveWithFlavor(dishDTO);
    return Result.success();
  }

  /**
   * 分页查询菜品
   * @param dishPageQueryDTO
   * @return
   */
  @ApiOperation(value = "分页查询菜品")
  @GetMapping("/page")
  public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
    log.info("分页查询菜品: {}", dishPageQueryDTO);
    PageResult pageResult = dishService.page(dishPageQueryDTO);
    return Result.success(pageResult);
  }

  /**
   * 批量删除菜品
   * @param ids
   * @return
   */
  @ApiOperation(value = "批量删除菜品")
  @DeleteMapping
  public Result delete(@RequestParam List<Long> ids) {
    log.info("批量删除菜品: {}", ids);
    dishService.deleteBatch(ids);
    return Result.success();
  }

  /**
   * 根据id查询菜品以及其口味信息
   * @param id
   * @return
   */
  @ApiOperation(value = "根据ID查询菜品")
  @GetMapping("/{id}")
  public Result<DishVO> getById(@PathVariable Long id) {
    log.info("根据ID查询菜品: {}", id);
    DishVO dishVO = dishService.getByWithFlavorId(id);
    return Result.success(dishVO);
  }

  /**
   * 修改菜品信息
   * @param dishDTO
   * @return
   */
  @ApiOperation(value = "修改菜品信息")
  @PutMapping
  public Result update(@RequestBody DishDTO dishDTO) {
    log.info("修改菜品信息: {}", dishDTO);
    dishService.updateWithFlavor(dishDTO);
    return Result.success();
  }

  /**
   * 根据分类id查询菜品
   * @param dish
   * @return
   */
  @ApiOperation(value = "根据分类id查询菜品")
  @GetMapping("/list")
  public Result<List<Dish>> getByCategoryIdAndName(Dish dish) {
    log.info("根据分类id和菜品名查询菜品: {}", dish);
    List<Dish> dishList = dishService.getByCategoryIdAndName(dish);
    return Result.success(dishList);
  }

  /**
   * 菜品起售、停售
   * @param status
   * @param id
   * @return
   */
  @PostMapping("/status/{status}")
  @ApiOperation("起售停售菜品")
  public Result startAndStop(@PathVariable Integer status, Long id) {
    if (status == 1) {
      log.info("起售菜品id: {}", id);
    } else if (status == 0) {
      log.info("停售菜品id: {}", id);
    }
    dishService.startAndStop(status, id);
    return Result.success();
  }
}