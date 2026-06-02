package com.example.employee_management.service;

import com.example.employee_management.dto.EmployeeDashboardData;
import com.example.employee_management.entity.Employee;
import com.example.employee_management.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    List<Employee> getAllEmployees();

    Employee getEmployeeById(Long id);

    Employee getEmployeeByEmployeeId(String employeeId);

    Employee saveEmployee(Employee employee);

    Employee updateEmployee(Long id, Employee employee);

    void deleteEmployee(Long id);

    void deleteByEmployeeId(String employeeId);

    EmployeeDashboardData getDashboard(Long departmentId, String keyword, EmployeeStatus status, Pageable pageable);

    Page<Employee> searchByKeyword(String keyword, Pageable pageable);
}
