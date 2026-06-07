package fun.xingji.wxshop.web;

import fun.xingji.wxshop.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理端登录拦截器
 * 校验 Session 中是否存在 adminId，未登录则抛出 ApiException
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    /**
     * 请求前置处理：检查管理员 Session
     * Session 存储在 Redis 中（通过 spring-session-data-redis）
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return true=放行，抛出异常=拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Object adminId = request.getSession().getAttribute("adminId");
        if (adminId == null) {
            throw new ApiException("管理员未登录");
        }
        return true;
    }
}
