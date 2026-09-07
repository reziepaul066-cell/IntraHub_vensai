package com.intrahub.portal.leave.dto;

import com.intrahub.portal.leave.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalDto {

    @NotNull(message = "Status is required")
    private LeaveStatus status;

    private String rejectionReason;
}
