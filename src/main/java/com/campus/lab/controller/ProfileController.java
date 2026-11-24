package com.campus.lab.controller;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.LoginUser;
import com.campus.lab.entity.User;
import com.campus.lab.form.PasswordForm;
import com.campus.lab.form.ProfileForm;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 个人中心：资料修改与密码修改
 */
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final AuthService authService;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        User user = authService.get(loginUser.getId());
        model.addAttribute("user", user);
        if (!model.containsAttribute("profileForm")) {
            ProfileForm profileForm = new ProfileForm();
            profileForm.setRealName(user.getRealName());
            profileForm.setStudentNo(user.getStudentNo());
            profileForm.setDepartment(user.getDepartment());
            profileForm.setPhone(user.getPhone());
            profileForm.setEmail(user.getEmail());
            model.addAttribute("profileForm", profileForm);
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new PasswordForm());
        }
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm form,
                                BindingResult bindingResult, HttpSession session,
                                Model model, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", authService.get(loginUser.getId()));
            model.addAttribute("passwordForm", new PasswordForm());
            return "profile";
        }
        try {
            authService.updateProfile(loginUser.getId(), form);
            loginUser.setRealName(form.getRealName());
            loginUser.setDepartment(form.getDepartment());
            redirectAttributes.addFlashAttribute("msg", "个人资料已更新");
        } catch (BusinessException e) {
            model.addAttribute("user", authService.get(loginUser.getId()));
            model.addAttribute("error", e.getMessage());
            model.addAttribute("passwordForm", new PasswordForm());
            return "profile";
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordForm form,
                                 BindingResult bindingResult, HttpSession session,
                                 Model model, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", authService.get(loginUser.getId()));
            model.addAttribute("profileForm", buildProfileForm(loginUser.getId()));
            return "profile";
        }
        try {
            authService.changePassword(loginUser.getId(), form);
            redirectAttributes.addFlashAttribute("msg", "密码修改成功，请重新登录");
            session.invalidate();
            return "redirect:/login";
        } catch (BusinessException e) {
            model.addAttribute("user", authService.get(loginUser.getId()));
            model.addAttribute("profileForm", buildProfileForm(loginUser.getId()));
            model.addAttribute("error", e.getMessage());
            return "profile";
        }
    }

    private ProfileForm buildProfileForm(Long userId) {
        User user = authService.get(userId);
        ProfileForm form = new ProfileForm();
        form.setRealName(user.getRealName());
        form.setStudentNo(user.getStudentNo());
        form.setDepartment(user.getDepartment());
        form.setPhone(user.getPhone());
        form.setEmail(user.getEmail());
        return form;
    }
}
