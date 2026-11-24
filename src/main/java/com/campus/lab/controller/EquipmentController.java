package com.campus.lab.controller;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.PageUtils;
import com.campus.lab.entity.Equipment;
import com.campus.lab.form.EquipmentForm;
import com.campus.lab.service.EquipmentService;
import com.campus.lab.service.LabService;
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
 * 设备台账：全体用户查询 / 管理员维护
 */
@Controller
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final LabService labService;

    /** 学生、教师查看设备台账（只读） */
    @GetMapping("/equipment")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long labId,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        renderList(model, keyword, labId, category, status, page);
        model.addAttribute("adminMode", false);
        return "equipment/list";
    }

    /** 管理员设备台账管理页 */
    @GetMapping("/admin/equipment")
    public String manage(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long labId,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) Long edit,
                         Model model) {
        renderList(model, keyword, labId, category, status, page);
        model.addAttribute("adminMode", true);
        if (!model.containsAttribute("equipmentForm")) {
            EquipmentForm form = edit == null
                    ? new EquipmentForm()
                    : equipmentService.toForm(equipmentService.get(edit));
            model.addAttribute("equipmentForm", form);
        }
        model.addAttribute("editId", edit);
        return "equipment/list";
    }

    @PostMapping("/admin/equipment/save")
    public String save(@Valid @ModelAttribute("equipmentForm") EquipmentForm form, BindingResult bindingResult,
                       Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareAdmin(model);
            return "equipment/list";
        }
        try {
            equipmentService.save(form);
            redirectAttributes.addFlashAttribute("msg",
                    form.getId() == null ? "设备已录入台账" : "设备信息已更新");
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareAdmin(model);
            return "equipment/list";
        }
        return "redirect:/admin/equipment";
    }

    @PostMapping("/admin/equipment/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipmentService.delete(id);
            redirectAttributes.addFlashAttribute("msg", "设备已从台账中删除");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/equipment";
    }

    /** 设备状态更新：维修登记 / 报废登记 / 恢复正常 */
    @PostMapping("/admin/equipment/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        try {
            equipmentService.changeStatus(id, status);
            redirectAttributes.addFlashAttribute("msg", "设备状态已更新");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/equipment";
    }

    private void renderList(Model model, String keyword, Long labId, String category, String status, int page) {
        Page<Equipment> data = equipmentService.page(keyword, labId, category, status, Math.max(page, 0));
        model.addAttribute("equipments", data.getContent());
        model.addAttribute("pageData", data);
        model.addAttribute("pageNumbers", PageUtils.numbers(data));
        model.addAttribute("labs", labService.listAll());
        model.addAttribute("categories", equipmentService.categories());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("labId", labId);
        model.addAttribute("category", category == null ? "" : category);
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("page", Math.max(page, 0));
    }

    private void prepareAdmin(Model model) {
        renderList(model, null, null, null, null, 0);
        model.addAttribute("adminMode", true);
        if (!model.containsAttribute("equipmentForm")) {
            model.addAttribute("equipmentForm", new EquipmentForm());
        }
    }
}
