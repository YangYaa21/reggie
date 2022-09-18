package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 保存菜品及口味信息
     **/
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //1.保存相应基本数据到dish表
        this.save(dishDto);
        //2.获取菜品id
        Long dishId = dishDto.getId();
        //3.将菜品id设置进口味中
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);//将菜品id存到集合中
            return item;
        }).collect(Collectors.toList());//转换为集合
        //4.菜品口味保存
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品及口味信息
     **/
    @Override
    @Transactional
    public boolean deleteWithFlavor(List<Long> ids) {
        //拿到ids，检测菜品是否在售
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId, ids);
        dishLambdaQueryWrapper.eq(Dish::getStatus, 1);
        int count = this.count(dishLambdaQueryWrapper);
        //如果在售抛异常，不能删除
        if (count > 0)
            return false;
        //否则正常删除，首先删除dish表信息
        this.removeByIds(ids);
        //再删除dishFlavor表信息
        LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        flavorLambdaQueryWrapper.in(DishFlavor::getId, ids);
        dishFlavorService.removeByIds(ids);

        //要删除菜品集合ids，去setmealDish表找含有removeDishId的记录
        for (int i = 0; i < ids.size(); i++) {
            Long removedDishId = ids.get(i);
            LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
            lqw.eq(SetmealDish::getDishId, removedDishId);
            //被删除的菜品集合
            List<SetmealDish> deletedDishes = setmealDishService.list(lqw);
            //设置含有被删除菜品的套餐内菜品IsDeleted为1
            for (SetmealDish setmealDish : deletedDishes) {
                setmealDish.setIsDeleted(1);
                setmealDishService.removeById(setmealDish.getId());
                setmealDishService.save(setmealDish);
            }
        }
        return true;
    }

    /**
     * 更新菜品及口味信息
     **/
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品表
        this.updateById(dishDto);
        //清理当前菜品对应的口味数据
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);
        //加载当前提交过来的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        //批量更新
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id获取菜品信息及口味信息
     **/
    @Override
    public DishDto getByIdWithFlavor(Long id) {

        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 分页查询菜品
     **/
    @Override
    public Page getPage(int page, int pageSize, String name) {
        Page<Dish> pa = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page(page, pageSize);
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(Strings.isNotEmpty(name), Dish::getName, name);
        lqw.orderByAsc(Dish::getCreateTime);
        this.page(pa, lqw);
        BeanUtils.copyProperties(pa, dishDtoPage, "records");

        List<Dish> records = pa.getRecords();//获取集合
        List<DishDto> list = records.stream().map((item) -> {//遍历集合
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//获取分类id
            Category category = categoryService.getById(categoryId);//根据id查询分类信息
            //对查出来的分类做判断可能有的菜品没有分类
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());//转换成集合
        dishDtoPage.setRecords(list);

        return dishDtoPage;
    }

    /**
     * 根据菜品分类Id或者name取dish集合
     **/
    @Override
    public List getList(Long categoryId, String name, Integer statu) {
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        if (name == null)
            lqw.eq(Dish::getCategoryId, categoryId);
        if (categoryId == null)
            lqw.like(Strings.isNotEmpty(name), Dish::getName, name);
        if (statu == 2)//商家端传的1，需要展示停售菜品，用户端传的是2，只需要展示在售菜品
            lqw.eq(Dish::getStatus, 1);
        List<Dish> dishes = this.list(lqw);

        List<DishDto> dishDtos = new ArrayList<>();
        for (Dish dish : dishes) {
            //判断是否有停售菜品
            //管理端套餐添加菜品获取集合时需要展示停售菜品
            //而用户端获取菜品时如果展示则能够添加停售商品
//            if (dish.getStatus() != 1)
//                continue;
            //根据dishId获取flavor集合，放入dishDto
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, dish.getId());
            List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

            DishDto dishDto = new DishDto();
            dishDto.setId(dish.getId());
            dishDto.setName(dish.getName());
            dishDto.setCategoryId(dish.getCategoryId());
            dishDto.setPrice(dish.getPrice());
            dishDto.setCode(dish.getCode());
            dishDto.setImage(dish.getImage());
            dishDto.setDescription(dish.getDescription());
            dishDto.setStatus(dish.getStatus());
            dishDto.setSort(dish.getSort());
            dishDto.setCreateTime(dish.getCreateTime());
            dishDto.setUpdateTime(dish.getUpdateTime());
            dishDto.setCreateUser(dish.getCreateUser());
            dishDto.setUpdateUser(dish.getUpdateUser());
            dishDto.setIsDeleted(dish.getIsDeleted());
//            dishDto.setCopies(copies[j]);
            dishDto.setFlavors(flavors);

            dishDtos.add(dishDto);
        }

        return dishDtos;
    }

}
