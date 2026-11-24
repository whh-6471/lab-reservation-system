package com.campus.lab.controller;

import com.campus.lab.common.BusinessException;
import com.campus.lab.common.ClassroomStatus;
import com.campus.lab.entity.Classroom;
import com.campus.lab.form.ClassroomForm;
import com.campus.lab.service.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 空教室查询：全体用户查询 / 管理员维护教室状态
 */
@Controller
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    /** 空教室查询页，学生、教师、管理员均可访问 */
    @GetMapping("/classrooms")
    public String list(@RequestParam(required = false) Integer floorNo,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        render(model, floorNo, status, keyword);
        model.addAttribute("adminMode", false);
        return "classroom/list";
    }

    /** 管理员教室维护页 */
    @GetMapping("/admin/classrooms")
    public String manage(@RequestParam(required = false) Integer floorNo,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long edit,
                         Model model) {
        render(model, floorNo, status, keyword);
        model.addAttribute("adminMode", true);
        if (!model.containsAttribute("classroomForm")) {
            ClassroomForm form = edit == null
                    ? new ClassroomForm()
                    : classroomService.toForm(classroomService.get(edit));
            if (edit == null) {
                form.setStatus(ClassroomStatus.FREE);
                form.setMediaType("多媒体教室");
            }
            model.addAttribute("classroomForm", form);
        }
        model.addAttribute("editId", edit);
        return "classroom/list";
    }

    @PostMapping("/admin/classrooms/save")
    public String save(@Valid @ModelAttribute("classroomForm") ClassroomForm form, BindingResult bindingResult,
                       Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareAdmin(model);
            return "classroom/list";
        }
        try {
            classroomService.save(form);
            redirectAttributes.addFlashAttribute("msg",
                    form.getId() == null ? "教室已添加" : "教室信息已更新");
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            prepareAdmin(model);
            return "classroom/list";
        }
        return "redirect:/admin/classrooms";
    }

    @PostMapping("/admin/classrooms/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        try {
            classroomService.changeStatus(id, status);
            redirectAttributes.addFlashAttribute("msg", "教室状态已更新");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/classrooms";
    }

    @PostMapping("/admin/classrooms/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            classroomService.delete(id);
            redirectAttributes.addFlashAttribute("msg", "教室已删除");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/classrooms";
    }

    private void render(Model model, Integer floorNo, String status, String keyword) {
        List<Classroom> classrooms = classroomService.list(floorNo, status, keyword);
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("stats", classroomService.stats());
        model.addAttribute("floors", classroomService.floors());
        model.addAttribute("mediaTypes", classroomService.mediaTypes());
        model.addAttribute("floorNo", floorNo);
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("freeCount", classrooms.stream()
                .filter(c -> ClassroomStatus.FREE.equals(c.getStatus())).count());
    }

    private void prepareAdmin(Model model) {
        render(model, null, null, null);
        model.addAttribute("adminMode", true);
        if (!model.containsAttribute("classroomForm")) {
            ClassroomForm form = new ClassroomForm();
            form.setStatus(ClassroomStatus.FREE);
            form.setMediaType("多媒体教室");
            model.addAttribute("classroomForm", form);
        }
    }
}
