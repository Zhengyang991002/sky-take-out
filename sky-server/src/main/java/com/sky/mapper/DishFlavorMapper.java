package com.sky.mapper;

import com.sky.entity.DishFlavor;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishFlavorMapper {
  void saveBatch(List<DishFlavor> dishFlavors);
}
