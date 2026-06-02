package com.example.employee_management.repository;

import com.example.employee_management.entity.Department;
import com.example.employee_management.entity.Employee;
import com.example.employee_management.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeId(String employeeId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Page<Employee> findByDepartment(Department department, Pageable pageable);

    Page<Employee> findByDepartment_Id(Long departmentId, Pageable pageable);

    long countByDepartment(Department department);

    long countByDepartment_Id(Long departmentId);

    boolean existsByDepartment_Id(Long departmentId);

    /**
     * Global search across employee code, personal fields, and department name (supports pagination).
     */
    @Query("""
            SELECT e FROM Employee e
            JOIN e.department d
            WHERE (:keywordPattern IS NULL OR (
                LOWER(e.employeeId) LIKE :keywordPattern OR
                LOWER(e.name) LIKE :keywordPattern OR
                LOWER(e.email) LIKE :keywordPattern OR
                LOWER(d.name) LIKE :keywordPattern
            ))
            """)
    Page<Employee> searchByKeyword(@Param("keywordPattern") String keywordPattern, Pageable pageable);

    /**
     * Dashboard: department filter + optional status + global keyword search, combined with pagination.
     */
    @Query("""
            SELECT e FROM Employee e
            JOIN e.department d
            WHERE (:departmentId IS NULL OR d.id = :departmentId)
            AND (:status IS NULL OR e.status = :status)
            AND (
                :keywordPattern IS NULL OR (
                    LOWER(e.employeeId) LIKE :keywordPattern OR
                    LOWER(e.name) LIKE :keywordPattern OR
                    LOWER(e.email) LIKE :keywordPattern OR
                    LOWER(d.name) LIKE :keywordPattern
                )
            )
            """)
    Page<Employee> searchEmployeesFiltered(
            @Param("departmentId") Long departmentId,
            @Param("status") EmployeeStatus status,
            @Param("keywordPattern") String keywordPattern,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Employee e
            JOIN FETCH e.department d
            ORDER BY e.employeeId ASC
            """)
    List<Employee> findAllWithDepartmentOrdered();

    @Query("""
            SELECT e FROM Employee e
            JOIN FETCH e.department d
            WHERE e.id = :id
            """)
    Optional<Employee> findWithDepartmentById(@Param("id") Long id);

    @Query("""
            SELECT e FROM Employee e
            JOIN FETCH e.department d
            WHERE e.employeeId = :employeeId
            """)
    Optional<Employee> findWithDepartmentByEmployeeId(@Param("employeeId") String employeeId);

    @Query(
            value = """
                    SELECT COALESCE(MAX(CAST(SUBSTRING(employee_id, 4) AS UNSIGNED)), 0)
                    FROM employees
                    WHERE employee_id REGEXP '^EMP[0-9]+$'
                    """,
            nativeQuery = true)
    long findMaxEmployeeNumericSuffix();
}
