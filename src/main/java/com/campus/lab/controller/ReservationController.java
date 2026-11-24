package com.campus.lab.controller;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.LoginUser;
import com.campus.lab.common.PageUtils;
import com.campus.lab.entity.Reservation;
import com.campus.lab.form.ReservationForm;
import com.campus.lab.service.LabService;
import com.campus.lab.service.ReservationService;
import com.campus.lab.service.SystemConfigService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约提交、我的预约、预约详情与取消
 */
@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final LabService labService;
    private final SystemConfigService systemConfigService;

    /** 预约提交页面 */
    @GetMapping("/reservations/new")
    public String createPage(@RequestParam(required = false) Long labId,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             Model model) {
        ReservationForm form = new ReservationForm();
        form.setLabId(labId);
        form.setReservationDate(date == null ? LocalDate.now().plusDays(1) : date);
        form.setParticipantCount(1);
        model.addAttribute("reservationForm", form);
        prepareFormModel(model, form.getLabId(), form.getReservationDate());
        return "student/reservation-form";
    }

    /** 提交预约 */
    @PostMapping("/reservations")
    public String create(@Valid @ModelAttribute("reservationForm") ReservationForm form, BindingResult bindingResult,
                         HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, form.getLabId(), form.getReservationDate());
            return "student/reservation-form";
        }
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        try {
            reservationService.create(form, loginUser);
            redirectAttributes.addFlashAttribute("msg", "预约提交成功，请等待教师审核");
            return "redirect:/reservations/my";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareFormModel(model, form.getLabId(), form.getReservationDate());
            return "student/reservation-form";
        }
    }

    /** 我的预约记录 */
    @GetMapping("/reservations/my")
    public String my(@RequestParam(required = false) String status,
                     @RequestParam(required = false) Long labId,
                     @RequestParam(required = false) String keyword,
                     @RequestParam(required = false) String date,
                     @RequestParam(defaultValue = "0") int page,
                     HttpSession session, Model model) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        LocalDate dateFilter = parseDate(date);
        Page<Reservation> data = reservationService.myPage(loginUser, status, labId, keyword, dateFilter,
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
        return "student/my-reservations";
    }

    /** 预约详情 */
    @GetMapping("/reservations/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        Reservation reservation = reservationService.get(id);
        if (!loginUser.isAdmin() && !loginUser.isTeacher()
                && !reservation.getApplicantId().equals(loginUser.getId())) {
            throw new BusinessException("无权查看该预约记录");
        }
        reservationService.fillLabNames(List.of(reservation));
        model.addAttribute("reservation", reservation);
        model.addAttribute("lab", labService.get(reservation.getLabId()));
        model.addAttribute("records", reservationService.auditRecords(id));
        model.addAttribute("backUrl", backUrlOf(loginUser));
        return "reservation/detail";
    }

    /** 取消预约 */
    @PostMapping("/reservations/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @RequestParam(required = false) String reason,
                         HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        try {
            reservationService.cancel(id, loginUser, reason);
            redirectAttributes.addFlashAttribute("msg", "预约已取消");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations/my";
    }

    private void prepareFormModel(Model model, Long labId, LocalDate date) {
        model.addAttribute("labs", labService.listAvailable());
        model.addAttribute("config", systemConfigService.get());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("maxDate", LocalDate.now().plusDays(systemConfigService.get().getAdvanceDays()));
        if (labId != null) {
            model.addAttribute("schedule", labService.schedule(labId, date == null ? LocalDate.now() : date));
        }
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

    private String backUrlOf(LoginUser loginUser) {
        if (loginUser.isAdmin()) {
            return "/admin/reservations";
        }
        if (loginUser.isTeacher()) {
            return "/teacher/reservations";
        }
        return "/reservations/my";
    }
}
