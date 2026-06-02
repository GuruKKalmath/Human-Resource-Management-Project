package com.example.employee_management.controller;

import com.example.employee_management.dto.EmployeeDashboardData;
import com.example.employee_management.entity.Employee;
import com.example.employee_management.entity.EmployeeStatus;
import com.example.employee_management.exception.DuplicateEmailException;
import com.example.employee_management.exception.ResourceNotFoundException;
import com.example.employee_management.service.DepartmentService;
import com.example.employee_management.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public EmployeeController(
            EmployeeService employeeService,
            DepartmentService departmentService
    ) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizePageSize(size));
        Long resolvedDepartmentId = parseDepartmentIdParameter(departmentId);
        EmployeeStatus resolvedStatus = parseStatusParameter(status);
        EmployeeDashboardData data = employeeService.getDashboard(resolvedDepartmentId, keyword, resolvedStatus, pageable);

        model.addAttribute("employees", data.page().getContent());
        model.addAttribute("currentPage", data.page().getNumber());
        model.addAttribute("totalPages", data.page().getTotalPages());
        model.addAttribute("totalElements", data.page().getTotalElements());
        model.addAttribute("departmentId", resolvedDepartmentId);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("status", resolvedStatus != null ? resolvedStatus.name() : "");
        model.addAttribute("totalEmployees", data.totalEmployees());
        model.addAttribute("totalDepartments", data.totalDepartments());
        model.addAttribute("employeesPerDepartment", data.employeesPerDepartment());
        model.addAttribute("departments", data.departmentsForFilter());
        model.addAttribute("employeeStatuses", EmployeeStatus.values());

        return "dashboard";
    }

    @GetMapping("/new")
    public String addEmployeePage(Model model) {
        populateEmployeeReferenceData(model);
        model.addAttribute("employee", new Employee());
        return "add-employee";
    }

    @PostMapping
    public String saveEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            populateEmployeeReferenceData(model);
            return "add-employee";
        }

        try {
            employeeService.saveEmployee(employee);
        } catch (DuplicateEmailException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateEmployeeReferenceData(model);
            return "add-employee";
        } catch (IllegalArgumentException | ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateEmployeeReferenceData(model);
            return "add-employee";
        }

        return "redirect:/employees/dashboard";
    }

    @GetMapping("/view-all")
    public String viewAll(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "view-all";
    }

    @GetMapping("/view")
    public String viewEmployeeLookupPage() {
        return "view-employee";
    }

    @GetMapping("/view-result")
    public String viewEmployeeLookupResult(@RequestParam("employeeId") String employeeId, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.getEmployeeByEmployeeId(employeeId);
            return "redirect:/employees/profile/" + employee.getEmployeeId();
        } catch (IllegalArgumentException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/employees/view";
        }
    }

    @GetMapping("/profile/{employeeId}")
    public String employeeProfile(@PathVariable String employeeId, Model model) {
        model.addAttribute("employee", employeeService.getEmployeeByEmployeeId(employeeId));
        return "employee-profile";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        populateEmployeeReferenceData(model);
        model.addAttribute("employee", employeeService.getEmployeeById(id));
        return "update-employee";
    }

    @PostMapping("/update")
    public String updateEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            populateEmployeeReferenceData(model);
            return "update-employee";
        }

        try {
            employeeService.updateEmployee(employee.getId(), employee);
        } catch (DuplicateEmailException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateEmployeeReferenceData(model);
            return "update-employee";
        } catch (IllegalArgumentException | ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateEmployeeReferenceData(model);
            return "update-employee";
        }

        return "redirect:/employees/dashboard";
    }

    @GetMapping("/delete-page")
    public String deletePage() {
        return "delete-employee";
    }

    @PostMapping("/delete")
    public String deleteEmployee(@RequestParam String employeeId, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteByEmployeeId(employeeId);
        } catch (IllegalArgumentException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/employees/delete-page";
        }
        return "redirect:/employees/dashboard";
    }

    private void populateEmployeeReferenceData(Model model) {
        model.addAttribute("departments", departmentService.findAllSorted());
        model.addAttribute("employeeStatuses", EmployeeStatus.values());
    }

    private static int normalizePageSize(int size) {
        if (size < 1) {
            return 5;
        }
        return Math.min(size, 100);
    }

    /**
     * Allow HTML forms to emit blank {@code departmentId} selects without MVC conversion failures.
     */
    private static Long parseDepartmentIdParameter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static EmployeeStatus parseStatusParameter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return EmployeeStatus.valueOf(raw.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
