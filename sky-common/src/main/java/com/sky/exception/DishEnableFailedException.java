package com.sky.exception;

/**
 * 菜品起售失败异常
 */
public class DishEnableFailedException extends BaseException {

    public DishEnableFailedException(){}

    public DishEnableFailedException(String msg){
        super(msg);
    }
}
