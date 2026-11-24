package com.campus.lab.controller;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.PageUtils;
import com.campus.lab.entity.Reservation;
import com.campus.lab.form.BatchCancelForm;
import com.campus.lab.service.LabService;
import com.campus.lab.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * 教师 / 管理员：全部预约管理，管理员可强制取消与批量取消
 */
@Controller
@RequiredArgsConstructor
public class ReservationManageController {

    private final ReservationService reservationService;
    private final LabService labService;

    /** 教师查看全部预约 */
    @GetMapping("/teacher/reservations")
    public String teacherList(@RequestParam(required = false) String status,
                              @RequestParam(required = false) Long labId,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String date,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        renderList(model, status, labId, keyword, date, page);
        model.addAttribute("manageRole", "TEACHER");
        return "reservations/all";
    }

    /** 管理员查看全部预约 */
    @GetMapping("/admin/reservations")
    public String adminList(@RequestParam(required = false) String status,
                            @RequestParam(required = false) Long labId,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String date,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        renderList(model, status, labId, keyword, date, page);
        model.addAttribute("manageRole", "ADMIN");
        return "reservations/all";
    }

    /** 管理员强制取消单条预约 */
    @PostMapping("/admin/reservations/{id}/force-cancel")
    public String forceCancel(@PathVariable Long id,
                              @RequestParam(required = false) String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            reservationService.forceCancel(id, reason);
            redirectAttributes.addFlashAttribute("msg", "预约已强制取消");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    /** 管理员批量强制取消 */
    @PostMapping("/admin/reservations/batch-cancel")
    public String batchCancel(@ModelAttribute BatchCancelForm form,
                              RedirectAttributes redirectAttributes) {
        try {
            int count = reservationService.batchCancel(form.getIds(), form.getReason());
            redirectAttributes.addFlashAttribute("msg", "已批量取消 " + count + " 条预约");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reservations";
    }

    private void renderList(Model model, String status, Long labId, String keyword, String date, int page) {
        Page<Reservation> data = reservationService.allPage(status, labId, keyword, parseDate(date),
                Math.max(page, 0));
        model.addAttribute("reservations", data.getContent());
        model.addAttribute("pageData", data);
        model.addAttribute("pageNumbers", PageUtils.numbers(data));
        model.addAttribute("labs", labService.listAll());
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("labId", labId);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("date", date == null ? "" : date);
        model.addAttribute("page", Math.max(page, 0));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
