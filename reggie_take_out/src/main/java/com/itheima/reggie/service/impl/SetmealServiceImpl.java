package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    public SetmealDto searchById(Long id) {
        Setmeal setmeal = setmealService.getById(id);

        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(lqw);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setSetmealDishes(setmealDishList);
        return setmealDto;
    }

    @Override
    public void updateStatus(Long[] ids, Integer status) {

        List<Long> longList = Arrays.asList(ids);
        List<Setmeal> lists = this.listByIds(longList);
        for (Setmeal setmeal : lists) {
            setmeal.setStatus(status);
            this.updateById(setmeal);
        }
    }

    @Override
    @Transactional
    public void updateCombo(SetmealDto setmealDto) {
        List<SetmealDish> dishList = setmealDto.getSetmealDishes();
        String setmealId = setmealDto.getId().toString();
        for (SetmealDish setmealDish : dishList
        ) {
            setmealDish.setSetmealId(setmealId);
        }
        log.info(dishList.toString());
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, setmealId);
        setmealDishService.remove(lqw);
        setmealDishService.saveOrUpdateBatch(dishList);
        setmealService.updateById((setmealDto));
    }

    @Override
    @Transactional
    public R<String> deleteIds(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId, ids);
        lambdaQueryWrapper.eq(Setmeal::getStatus, 1);

        int count = this.count(lambdaQueryWrapper);
        if (count > 0) {
//            throw new DiyException("套餐在售");
            return R.error("套餐在售");
        }

        //删除setmealDish表对应数据
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lqw);
        //删除setmeal表对应数据
        setmealService.removeByIds(ids);
        return R.success("删除成功");

    }

    @Override
    public IPage getPage(Integer page, Integer pageSize, String name) {
        Page<Setmeal> pa = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(Strings.isNotEmpty(name), Setmeal::getName, name);
        lqw.orderByAsc(Setmeal::getCreateTime);
        setmealService.page(pa, lqw);
        //将pa数据拷贝到dtoPage，忽略records
        BeanUtils.copyProperties(pa, dtoPage, "records");

        //取出pa中数据放在records集合
        List<Setmeal> records = pa.getRecords();
        //将records中数据逐个取出，每一项拷贝到setmealDto
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //查询对应分类，有分类加上分类名，没有的直接return
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());//转换为集合
        //填充dtoPage的records
        dtoPage.setRecords(list);
        return dtoPage;
    }

    @Override
    @Transactional
    public void saveCombo(SetmealDto setmealDto) {
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        setmealService.save(setmealDto);
        String setmealId = setmealDto.getId().toString();
        for (SetmealDish setmealDish : dishes) {
            setmealDish.setSetmealId(setmealId);
        }
        setmealDishService.saveBatch(dishes);
    }

    @Override
    @Transactional
    public List dishes(Long id) {
        //查看套餐详情，展示套餐内菜品详情，首先根据套餐id获取setmealDish列表
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(lqw);

        Long[] ids = new Long[100];//存放dishId
        Integer[] copies = new Integer[100];//存放数量
        int i = 0;
        //取出菜品集合中所有的dishId ids[]和数量copies[]
        for (SetmealDish setmealDish : setmealDishList) {
            ids[i] = Long.valueOf(setmealDish.getDishId());
            copies[i] = setmealDish.getCopies();
            i++;
        }
        //挨个赋值存进dishDtos
        List<DishDto> dishDtos = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            DishDto dishDto = new DishDto();
            //根据dishId取出dishFlavor集合
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, ids[j]);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);

            Dish dish = dishService.getById(ids[j]);
            //判断菜品是否被删除，是则停售并删除该套餐
            //也可以只停售不删除套餐，前端一样不会展示停售套餐，方便商家管理端修改
//            if (dish == null || dish.getIsDeleted() == 1) {
//                log.info("判断菜品是否被删除，是则停售并删除该套餐");
//                List<Long> deleteSetmeal = new ArrayList<>();
//                deleteSetmeal.add(id);
//                Setmeal setmeal = this.getById(id);
//                setmeal.setStatus(0);
//                this.updateById(setmeal);
//                this.deleteIds(deleteSetmeal);
//                dishDtos.clear();
//                return dishDtos;
//            }
            //判断是否有停售菜品，将套餐停售，返回空list
            if (dish == null || dish.getIsDeleted() == 1 || dish.getStatus() == 0) {
                log.info("判断是否有停售菜品，将套餐停售，返回空list");
                Setmeal setmeal = this.getById(id);
                setmeal.setStatus(0);
                this.updateById(setmeal);
                dishDtos.clear();
                return dishDtos;
            }
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
            dishDto.setCopies(copies[j]);
            dishDto.setFlavors(dishFlavors);
            dishDto.setIsDeleted(dish.getIsDeleted());
            dishDtos.add(dishDto);
        }
        return dishDtos;
    }
}
