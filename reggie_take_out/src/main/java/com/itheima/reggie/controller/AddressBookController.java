package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 获取所有地址信息
     *
     * @param session
     * @return
     */
    @GetMapping("/list")
    public R<List> addressList(HttpSession session) {
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId, session.getAttribute("user"));
        List<AddressBook> addressBookList = addressBookService.list(lqw);
        return R.success(addressBookList);
    }

    /**
     * 添加地址信息
     *
     * @param addressBook
     * @param session
     * @return
     */
    @PostMapping
    public R<String> addAddress(@RequestBody AddressBook addressBook, HttpSession session) {
        addressBook.setUserId((Long) session.getAttribute("user"));
        addressBookService.save(addressBook);
        return R.success("添加地址成功");
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook) {
        addressBookService.setDefault(addressBook);
        return R.success("默认地址设置成功");
    }

    /**
     * 获取默认地址
     *
     * @param session
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault(HttpSession session) {
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getIsDefault, 1);
        lqw.eq(AddressBook::getUserId, session.getAttribute("user"));
        AddressBook address = addressBookService.getOne(lqw);
        return R.success(address);
    }

    /**
     * 根据id获取地址信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> getOneAddress(@PathVariable Long id) {
        return R.success(addressBookService.getById(id));
    }

    /**
     * 修改地址信息
     *
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> updateAddress(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return R.success("修改地址成功");
    }

    /**
     * 删除地址信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        addressBookService.removeById(ids);
        return R.success("删除成功");
    }
}
