package com.itheima.reggie.entity;

import lombok.Data;

@Data
public class OrderDetail {
    private Long id;
    private String name;
    private String image;
    private Long orderId;
    private Long dishId;
    private Long setmealId;
    private String dishFlavor;
    private Integer number;
    private Float amount;
}
