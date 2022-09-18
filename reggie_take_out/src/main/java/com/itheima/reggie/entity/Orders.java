package com.itheima.reggie.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Orders {
    private Long id;
    private String number;
    private Integer status;
    private Long userId;
    private Long addressBookId;
    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private Integer payMethod;
    private Float amount;
    private String remark;
    private String phone;
    private String address;
    private String userName;
    private String consignee;
}
