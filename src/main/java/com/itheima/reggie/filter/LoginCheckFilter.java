package com.itheima.reggie.filter;
import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*检测用户是否登录
* filterName = "loginCheckFilter",urlPatterns = "/*"  拦截的路径这里是拦截所有
* */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter
{
    //路径匹配器，支持通配符 工具类
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();// /backend/index.html
        log.info("拦截到请求：{}",request.getRequestURI());
//       filterChain.doFilter(request,response);// 放行
//        2.定义不需要处理的路径
        String [] urls = new String[]
                {
                        "/employee/login",
                        "/employee/logout",
                        "/backend/**",
                        "/front/**",
                        "/user/sendMsg",  // 移动端发送短信
                        "/user/login"   //   移动段登录
                };
//        3.判断本次是否需要处理
        boolean check = check(urls,requestURI);
//        4.如果不需要直接放行
        if(check)
        {
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);// 放行
            return;
        }
//        判断是否登录，如果已经登录了就放行
        if(request.getSession().getAttribute("employee")!= null)
        {
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId); //设置登录的id  也就是线程的局部变量
            filterChain.doFilter(request,response);// 放行
            return;
        }
        //4-2、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null)
        {
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }
        log.info("用户未登录");
        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI)
    {
        for (String url : urls)
        {
//            match是比配的方法成功后返回boolean 形 true 就返回true  其他就false
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
