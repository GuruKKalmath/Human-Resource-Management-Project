package com.example.employee_management.repository;

import com.example.employee_management.dto.DepartmentStatistic;
import com.example.employee_management.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
            SELECT new com.example.employee_management.dto.DepartmentStatistic(d.name, COUNT(e.id))
            FROM Department d
            LEFT JOIN d.employees e
            GROUP BY d.id, d.name
            ORDER BY d.name
            """)
    List<DepartmentStatistic> summarizeEmployeesPerDepartment();

    List<Department> findAllByOrderByNameAsc();
}
