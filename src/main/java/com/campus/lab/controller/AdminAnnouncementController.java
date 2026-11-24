package com.campus.lab.controller;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.LoginUser;
import com.campus.lab.common.PageUtils;
import com.campus.lab.entity.Announcement;
import com.campus.lab.form.AnnouncementForm;
import com.campus.lab.service.AnnouncementService;
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
 * 管理员：系统公告维护
 */
@Controller
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/admin/announcements")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) Long edit,
                       Model model) {
        prepareList(model, page);
        if (!model.containsAttribute("announcementForm")) {
            AnnouncementForm form = new AnnouncementForm();
            if (edit != null) {
                Announcement announcement = announcementService.get(edit);
                form.setId(announcement.getId());
                form.setTitle(announcement.getTitle());
                form.setContent(announcement.getContent());
            }
            model.addAttribute("announcementForm", form);
        }
        model.addAttribute("editId", edit);
        return "admin/announcements";
    }

    @PostMapping("/admin/announcements/save")
    public String save(@Valid @ModelAttribute("announcementForm") AnnouncementForm form,
                       BindingResult bindingResult, HttpSession session,
                       Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareList(model, 0);
            return "admin/announcements";
        }
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        try {
            announcementService.save(form, loginUser.getRealName());
            redirectAttributes.addFlashAttribute("msg",
                    form.getId() == null ? "公告已发布" : "公告已更新");
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareList(model, 0);
            return "admin/announcements";
        }
        return "redirect:/admin/announcements";
    }

    @PostMapping("/admin/announcements/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            announcementService.delete(id);
            redirectAttributes.addFlashAttribute("msg", "公告已删除");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/announcements";
    }

    private void prepareList(Model model, int page) {
        Page<Announcement> data = announcementService.page(Math.max(page, 0));
        model.addAttribute("announcements", data.getContent());
        model.addAttribute("pageData", data);
        model.addAttribute("pageNumbers", PageUtils.numbers(data));
        model.addAttribute("page", Math.max(page, 0));
    }
}
