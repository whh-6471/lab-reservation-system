package com.campus.lab.controller;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.PageUtils;
import com.campus.lab.entity.Lab;
import com.campus.lab.form.LabForm;
import com.campus.lab.service.EquipmentService;
import com.campus.lab.service.LabService;
import com.campus.lab.service.SystemConfigService;
import com.campus.lab.vo.ScheduleVO;
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

/**
 * 实验室详情（含时段查看）与管理员实验室维护
 */
@Controller
@RequiredArgsConstructor
public class LabController {

    private final LabService labService;
    private final EquipmentService equipmentService;
    private final SystemConfigService systemConfigService;

    /** 实验室详情：查看可预约时段与设备台账 */
    @GetMapping("/labs/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                         Model model) {
        ScheduleVO schedule = labService.schedule(id, date);
        model.addAttribute("schedule", schedule);
        model.addAttribute("lab", schedule.getLab());
        model.addAttribute("equipments", equipmentService.listForLab(id));
        model.addAttribute("config", systemConfigService.get());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("maxDate", LocalDate.now().plusDays(systemConfigService.get().getAdvanceDays()));
        return "student/lab-detail";
    }

    /** 管理员：实验室列表 */
    @GetMapping("/admin/labs")
    public String manage(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) Long edit,
                         Model model) {
        prepareList(model, keyword, status, page);
        if (!model.containsAttribute("labForm")) {
            LabForm form = edit == null ? new LabForm() : labService.toForm(labService.get(edit));
            model.addAttribute("labForm", form);
        }
        model.addAttribute("editId", edit);
        return "admin/labs";
    }

    /** 管理员：新增 / 编辑实验室 */
    @PostMapping("/admin/labs/save")
    public String save(@Valid @ModelAttribute("labForm") LabForm form, BindingResult bindingResult,
                       Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareList(model, null, null, 0);
            return "admin/labs";
        }
        try {
            labService.save(form);
            redirectAttributes.addFlashAttribute("msg",
                    form.getId() == null ? "实验室新增成功" : "实验室信息已更新");
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareList(model, null, null, 0);
            return "admin/labs";
        }
        return "redirect:/admin/labs";
    }

    /** 管理员：删除实验室 */
    @PostMapping("/admin/labs/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            labService.delete(id);
            redirectAttributes.addFlashAttribute("msg", "实验室已删除");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/labs";
    }

    private void prepareList(Model model, String keyword, String status, int page) {
        Page<Lab> data = labService.page(keyword, status, Math.max(page, 0));
        model.addAttribute("labs", data.getContent());
        model.addAttribute("pageData", data);
        model.addAttribute("pageNumbers", PageUtils.numbers(data));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("page", Math.max(page, 0));
    }
}
