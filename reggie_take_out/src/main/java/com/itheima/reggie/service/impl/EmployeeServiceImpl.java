package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployeeService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public boolean save(Employee entity) {
        return employeeMapper.insert(entity) > 0;
    }

    @Override
    public IPage<Employee> selectList(int currentPage, int pageSize, String name) {
        IPage page = new Page(currentPage, pageSize);

        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<Employee>();
        lqw.like(Strings.isNotEmpty(name), Employee::getName, name);
//        lqw.like(Strings.isNotEmpty(queryBook.getType()), Book::getType, queryBook.getType());
        return employeeMapper.selectPage(page, lqw);
    }


}
