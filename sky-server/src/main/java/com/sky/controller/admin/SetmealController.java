package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐管理")
public class SetmealController {
  @Autowired
  private SetmealService setmealService;
  @Autowired
  private DishMapper dishMapper;
  @Autowired
  private DishService dishService;

  /**
   * 新增套餐
   * @param setmealDTO
   * @return
   */
  @ApiOperation(value = "新增套餐")
  @PostMapping
  public Result save(@RequestBody SetmealDTO setmealDTO) {
    log.info("新增套餐: {}", setmealDTO);
    setmealService.save(setmealDTO);
    return Result.success();
  }

  /**
   * 套餐分页查询
   * @param setmealPageQueryDTO
   * @return
   */
  @ApiOperation(value = "套餐分页查询")
  @GetMapping("/page")
  public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
    log.info("套餐分页查询: {}", setmealPageQueryDTO);
    PageResult pageResult = setmealService.page(setmealPageQueryDTO);
    return Result.success(pageResult);
  }

  /**
   * 批量删除套餐
   * @param ids
   * @return
   */
  @ApiOperation(value = "批量删除套餐")
  @DeleteMapping
  public Result delete(@RequestParam List<Long> ids) {
    log.info("批量删除套餐: {}", ids);
    setmealService.deleteBatch(ids);
    return Result.success();
  }

  /**
   * 根据id查询套餐
   * @param id
   * @return
   */
  @ApiOperation("根据id查询套餐")
  @GetMapping("/{id}")
  public Result<SetmealVO> getById(@PathVariable Long id) {
    log.info("根据id查询套餐信息: {}", id);
    SetmealVO setmealVO = setmealService.getById(id);
    return Result.success(setmealVO);
  }

  /**
   * 修改套餐
   * @param setmealDTO
   * @return
   */
  @PutMapping
  @ApiOperation("修改套餐")
  public Result update(@RequestBody SetmealDTO setmealDTO) {
    log.info("修改套餐: {}", setmealDTO);
    setmealService.update(setmealDTO);
    return Result.success();
  }

  /**
   * 起售停售套餐
   * @param status
   * @param id
   * @return
   */
  @PostMapping("/status/{status}")
  @ApiOperation("起售，停售套餐")
  public Result startAndStop(@PathVariable Integer status, Long id) {
    if (status == 1) {
      log.info("起售套餐id: {}", id);
    } else if (status ==0) {
      log.info("停售套餐id, {}", id);
    }
    setmealService.startAndStop(status, id);
    return Result.success();
  }

}
