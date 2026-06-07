package fun.xingji.wxshop.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpSession;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Session Token 过滤器
 * 微信小程序无法可靠传递 Cookie，改为通过 X-Token 请求头传递 JSESSIONID。
 * 本过滤器读取 X-Token 头，从 Tomcat Manager 中查找对应的 Session，
 * 并包装 HttpServletRequest 使其返回该 Session。
 * 优先级设为最高，确保在 LoginInterceptor 之前执行。
 */
@Component
@Order(Integer.MIN_VALUE)
public class SessionTokenFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SessionTokenFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("X-Token");

        if (token != null && !token.isEmpty()) {
            try {
                // 从 Tomcat 的 Manager 中查找 Session
                Manager manager = (Manager) req.getServletContext()
                        .getAttribute("org.apache.catalina.Manager");
                if (manager != null) {
                    Session catalinaSession = manager.findSession(token);
                    if (catalinaSession != null && catalinaSession.isValid()) {
                        HttpSession httpSession = catalinaSession.getSession();
                        // 包装 request，重写 getSession 方法
                        req = new HttpServletRequestWrapper(req) {
                            @Override
                            public HttpSession getSession(boolean create) {
                                return httpSession;
                            }

                            @Override
                            public HttpSession getSession() {
                                return httpSession;
                            }

                            @Override
                            public String getRequestedSessionId() {
                                return token;
                            }

                            @Override
                            public boolean isRequestedSessionIdValid() {
                                return true;
                            }
                        };
                    } else {
                        log.debug("SessionTokenFilter: 无效或过期的 token: {}", token.substring(0, Math.min(8, token.length())));
                    }
                }
            } catch (Exception e) {
                log.warn("SessionTokenFilter: 处理 X-Token 时出错", e);
            }
        }

        chain.doFilter(req, response);
    }
}
