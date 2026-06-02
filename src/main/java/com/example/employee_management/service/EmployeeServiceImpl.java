package com.example.employee_management.service;

import com.example.employee_management.dto.DepartmentStatistic;
import com.example.employee_management.dto.EmployeeDashboardData;
import com.example.employee_management.entity.Department;
import com.example.employee_management.entity.Employee;
import com.example.employee_management.entity.EmployeeStatus;
import com.example.employee_management.exception.DuplicateEmailException;
import com.example.employee_management.exception.ResourceNotFoundException;
import com.example.employee_management.repository.DepartmentRepository;
import com.example.employee_management.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAllWithDepartmentOrdered();
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findWithDepartmentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findWithDepartmentByEmployeeId(normalizeEmployeeCode(employeeId))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + employeeId));
    }

    @Override
    @Transactional
    public Employee saveEmployee(Employee employee) {
        assertNewEmployeeHasNoPersistedIdentifiers(employee);
        String normalizedEmail = normalizeEmail(employee.getEmail());
        if (employeeRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException("Email '" + normalizedEmail + "' is already registered.");
        }
        employee.setEmail(normalizedEmail);
        hydrateDepartmentReference(employee);
        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }
        employee.setEmployeeId(generateNextEmployeeId());
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Employee updateEmployee(Long id, Employee incoming) {
        Employee existing = getEmployeeById(id);
        String normalizedEmail = normalizeEmail(incoming.getEmail());
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id)) {
            throw new DuplicateEmailException("Email '" + normalizedEmail + "' belongs to another employee.");
        }
        existing.setName(incoming.getName());
        existing.setEmail(normalizedEmail);
        hydrateDepartmentReference(incoming);
        existing.setDepartment(incoming.getDepartment());
        existing.setSalary(incoming.getSalary());
        if (incoming.getStatus() != null) {
            existing.setStatus(incoming.getStatus());
        }
        return employeeRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByEmployeeId(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(normalizeEmployeeCode(employeeId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with code: " + employeeId
                ));
        employeeRepository.deleteById(employee.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDashboardData getDashboard(
            Long departmentId,
            String keyword,
            EmployeeStatus status,
            Pageable pageable
    ) {
        String keywordPattern = buildLikePattern(keyword);
        Page<Employee> page = employeeRepository.searchEmployeesFiltered(
                departmentId,
                status,
                keywordPattern,
                pageable
        );
        long totalEmployees = employeeRepository.count();
        long totalDepartments = departmentRepository.count();
        List<DepartmentStatistic> perDept = departmentRepository.summarizeEmployeesPerDepartment();
        List<Department> filters = departmentRepository.findAllByOrderByNameAsc();

        return new EmployeeDashboardData(
                page,
                totalEmployees,
                totalDepartments,
                perDept,
                filters
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Employee> searchByKeyword(String keyword, Pageable pageable) {
        return employeeRepository.searchByKeyword(buildLikePattern(keyword), pageable);
    }

    /**
     * Next code in EMP### sequence derived from stored employee codes (generation lives in Service layer).
     */
    private synchronized String generateNextEmployeeId() {
        long suffix = employeeRepository.findMaxEmployeeNumericSuffix();
        int next = (int) suffix + 1;
        String candidate = String.format(Locale.US, "EMP%03d", next);
        if (employeeRepository.findByEmployeeId(candidate).isPresent()) {
            throw new IllegalStateException("Collision while generating employee id; retry the operation.");
        }
        return candidate;
    }

    private void hydrateDepartmentReference(Employee employee) {
        Department reference = employee.getDepartment();
        if (reference == null || reference.getId() == null) {
            throw new IllegalArgumentException("Department is required.");
        }
        Department managed = departmentRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + reference.getId()
                ));
        employee.setDepartment(managed);
    }

    private static void assertNewEmployeeHasNoPersistedIdentifiers(Employee employee) {
        if (employee.getId() != null) {
            throw new IllegalArgumentException("New employee must not include a database identifier.");
        }
        if (employee.getEmployeeId() != null && !employee.getEmployeeId().isBlank()) {
            throw new IllegalArgumentException("Employee code is assigned automatically.");
        }
    }

    private static String normalizeEmail(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeEmployeeCode(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        return employeeId.trim();
    }

    /**
     * Reusable lowercase LIKE pattern shared by dashboard filtering and standalone search endpoints.
     */
    private static String buildLikePattern(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String trimmed = keyword.trim().toLowerCase(Locale.ROOT);
        return "%" + trimmed + "%";
    }
}
