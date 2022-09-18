package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Date;

public interface OrdersService extends IService<Orders> {
    boolean saveOrder(Orders order, HttpSession session);

    Page getPage(int page, int pageSize, HttpSession session);

    Page getAllOrders(int page, int pageSize, String number, String beginTime, String endTime);
}
