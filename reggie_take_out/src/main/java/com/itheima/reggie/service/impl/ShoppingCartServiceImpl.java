package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mapper.ShoppingCartMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 获取购物车中菜品和套餐
     **/
    @Override
    public List<ShoppingCart> getShoppingCartList(HttpSession session) {
        Object userId = session.getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> list = this.list(lqw);
        //判断是否在售，否则移除
        for (int i = 0; i < list.size(); i++) {
            ShoppingCart shoppingCart = list.get(i);
            Dish dish = dishService.getById(shoppingCart.getDishId());
            Setmeal setmeal = setmealService.getById(shoppingCart.getSetmealId());
            if (dish != null)
                if (dish.getStatus() != 1)
                    list.remove(shoppingCart);
            if (setmeal != null)
                if (setmeal.getStatus() != 1)
                    list.remove(shoppingCart);
        }
        return list;
    }

    /**
     * 向购物车中添加菜品或套餐
     **/
    @Override
    public ShoppingCart addMeal(ShoppingCart shoppingCart, HttpSession session) {
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCart.setUserId((Long) session.getAttribute("user"));
        //获取名字，如果有同名，转变成update
        String name = shoppingCart.getName();
        //查找表中是否有同名商品
        ShoppingCart savedShoppingCart = this.
                getOne(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getName, name));
        //存在同名商品，获取商品数量再加1
        if (savedShoppingCart != null) {
            shoppingCart.setId(savedShoppingCart.getId());
            shoppingCart.setDishFlavor(savedShoppingCart.getDishFlavor());
            shoppingCart.setNumber(savedShoppingCart.getNumber() + 1);
            shoppingCart.setCreateTime(savedShoppingCart.getCreateTime());
        }
        this.saveOrUpdate(shoppingCart);
        return shoppingCart;
    }

    /**
     * 修改购物车中商品
     **/
    @Override
    public ShoppingCart updateShoppingCart(ShoppingCart shoppingCart) {

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getDishId, shoppingCart.getDishId())
                .or()
                .eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        ShoppingCart shoppingCart1 = this.getOne(lqw);

        int number = shoppingCart1.getNumber() - 1;
        shoppingCart1.setNumber(number);

        if (number == 0) {
            this.removeById(shoppingCart1.getId());
            return shoppingCart1;
        }
        this.updateById(shoppingCart1);
        return shoppingCart1;
    }

    @Override
    public boolean clean(HttpSession session) {
        Long userId = (Long) session.getAttribute("user");

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = this.list(lqw);

        List<Long> ids = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            ids.add(shoppingCart.getId());
        }
        if (this.removeByIds(ids))
            return true;
        return false;
    }
}
