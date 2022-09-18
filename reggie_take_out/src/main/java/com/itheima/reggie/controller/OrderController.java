package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrdersService ordersService;

    /**
     * 分页查询订单
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> getPage(int page, int pageSize, HttpSession session) {
        return R.success(ordersService.getPage(page, pageSize, session));
    }

    /**
     * 提交订单
     *
     * @param order
     * @param session
     * @return
     */
    @PostMapping("/submit")
    @Transactional
    public R<String> submitOrder(@RequestBody Orders order, HttpSession session) {
        if (ordersService.saveOrder(order, session))
            return R.success("提交订单成功");
        return R.error("订单提交失败");
    }

    /**
     * 获取所有订单
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> getAllOrders(@RequestParam int page, int pageSize, String number, String beginTime, String endTime) {
        return R.success(ordersService.getAllOrders(page, pageSize, number, beginTime, endTime));
    }
}
