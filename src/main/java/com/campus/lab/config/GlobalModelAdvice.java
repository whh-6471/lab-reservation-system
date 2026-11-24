package com.campus.lab.config;

import com.campus.lab.common.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 向所有页面注入登录用户与当前路径，供导航栏渲染
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpSession session, HttpServletRequest request) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof LoginUser user) {
            model.addAttribute("loginUser", user);
        }
        model.addAttribute("currentUri", request.getRequestURI());
    }
}
