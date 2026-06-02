package com.example.employee_management.service;

import com.example.employee_management.dto.DepartmentStatistic;
import com.example.employee_management.entity.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> findAllSorted();

    Department getDepartmentById(Long id);

    Department createDepartment(Department department);

    Department updateDepartment(Long id, Department incoming);

    void deleteDepartment(Long id);

    long countDepartments();

    List<DepartmentStatistic> getEmployeesPerDepartmentStatistics();
}
