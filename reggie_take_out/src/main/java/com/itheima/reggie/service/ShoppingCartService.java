package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.ShoppingCart;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    List<ShoppingCart> getShoppingCartList(HttpSession session);

    ShoppingCart addMeal(ShoppingCart shoppingCart, HttpSession session);

    ShoppingCart updateShoppingCart(ShoppingCart shoppingCart);

    boolean clean(HttpSession session);
}
