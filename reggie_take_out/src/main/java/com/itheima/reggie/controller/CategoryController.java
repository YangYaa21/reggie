package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 查询所有分类
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<IPage> getPage(int page, int pageSize) {
        IPage<Category> pa = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.orderByAsc(Category::getSort);
        categoryService.page(pa, lqw);
        return R.success(pa);
    }

    /**
     * 获取分类列表(菜品和套餐)
     *
     * @param type
     * @return
     */
    @GetMapping("/list")
    public R<List> getList(int type) {
        if (type == 100) {
            //针对用户端按分类排序
            LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
            lqw.orderByAsc(Category::getType);
            return R.success(categoryService.list(lqw));
        }
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Category::getType, type);
        return R.success(categoryService.list(lqw));
    }

    /**
     * 根据分类id删除分类
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @Transactional
    public R<String> deleteById(Long ids) {
        //删除setmeal表中该分类对应套餐
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Setmeal::getCategoryId, ids);
        List<Setmeal> setmeals = setmealService.list(lqw);
        //获取套餐id存入setmealId集合
        List<Long> setmealIds = new ArrayList<>();
        for (Setmeal setmeal : setmeals) {
            setmealIds.add(setmeal.getId());
        }
        setmealService.deleteIds(setmealIds);

        if (categoryService.removeById(ids) == true)
            return R.success("删除成功");
        return R.error("删除失败");
    }

    /**
     * 修改分类信息
     *
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
//        log.info("要修改的菜品{}", category);
        if (categoryService.updateById(category))
            return R.success("修改成功");
        return R.error("修改失败");
    }

    @PostMapping
    public R<String> save(@RequestBody Category category) {
//        log.info("要增加的菜品{}", category);
        if (categoryService.save(category))
            return R.success("添加成功");
        return R.error("添加失败");
    }

}
