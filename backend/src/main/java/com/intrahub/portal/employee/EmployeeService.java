package com.intrahub.portal.employee;

import com.intrahub.portal.auth.dto.UserProfileDto;
import com.intrahub.portal.common.BusinessException;
import com.intrahub.portal.common.ResourceNotFoundException;
import com.intrahub.portal.common.Role;
import com.intrahub.portal.employee.dto.EmployeeUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public Page<UserProfileDto> getEmployees(String search, Long departmentId, Boolean active, Pageable pageable) {
        return employeeRepository.searchEmployees(search, departmentId, active, pageable)
                .map(UserProfileDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public UserProfileDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return UserProfileDto.fromEntity(employee);
    }

    @Transactional
    public UserProfileDto updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setDesignation(request.getDesignation());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
            employee.setDepartment(department);
        } else {
            employee.setDepartment(null);
        }

        if (request.getManagerId() != null) {
            if (request.getManagerId().equals(id)) {
                throw new BusinessException("An employee cannot be their own manager");
            }
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager employee", request.getManagerId()));
            employee.setManager(manager);
        }

        if (request.getDateOfJoining() != null) {
            employee.setDateOfJoining(request.getDateOfJoining());
        }
        if (request.getDateOfBirth() != null) {
            employee.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getProfilePictureUrl() != null) {
            employee.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        if (request.getLeaveBalanceAnnual() != null) {
            employee.setLeaveBalanceAnnual(request.getLeaveBalanceAnnual());
        }
        if (request.getLeaveBalanceSick() != null) {
            employee.setLeaveBalanceSick(request.getLeaveBalanceSick());
        }

        Employee updated = employeeRepository.save(employee);
        return UserProfileDto.fromEntity(updated);
    }

    @Transactional
    public UserProfileDto updateRole(Long id, Role newRole) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        employee.setRole(newRole);
        Employee updated = employeeRepository.save(employee);
        return UserProfileDto.fromEntity(updated);
    }

    @Transactional
    public UserProfileDto toggleActive(Long id, boolean active) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        employee.setActive(active);
        Employee updated = employeeRepository.save(employee);
        return UserProfileDto.fromEntity(updated);
    }
}
