package com.intrahub.portal.leave.dto;

import com.intrahub.portal.leave.LeaveRequest;
import com.intrahub.portal.leave.LeaveStatus;
import com.intrahub.portal.leave.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponseDto {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String departmentName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private LeaveStatus status;
    private String rejectionReason;
    private String approvedByName;
    private java.time.Instant createdAt;

    public static LeaveResponseDto fromEntity(LeaveRequest request) {
        if (request == null) return null;

        return LeaveResponseDto.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName())
                .employeeCode(request.getEmployee().getEmployeeCode())
                .departmentName(request.getEmployee().getDepartment() != null ? request.getEmployee().getDepartment().getName() : "Unassigned")
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(request.getTotalDays())
                .reason(request.getReason())
                .status(request.getStatus())
                .rejectionReason(request.getRejectionReason())
                .approvedByName(request.getApprovedBy() != null ? request.getApprovedBy().getFirstName() + " " + request.getApprovedBy().getLastName() : null)
                .createdAt(request.getCreatedAt())
                .build();
    }
}
