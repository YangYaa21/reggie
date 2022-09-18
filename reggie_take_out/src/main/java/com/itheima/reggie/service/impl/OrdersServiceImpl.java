package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private UserService userService;

    /**
     * 提交订单
     **/
    @Override
    public boolean saveOrder(Orders order, HttpSession session) {
        // order：{amount: 2202, remark: "", payMethod: 1, addressBookId: "1417414526093082626"}
        // 获取用户id，查购物车商品status, isDeleted,设置orders的userId, orderTime, checkoutTime
        // 设置订单状态status 1待付款，2待派送，3已派送，4已完成，5已取消
        // 提交订单，orders表：status置1，userId，/addressBookId，orderTime，checkoutTime，/payMethod，/amount
        // orderDetail表：orderId，dishId/setmealId，number，amount(商品单价)
        //保存到orders表：
        Long userId = (Long) session.getAttribute("user");
        order.setUserId(userId);
        order.setNumber("2022" + userId + LocalDateTime.now().hashCode());
        order.setStatus(1);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        //获取地址信息
        AddressBook addressBook = addressBookService.getById(order.getAddressBookId());
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());

        order.setUserName(userService.getById(userId).getName());

        this.save(order);

        //保存到orderDetail表：先获取购物车商品集合shoppingCartList
        List<ShoppingCart> shoppingCartList = shoppingCartService.getShoppingCartList(session);
        //创建orderDetailList，
        List<OrderDetail> orderDetailList = new ArrayList<>();
        //遍历shoppingCartList，给orderDetail赋值
        for (ShoppingCart shoppingCart :
                shoppingCartList) {

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getId());
            orderDetail.setName(shoppingCart.getName());
            orderDetail.setImage(shoppingCart.getImage());
            orderDetail.setDishId(shoppingCart.getDishId());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            orderDetail.setNumber(shoppingCart.getNumber());
            orderDetail.setAmount(shoppingCart.getAmount());

            orderDetailList.add(orderDetail);
        }
        orderDetailService.saveBatch(orderDetailList);
        //清空购物车
        shoppingCartService.clean(session);
        return true;
    }

    /**
     * 分页查询用户订单
     **/
    @Override
    public Page getPage(int page, int pageSize, HttpSession session) {
        // 先创建OrdersDto，再初始化一个Page<OrderDto>，通过ordersDtoList添加数据
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
        List<OrdersDto> ordersDtoList = new ArrayList<>();

        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getUserId, session.getAttribute("user"));
        List<Orders> orders = this.list(lqw);

        //向orderDto添加orders信息，以及orderDetailList
        for (int i = 0; i < orders.size(); i++) {
            OrdersDto orderDto = new OrdersDto();

            //添加order数据
            orderDto.setId(orders.get(i).getId());
            orderDto.setNumber(orders.get(i).getNumber());
            orderDto.setStatus(orders.get(i).getStatus());
            orderDto.setUserId(orders.get(i).getUserId());
            orderDto.setAddressBookId(orders.get(i).getAddressBookId());
            orderDto.setOrderTime(orders.get(i).getOrderTime());
            orderDto.setCheckoutTime(orders.get(i).getCheckoutTime());
            orderDto.setPayMethod(orders.get(i).getPayMethod());
            orderDto.setAmount(orders.get(i).getAmount());
            orderDto.setRemark(orders.get(i).getRemark());
            orderDto.setPhone(orders.get(i).getPhone());
            orderDto.setAddress(orders.get(i).getAddress());
            orderDto.setUserName(orders.get(i).getUserName());
            orderDto.setConsignee(orders.get(i).getConsignee());
            //添加orderDetail数据
            LambdaQueryWrapper<OrderDetail> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(OrderDetail::getOrderId, orderDto.getId());
            orderDto.setOrderDetails(orderDetailService.list(lqw1));

            ordersDtoList.add(orderDto);
        }

        ordersDtoPage.setRecords(ordersDtoList);
        return ordersDtoPage;
    }

    @Override
    public Page getAllOrders(int page, int pageSize, String number, String beginTime, String endTime) {

        Page<OrdersDto> pages = new Page<>(page, pageSize);
        List<OrdersDto> ordersDtoList = new ArrayList<>();

        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();

        if (number != null)
            lqw.like(Orders::getNumber, number);
        if (beginTime != null && endTime != null) {
            LocalDateTime begin = LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            lqw.between(Orders::getOrderTime, begin, end);
        }
        List<Orders> orders = this.list(lqw);

        //向orderDto添加orders信息，以及orderDetailList
        for (int i = 0; i < orders.size(); i++) {
            OrdersDto orderDto = new OrdersDto();
            //添加order数据
            orderDto.setId(orders.get(i).getId());
            orderDto.setNumber(orders.get(i).getNumber());
            orderDto.setStatus(orders.get(i).getStatus());
            orderDto.setUserId(orders.get(i).getUserId());
            orderDto.setAddressBookId(orders.get(i).getAddressBookId());
            orderDto.setOrderTime(orders.get(i).getOrderTime());
            orderDto.setCheckoutTime(orders.get(i).getCheckoutTime());
            orderDto.setPayMethod(orders.get(i).getPayMethod());
            orderDto.setAmount(orders.get(i).getAmount());
            orderDto.setRemark(orders.get(i).getRemark());
            orderDto.setPhone(orders.get(i).getPhone());
            orderDto.setAddress(orders.get(i).getAddress());
            orderDto.setUserName(orders.get(i).getUserName());
            orderDto.setConsignee(orders.get(i).getConsignee());
            //添加orderDetail数据
            LambdaQueryWrapper<OrderDetail> lqw1 = new LambdaQueryWrapper<>();
            lqw1.eq(OrderDetail::getOrderId, orderDto.getId());
            orderDto.setOrderDetails(orderDetailService.list(lqw1));

            ordersDtoList.add(orderDto);
        }

        pages.setRecords(ordersDtoList);

        return pages;
    }
}
