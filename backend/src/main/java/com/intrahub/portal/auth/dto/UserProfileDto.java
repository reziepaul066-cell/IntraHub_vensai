package com.intrahub.portal.auth.dto;

import com.intrahub.portal.common.Role;
import com.intrahub.portal.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String employeeCode;
    private String phone;
    private String designation;
    private Role role;
    private String departmentName;
    private String profilePictureUrl;
    private boolean active;
    private int leaveBalanceAnnual;
    private int leaveBalanceSick;

    public static UserProfileDto fromEntity(Employee employee) {
        if (employee == null) return null;
        return UserProfileDto.builder()
                .id(employee.getId())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .employeeCode(employee.getEmployeeCode())
                .phone(employee.getPhone())
                .designation(employee.getDesignation())
                .role(employee.getRole())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .profilePictureUrl(employee.getProfilePictureUrl())
                .active(employee.isActive())
                .leaveBalanceAnnual(employee.getLeaveBalanceAnnual())
                .leaveBalanceSick(employee.getLeaveBalanceSick())
                .build();
    }
}
