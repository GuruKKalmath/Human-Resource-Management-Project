package com.example.employee_management.controller;

import com.example.employee_management.entity.Department;
import com.example.employee_management.exception.ResourceNotFoundException;
import com.example.employee_management.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.findAllSorted());
        return "departments/list";
    }

    @GetMapping("/new")
    public String newDepartmentForm(Model model) {
        model.addAttribute("department", new Department());
        return "departments/add";
    }

    @PostMapping("/save")
    public String saveDepartment(
            @Valid @ModelAttribute("department") Department department,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "departments/add";
        }
        try {
            departmentService.createDepartment(department);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "departments/add";
        }
        return "redirect:/departments";
    }

    @GetMapping("/edit/{id}")
    public String editDepartmentForm(@PathVariable Long id, Model model) {
        model.addAttribute("department", departmentService.getDepartmentById(id));
        return "departments/edit";
    }

    @PostMapping("/update")
    public String updateDepartment(
            @Valid @ModelAttribute("department") Department department,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "departments/edit";
        }
        try {
            departmentService.updateDepartment(department.getId(), department);
        } catch (IllegalArgumentException | ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("department", department);
            return "departments/edit";
        }
        return "redirect:/departments";
    }

    @PostMapping("/delete")
    public String deleteDepartment(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteDepartment(id);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/departments";
    }
}
