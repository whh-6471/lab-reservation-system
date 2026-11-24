package com.campus.lab.controller;

import com.campus.lab.common.LoginUser;
import com.campus.lab.common.Role;
import com.campus.lab.service.AnnouncementService;
import com.campus.lab.service.LabService;
import com.campus.lab.service.SystemConfigService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页与学生首页
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final LabService labService;
    private final AnnouncementService announcementService;
    private final SystemConfigService systemConfigService;

    @GetMapping("/")
    public String index(HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        return switch (loginUser.getRole()) {
            case Role.TEACHER -> "redirect:/teacher/audit";
            case Role.ADMIN -> "redirect:/admin/dashboard";
            default -> "redirect:/student/home";
        };
    }

    @GetMapping("/student/home")
    public String studentHome(Model model) {
        model.addAttribute("labs", labService.listAll());
        model.addAttribute("announcements", announcementService.latest());
        model.addAttribute("config", systemConfigService.get());
        return "student/home";
    }
}
