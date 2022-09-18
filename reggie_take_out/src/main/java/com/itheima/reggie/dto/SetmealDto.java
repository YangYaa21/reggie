package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {
    //DTO：Data Transfer Object,数据传输对象，用于展示层与服务层之间的数据传输
    private List<SetmealDish> setmealDishes;
    private String categoryName;
}
