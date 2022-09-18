package com.itheima.reggie.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Employee;

public interface EmployeeService extends IService<Employee> {
    IPage<Employee> selectList(int currentPage, int pageSize, String name);
}
