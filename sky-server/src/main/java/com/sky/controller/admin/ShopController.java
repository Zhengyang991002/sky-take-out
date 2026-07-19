package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺管理接口")
public class ShopController {

  @Autowired
  private RedisTemplate redisTemplate;
  private static final String KEY = "SHOP_STATUS";

  /**
   * 设置店铺营业状态
   * @param status
   * @return
   */
  @ApiOperation("设置店铺营业状态")
  @PutMapping("/{status}")
  public Result setStatus(@PathVariable Integer status) {
    log.info("设置店铺营业状态：{}",status == StatusConstant.ENABLE ? "营业中" : "已打烊");
    redisTemplate.opsForValue().set(KEY, status);
    return Result.success();
  }

  /**
   * 查询店铺营业状态
   * @return
   */
  @ApiOperation("查询店铺营业状态")
  @GetMapping("/status")
  public Result<Integer> getStatus() {
    Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
    log.info("查询店铺营业状态: {}", status == StatusConstant.DISABLE ? "已打烊" : "营业中");
    return Result.success(status);
  }

}
