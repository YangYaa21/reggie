package com.itheima.reggie.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    SetmealDto searchById(Long id);

    void updateStatus(Long[] ids, Integer status);

    void updateCombo(SetmealDto setmealDto);

    R<String> deleteIds(List<Long> ids);

    IPage getPage(Integer page, Integer pageSize, String name);

    void saveCombo(SetmealDto setmealDto);

    List dishes(Long id);
}
