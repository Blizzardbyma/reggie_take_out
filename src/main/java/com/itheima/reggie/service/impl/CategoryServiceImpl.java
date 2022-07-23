package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService
{
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Override
// 根据id 删除分类，删除之前要进行判断
    public void remove(Long id)
    {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        添加查询条件，获取数据的id  和传进来的id 进行对比
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
//        查询关联的菜品数量
         int count1 = dishService.count(dishLambdaQueryWrapper);
//        是否关联菜品，有则抛出异常
        if(count1>0)
        {
            throw new CustomException("当前分类，已经关联菜品不能被删除");

        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        添加查询条件，获取数据的id  和传进来的id 进行对比
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
//        查询关联的套餐数量
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //        是否关联套餐，有则抛出异常
        if(count2>0)
        {
            throw new CustomException("当前分类，已经关联套餐不能被删除");
        }
//        正常执行
        super.removeById(id);
    }
}
