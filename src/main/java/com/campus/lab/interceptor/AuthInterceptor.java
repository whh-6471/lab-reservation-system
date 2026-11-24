package com.campus.lab.interceptor;

import com.campus.lab.common.LoginUser;
import com.campus.lab.common.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录与角色权限拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_KEY = "loginUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);
        LoginUser loginUser = session == null ? null : (LoginUser) session.getAttribute(SESSION_KEY);
        if (loginUser == null) {
            response.sendRedirect("/login");
            return false;
        }
        if (uri.startsWith("/admin") && !Role.ADMIN.equals(loginUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        if (uri.startsWith("/teacher") && !Role.TEACHER.equals(loginUser.getRole())
                && !Role.ADMIN.equals(loginUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        if (uri.startsWith("/student") && !Role.STUDENT.equals(loginUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
