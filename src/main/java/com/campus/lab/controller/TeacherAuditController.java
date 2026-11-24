package com.campus.lab.controller;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.LoginUser;
import com.campus.lab.common.PageUtils;
import com.campus.lab.common.ReservationStatus;
import com.campus.lab.entity.Reservation;
import com.campus.lab.form.AuditForm;
import com.campus.lab.service.LabService;
import com.campus.lab.service.ReservationService;
import jakarta.servlet.http.HttpSession;
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
 * 教师端：预约审核与审核记录查询
 */
@Controller
@RequiredArgsConstructor
public class TeacherAuditController {

    private final ReservationService reservationService;
    private final LabService labService;

    @GetMapping("/teacher/audit")
    public String auditPage(@RequestParam(defaultValue = "pending") String tab,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Long labId,
                            @RequestParam(required = false) String date,
                            @RequestParam(defaultValue = "0") int page,
                            HttpSession session, Model model) {
        LoginUser teacher = (LoginUser) session.getAttribute("loginUser");
        LocalDate dateFilter = parseDate(date);
        Page<Reservation> data;
        if ("audited".equals(tab)) {
            data = reservationService.auditedPage(teacher, null, labId, keyword, dateFilter, Math.max(page, 0));
        } else {
            data = reservationService.allPage(ReservationStatus.PENDING, labId, keyword, dateFilter,
                    Math.max(page, 0));
        }
        model.addAttribute("tab", "audited".equals(tab) ? "audited" : "pending");
        model.addAttribute("reservations", data.getContent());
        model.addAttribute("pageData", data);
        model.addAttribute("pageNumbers", PageUtils.numbers(data));
        model.addAttribute("labs", labService.listAll());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("labId", labId);
        model.addAttribute("date", date == null ? "" : date);
        model.addAttribute("page", Math.max(page, 0));
        return "teacher/audit";
    }

    @PostMapping("/teacher/audit/{id}")
    public String audit(@PathVariable Long id,
                        @ModelAttribute AuditForm auditForm,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        LoginUser teacher = (LoginUser) session.getAttribute("loginUser");
        try {
            reservationService.audit(id, teacher, auditForm);
            redirectAttributes.addFlashAttribute("msg",
                    "APPROVE".equals(auditForm.getAction()) ? "已通过该预约" : "已驳回该预约");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/audit";
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
