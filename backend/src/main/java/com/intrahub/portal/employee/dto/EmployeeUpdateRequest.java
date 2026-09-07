package com.intrahub.portal.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phone;
    private String designation;
    private Long departmentId;
    private Long managerId;
    private LocalDate dateOfJoining;
    private LocalDate dateOfBirth;
    private String profilePictureUrl;
    private Integer leaveBalanceAnnual;
    private Integer leaveBalanceSick;
}
