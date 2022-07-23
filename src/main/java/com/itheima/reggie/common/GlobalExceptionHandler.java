package com.itheima.reggie.common;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler
{

    /**
     * 异常处理方法 @ExceptionHandler 里面放具体的异常，
     * 看控制台把它贴进去
     * SQLIntegrityConstraintViolationException.class
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex)
    {
//        通过异常对象ex 获取异常信息
        log.error(ex.getMessage());

        if(ex.getMessage().contains("Duplicate entry"))
        {  // 判断包含关键字Duplicate entry 通过空格进行分隔
            String[] split = ex.getMessage().split(" "); //通过空格进行分隔
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    //根据id删除分类
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex)
    {
//        通过异常对象ex 获取异常信息
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }
}
