package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 分页查询菜品
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<IPage> getPage(int page, int pageSize, String name) {
        return R.success(dishService.getPage(page, pageSize, name));
    }

    /**
     * 查单个菜品详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 根据菜品分类Id或者name取dish集合
     *
     * @param categoryId
     * @param name
     * @return
     */
    @GetMapping("/list")
    public R<List> getList(Long categoryId, String name, Integer statu) {
        return R.success(dishService.getList(categoryId, name, statu));
    }

    /**
     * 添加菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> add(@RequestBody DishDto dishDto) {
        if (dishDto.getFlavors() == null) {
            dishService.save(dishDto);
            return R.success("菜品添加成功");
        }
        dishService.saveWithFlavor(dishDto);
        return R.success("菜品添加成功");
    }

    /**
     * （批量）删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        if (!dishService.deleteWithFlavor(ids))
            return R.error("菜品在售");
        return R.success("删除成功");
    }

    /**
     * 修改菜品信息
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功!");
    }

    /**
     * 批量起/停售
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> editStatus(@PathVariable Integer status, Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            Dish dish = dishService.getById(ids[i]);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("状态修改成功");
    }

}
