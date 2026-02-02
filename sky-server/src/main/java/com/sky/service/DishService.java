package com.sky.service;

import com.sky.dto.DishDTO;
import org.springframework.stereotype.Service;


public interface DishService {
    /*新增菜品与对应口味数据
    * */
    public void saveWithFlavor(DishDTO dishDTO);
}
