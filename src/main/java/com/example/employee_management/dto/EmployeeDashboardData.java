package com.example.employee_management.dto;

import com.example.employee_management.entity.Department;
import com.example.employee_management.entity.Employee;
import org.springframework.data.domain.Page;

import java.util.List;

public record EmployeeDashboardData(
        Page<Employee> page,
        long totalEmployees,
        long totalDepartments,
        List<DepartmentStatistic> employeesPerDepartment,
        List<Department> departmentsForFilter
) {
}
