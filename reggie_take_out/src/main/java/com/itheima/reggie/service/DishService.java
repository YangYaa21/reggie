package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.dto.DishDto;

import java.util.List;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);

    boolean deleteWithFlavor(List<Long> ids);

    void updateWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    Page getPage(int page, int pageSize, String name);

    List getList(Long categoryId, String name, Integer statu);
}
