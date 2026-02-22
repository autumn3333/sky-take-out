package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    void insertBatch(List<DishFlavor> flavors);

    /*
    * 根据菜品ID删除口味
    * */
    @Delete("delete from dish_flavor where dish_id =#{dishId}")
    void deleteByDishId(Long dishId);

    /*根据菜品ID集合批量删除关联口味数据*/
    void deleteByDishIds(List<Long> dishIds);

    //根据ID查询菜品口味
    @Select("select * from dish_flavor where  dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
