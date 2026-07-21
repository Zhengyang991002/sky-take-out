package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端-购物车接口")
@Slf4j
public class ShoppingCartController {

  @Autowired
  private ShoppingCartService shoppingCartService;

  /**
   * 添加购物车
   * @param shoppingCartDTO
   * @return
   */
  @RequestMapping("/add")
  public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
    log.info("添加购物车：{}", shoppingCartDTO);
    shoppingCartService.addShoppingCart(shoppingCartDTO);
    return Result.success();
  }

}
