package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<IPage> getPage(Integer page, Integer pageSize, String name) {
        return R.success(setmealService.getPage(page, pageSize, name));
    }

    /**
     * 根据id查询套餐详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getOne(@PathVariable Long id) {
        return R.success(setmealService.searchById(id));
    }

    /**
     * 批量起/停售
     *
     * @param ids
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, Long[] ids) {
        setmealService.updateStatus(ids, status);
        return R.success("套餐状态修改成功");
    }

    /**
     * 修改套餐详情
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> updateCombo(@RequestBody SetmealDto setmealDto) {
        setmealService.updateCombo(setmealDto);
        return R.success("套餐修改成功");
    }

    /**
     * 添加套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> saveCombo(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());
        setmealService.saveCombo(setmealDto);
        return R.success("套餐添加成功");
    }

    /**
     * (批量)删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        return setmealService.deleteIds(ids);
    }

    /**
     * 根据套餐分类id获取套餐列表
     *
     * @param categoryId
     * @param status
     * @return
     */
    @GetMapping("/list")
    public R<List> getList(Long categoryId, int status) {
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Setmeal::getStatus, status);
        lqw.eq(Setmeal::getCategoryId, categoryId);
        return R.success(setmealService.list(lqw));
    }

    /**
     * 根据套餐id获取菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List> getDishes(@PathVariable Long id) {
        if (setmealService.dishes(id).isEmpty())
            return R.error("套餐停售");
        return R.success(setmealService.dishes(id));
    }

}
