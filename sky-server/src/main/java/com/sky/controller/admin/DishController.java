package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
}