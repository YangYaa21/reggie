package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressBook {
    private Long id;
    private Long userId;
    private String consignee;
    private Short sex;
    private String phone;
    private String province_code;
    private String province_name;
    private String city_code;
    private String city_name;
    private String district_code;
    private String district_name;
    private String detail;
    private String label;
    private Short isDefault;
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
