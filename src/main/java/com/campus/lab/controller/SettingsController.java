package com.campus.lab.controller;

import com.campus.lab.form.ConfigForm;
import com.campus.lab.service.SystemConfigService;
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
 * 管理员：预约规则配置
 */
@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/admin/settings")
    public String settings(Model model) {
        model.addAttribute("config", systemConfigService.get());
        if (!model.containsAttribute("configForm")) {
            model.addAttribute("configForm", systemConfigService.toForm(systemConfigService.get()));
        }
        return "admin/settings";
    }

    @PostMapping("/admin/settings")
    public String save(@Valid @ModelAttribute("configForm") ConfigForm form, BindingResult bindingResult,
                       Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("config", systemConfigService.get());
            return "admin/settings";
        }
        systemConfigService.save(form);
        redirectAttributes.addFlashAttribute("msg", "预约规则已保存");
        return "redirect:/admin/settings";
    }
}
