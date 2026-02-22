package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DishService {
    /*新增菜品与对应口味数据
    * */
    public void saveWithFlavor(DishDTO dishDTO);


    /*菜品分页查询*/
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /*菜品批量删除*/
    void deleteBatch(List<Long> ids);

    /*根据id查询菜品和口味*/
    DishVO getByIdWithFlavor(Long id);

    /*根据id修改菜品和口味*/
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 菜品起售停售
     * @param status
     * @param id
     * @return
     */
    void startOrStop(Integer status, Long id);


    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> list(Long categoryId);
}
