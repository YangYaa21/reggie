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

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1、获取本次请求的URI
        String uri = request.getRequestURI();
        //2、定义不需要拦截的路径
        String urls[] = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/login",
                "/user/sendMsg"
        };
        //3、路径比对
        boolean check = check(urls, uri);
        //4、如果不需要处理，直接放行
        if (check) {
            filterChain.doFilter(request, response);
//            log.info("不需要处理，放行");
            return;
        }

        //5-1、判断登录状态，如果已登录，放行，未登录则通过输出流返回error
        if (request.getSession().getAttribute("employee") != null) {
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request, response);
            log.info("已登录, employeeId是{}", request.getSession().getAttribute("employee"));
            return;
        }

        //5-2、判断登录状态，如果已登录，放行，未登录则通过输出流返回error
        if (request.getSession().getAttribute("user") != null) {
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request, response);
            log.info("已登录, userId是{}", request.getSession().getAttribute("user"));
            return;
        }

        response.getWriter().write(JSON.toJSONString(R.error("NOLOGGING")));
        log.info("没登录");
        return;
    }

    boolean check(String[] urls, String uri) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, uri);
            if (match)
                return true;
        }
        return false;
    }
}
