package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>implements SetmealService
{
    @Autowired
    private SetmealDishService setmealDishService;

    // 删除套餐同时删除套餐和菜品的关系
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids)
    {
        //查询套餐状态，判断是否可以删除  select count(*) from setmeal where id in(1,2,3) end status = 1
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids); //id  在 ids 上
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);  // 框架的方法 IService 里面的方法
        if(count>0) // 上面查询的是  在售的
        {
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
    }

    // 新增套餐同时及保存套餐和菜品的关系
    @Override
    @Transactional   // 操作两张表需要加这个注解保持一致性   套餐和套餐菜品关系表
    public void saveWithDish(SetmealDto setmealDto)
    {
//        保存菜品
        this.save(setmealDto);
        //保存套餐和菜品的关系
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)->
        {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }
}
