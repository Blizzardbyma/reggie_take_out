package com.itheima.reggie.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController
{
    @Autowired
    private EmployeeService employeeService;
    @PostMapping("/login")
    // 用来获取 seesion     HttpServletRequest request
    public R<Employee> login(HttpServletRequest request,@RequestBody Employee employee)
    {
        String password = employee.getPassword(); //1、将页面提交的密码password进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());  //把密码转成数组md5加密
        //1.根据前端传过来的用户名userName  查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //2.如果没有查到返回失败
        if(emp==null)
        {
            return R.error("登录失败");
        }
        //3.如果登录成功进行密码比对
        if(!emp.getPassword().equals(password))
        {
            return R.error("登录失败");
        }
        //4.查看账号是否禁用了
        if(emp.getStatus()==0)
        {
            return R.error("登录失败,账号被禁用了");
        }
        //6. 将员工id  设置到seesion 并返回成功
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
    //  退出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request)
    {
      /* 清理Session中保存的当前登录员工的id*/
        request.getSession().removeAttribute("employee");
        return R.success("EXIT SUCCESS");
    }
    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());

        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

   /*     employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
*/
        employeeService.save(employee);

        return R.success("新增员工成功");
    }
    /**
     * 员工信息分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name)
    {
        log.info("page{},pageSize{},name{}",page,pageSize,name);
//        分页构造器
        Page pageInfo = new Page(page,pageSize);
//        条件构造器   如果name是空的 就不添加这个条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
//        添加排序条件
        queryWrapper.orderByDesc( Employee::getUpdateTime);
//        执行查询

        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);

    }


    /**
     * 根据id 修改status
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee)
    {
        log.info(employee.toString());
/*//        通过session 获取登录的人 id
         Long empId = (Long) request.getSession().getAttribute("employee");
//         设置当前更新时间  设置更新的用户id
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);*/
        //  通过service 调用更新数据库
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    @GetMapping("/{id}")    //从url取出id 传递给形参
    public R<Employee> getById(@PathVariable Long id)
    {
        log.info("根据员工id查信息");
        Employee employee = employeeService.getById(id);
        if(employee != null)
        {
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }


}
