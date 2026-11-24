package com.campus.lab.controller;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.LoginUser;
import com.campus.lab.common.PageUtils;
import com.campus.lab.entity.User;
import com.campus.lab.form.UserForm;
import com.campus.lab.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 管理员：用户账号管理
 */
@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/admin/users")
    public String list(@RequestParam(required = false) String role,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) Long edit,
                       Model model) {
        prepareList(model, role, keyword, status, page);
        if (!model.containsAttribute("userForm")) {
            UserForm form = edit == null ? new UserForm() : userService.toForm(userService.get(edit));
            model.addAttribute("userForm", form);
        }
        model.addAttribute("editId", edit);
        return "admin/users";
    }

    @PostMapping("/admin/users/save")
    public String save(@Valid @ModelAttribute("userForm") UserForm form, BindingResult bindingResult,
                       Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareList(model, null, null, null, 0);
            return "admin/users";
        }
        try {
            if (form.getId() == null) {
                userService.create(form);
                redirectAttributes.addFlashAttribute("msg", "用户新增成功");
            } else {
                userService.update(form);
                redirectAttributes.addFlashAttribute("msg", "用户信息已更新");
            }
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareList(model, null, null, null, 0);
            return "admin/users";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/toggle")
    public String toggle(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        try {
            userService.toggleStatus(id, loginUser.getId());
            redirectAttributes.addFlashAttribute("msg", "账号状态已更新");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        try {
            userService.delete(id, loginUser.getId());
            redirectAttributes.addFlashAttribute("msg", "用户已删除");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    private void prepareList(Model model, String role, String keyword, Integer status, int page) {
        Page<User> data = userService.page(role, keyword, status, Math.max(page, 0));
        model.addAttribute("users", data.getContent());
        model.addAttribute("pageData", data);
        model.addAttribute("pageNumbers", PageUtils.numbers(data));
        model.addAttribute("role", role == null ? "" : role);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("status", status);
        model.addAttribute("page", Math.max(page, 0));
    }
}
