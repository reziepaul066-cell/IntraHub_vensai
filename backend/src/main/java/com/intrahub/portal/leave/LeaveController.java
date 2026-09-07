package com.intrahub.portal.leave;

import com.intrahub.portal.common.ApiResponse;
import com.intrahub.portal.leave.dto.LeaveApprovalDto;
import com.intrahub.portal.leave.dto.LeaveRequestDto;
import com.intrahub.portal.leave.dto.LeaveResponseDto;
import com.intrahub.portal.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LeaveResponseDto>> applyForLeave(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody LeaveRequestDto requestDto) {
        LeaveResponseDto response = leaveService.applyForLeave(currentUser.getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Leave request submitted successfully", response));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<LeaveResponseDto>>> getMyLeaveRequests(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<LeaveResponseDto> requests = leaveService.getMyLeaveRequests(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(requests));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<LeaveResponseDto>>> getPendingLeaveRequests(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<LeaveResponseDto> pending = leaveService.getPendingLeaveRequests(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(pending));
    }

    @PutMapping("/{id}/process")
    public ResponseEntity<ApiResponse<LeaveResponseDto>> processLeaveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody LeaveApprovalDto approvalDto) {
        LeaveResponseDto processed = leaveService.processLeaveRequest(id, approvalDto, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Leave request processed successfully", processed));
    }
}
