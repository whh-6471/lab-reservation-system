package com.campus.lab.controller;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.LoginUser;
import com.campus.lab.common.Role;
import com.campus.lab.form.LoginForm;
import com.campus.lab.form.RegisterForm;
import com.campus.lab.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 登录、注册、退出
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        model.addAttribute("logoutNotice", logout != null);
        model.addAttribute("registeredNotice", registered != null);
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form, BindingResult bindingResult,
                        HttpSession session, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }
        try {
            LoginUser loginUser = authService.login(form);
            session.setAttribute("loginUser", loginUser);
            return "redirect:" + homeOf(loginUser.getRole());
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form, BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(form);
            return "redirect:/login?registered";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    private String homeOf(String role) {
        return switch (role) {
            case Role.TEACHER -> "/teacher/audit";
            case Role.ADMIN -> "/admin/dashboard";
            default -> "/student/home";
        };
    }
}
