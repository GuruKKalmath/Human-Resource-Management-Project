package com.example.employee_management.service;

import com.example.employee_management.dto.DepartmentStatistic;
import com.example.employee_management.entity.Department;
import com.example.employee_management.exception.ResourceNotFoundException;
import com.example.employee_management.repository.DepartmentRepository;
import com.example.employee_management.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> findAllSorted() {
        return departmentRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Override
    @Transactional
    public Department createDepartment(Department department) {
        String normalizedName = normalizeName(department.getName());
        if (departmentRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("A department named '" + normalizedName + "' already exists.");
        }
        department.setName(normalizedName);
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, Department incoming) {
        Department existing = getDepartmentById(id);
        String normalizedName = normalizeName(incoming.getName());
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new IllegalArgumentException("Another department named '" + normalizedName + "' already exists.");
        }
        existing.setName(normalizedName);
        existing.setDescription(incoming.getDescription());
        existing.setLocation(incoming.getLocation());
        return departmentRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        if (employeeRepository.existsByDepartment_Id(id)) {
            throw new IllegalStateException("Cannot delete department while employees are assigned to it.");
        }
        departmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countDepartments() {
        return departmentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentStatistic> getEmployeesPerDepartmentStatistics() {
        return departmentRepository.summarizeEmployeesPerDepartment();
    }

    private static String normalizeName(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Department name is required.");
        }
        return raw.trim();
    }
}
