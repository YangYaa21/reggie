package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 获取购物车中商品
     *
     * @param session
     * @return
     */
    @GetMapping("/list")
    public R<List> getList(HttpSession session) {
        return R.success(shoppingCartService.getShoppingCartList(session));
    }

    /**
     * 向购物车中添加商品
     *
     * @param shoppingCart
     * @param session
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> addMeal(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        return R.success(shoppingCartService.addMeal(shoppingCart, session));
    }

    /**
     * 修改购物车中商品
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> updateShoppingCart(@RequestBody ShoppingCart shoppingCart) {
        return R.success(shoppingCartService.updateShoppingCart(shoppingCart));
    }

    /**
     * 根据用户id清空购物车
     *
     * @param session
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clear(HttpSession session) {
        if (shoppingCartService.clean(session))
            return R.success("清空购物车成功");
        return R.error("清空购物车失败");
    }
}
