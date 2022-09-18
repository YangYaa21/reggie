package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.mapper.AddressBookMapper;
import com.itheima.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    /**
     * 设置默认地址
     **/
    @Override
    public void setDefault(AddressBook addressBook) {
        AddressBook addressBook1 = this.getById(addressBook.getId());

        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getIsDefault, 1);
        lqw.eq(AddressBook::getUserId, addressBook1.getUserId());
        AddressBook address = this.getOne(lqw);
        if (address != null) {
            address.setIsDefault((short) 0);
            this.updateById(address);
        }

        addressBook.setIsDefault((short) 1);
        this.updateById(addressBook);
    }
}
