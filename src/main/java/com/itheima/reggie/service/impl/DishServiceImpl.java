package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Override
    public void updateWithFlavor(DishDto dishDto)
    {
//        更新dish表的信息
        this.updateById(dishDto);
//        清理当前菜品对应的口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
//        提交数据对dishflavor 进行insert 操做
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) ->
        {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

    }

    @Autowired
   private DishFlavorService dishFlavorService;

    // 根据id 查询菜品
    @Override
    public DishDto getByIdWithFlavor(Long id) {
//        先查询菜品
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
//        拷贝对象
        BeanUtils.copyProperties(dish,dishDto);
//        后查询口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    //新曾菜品和对应的口味
    @Override
    public void saveWithFlavor(DishDto dishDto)
    {
        this.save(dishDto);//保存菜品 dish 表

//        菜品的id  dishId
        Long dishId = dishDto.getId();
//        菜品口味
        List<DishFlavor> flavors =dishDto.getFlavors();
        //遍历这个list集合 重新赋值
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
//        保存口味 dishflavor
        dishFlavorService.saveBatch(flavors);

    }
}
