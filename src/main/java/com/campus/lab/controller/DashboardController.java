package com.campus.lab.controller;

import com.campus.lab.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 管理员数据看板
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final StatsService statsService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", statsService.overview());
        return "admin/dashboard";
    }
}
