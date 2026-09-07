package com.intrahub.portal.employee;

import com.intrahub.portal.auth.dto.UserProfileDto;
import com.intrahub.portal.common.ApiResponse;
import com.intrahub.portal.common.Role;
import com.intrahub.portal.employee.dto.EmployeeUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/api/employees")
    public ResponseEntity<ApiResponse<Page<UserProfileDto>>> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserProfileDto> employees = employeeService.getEmployees(search, departmentId, active, pageable);
        return ResponseEntity.ok(ApiResponse.ok(employees));
    }

    @GetMapping("/api/employees/{id}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getEmployeeById(@PathVariable Long id) {
        UserProfileDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.ok(employee));
    }

    @PutMapping("/api/employees/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #id == authentication.principal.employee.id")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {

        UserProfileDto updated = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Employee updated successfully", updated));
    }

    @PutMapping("/api/admin/employees/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateRole(
            @PathVariable Long id,
            @RequestParam Role role) {

        UserProfileDto updated = employeeService.updateRole(id, role);
        return ResponseEntity.ok(ApiResponse.ok("Employee role updated successfully", updated));
    }

    @PutMapping("/api/admin/employees/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> toggleStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        UserProfileDto updated = employeeService.toggleActive(id, active);
        String msg = active ? "Employee activated successfully" : "Employee deactivated successfully";
        return ResponseEntity.ok(ApiResponse.ok(msg, updated));
    }
}
