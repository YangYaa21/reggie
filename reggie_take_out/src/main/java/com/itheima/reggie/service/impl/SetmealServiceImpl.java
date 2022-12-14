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
//            throw new DiyException("????????????");
            return R.error("????????????");
        }

        //??????setmealDish???????????????
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lqw);
        //??????setmeal???????????????
        setmealService.removeByIds(ids);
        return R.success("????????????");

    }

    @Override
    public IPage getPage(Integer page, Integer pageSize, String name) {
        Page<Setmeal> pa = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(Strings.isNotEmpty(name), Setmeal::getName, name);
        lqw.orderByAsc(Setmeal::getCreateTime);
        setmealService.page(pa, lqw);
        //???pa???????????????dtoPage?????????records
        BeanUtils.copyProperties(pa, dtoPage, "records");

        //??????pa???????????????records??????
        List<Setmeal> records = pa.getRecords();
        //???records??????????????????????????????????????????setmealDto
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            //???????????????????????????????????????????????????????????????return
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());//???????????????
        //??????dtoPage???records
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
        //?????????????????????????????????????????????????????????????????????id??????setmealDish??????
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(lqw);

        Long[] ids = new Long[100];//??????dishId
        Integer[] copies = new Integer[100];//????????????
        int i = 0;
        //??????????????????????????????dishId ids[]?????????copies[]
        for (SetmealDish setmealDish : setmealDishList) {
            ids[i] = Long.valueOf(setmealDish.getDishId());
            copies[i] = setmealDish.getCopies();
            i++;
        }
        //??????????????????dishDtos
        List<DishDto> dishDtos = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            DishDto dishDto = new DishDto();
            //??????dishId??????dishFlavor??????
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, ids[j]);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);

            Dish dish = dishService.getById(ids[j]);
            //????????????????????????????????????????????????????????????
            //??????????????????????????????????????????????????????????????????????????????????????????????????????
//            if (dish == null || dish.getIsDeleted() == 1) {
//                log.info("????????????????????????????????????????????????????????????");
//                List<Long> deleteSetmeal = new ArrayList<>();
//                deleteSetmeal.add(id);
//                Setmeal setmeal = this.getById(id);
//                setmeal.setStatus(0);
//                this.updateById(setmeal);
//                this.deleteIds(deleteSetmeal);
//                dishDtos.clear();
//                return dishDtos;
//            }
            //?????????????????????????????????????????????????????????list
            if (dish == null || dish.getIsDeleted() == 1 || dish.getStatus() == 0) {
                log.info("?????????????????????????????????????????????????????????list");
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
