package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper
public interface SetmealMapper {
  /**
   * 添加套餐
   * @param setmeal
   */
  @AutoFill(OperationType.INSERT)
  void save(Setmeal setmeal);

  /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

  /**
   * 分页查询套餐
    * @param setmealPageQueryDTO
   * @return
   */
    Page<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO);

  /**
   * 根据id批量查询套餐
   * @param ids
   * @return
   */
    List<Setmeal> getByIds(@Param("ids") List<Long> ids);

  /**
   * 根据id批量删除套餐
   * @param ids
   */
  void deleteBatch(@Param("ids") List<Long> ids);

  // 根据单个id查询套餐
  @Select("select * from setmeal where id = #{id}")
  Setmeal getById(Long id);

  /**
   * 更新套餐基本信息
   * @param setmeal
   */
  @AutoFill(OperationType.UPDATE)
  void update(Setmeal setmeal);
}
