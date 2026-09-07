package com.intrahub.portal.leave;

import com.intrahub.portal.common.BusinessException;
import com.intrahub.portal.common.ResourceNotFoundException;
import com.intrahub.portal.employee.Employee;
import com.intrahub.portal.employee.EmployeeRepository;
import com.intrahub.portal.leave.dto.LeaveApprovalDto;
import com.intrahub.portal.leave.dto.LeaveRequestDto;
import com.intrahub.portal.leave.dto.LeaveResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveResponseDto applyForLeave(Long employeeId, LeaveRequestDto dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException("End date cannot be before start date");
        }

        int totalDays = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        // Verify leave balance for ANNUAL and SICK leave types
        if (dto.getLeaveType() == LeaveType.ANNUAL) {
            int currentBalance = employee.getLeaveBalanceAnnual();
            if (currentBalance < totalDays) {
                throw new BusinessException("Insufficient annual leave balance. Requested: " + totalDays + " days, Available: " + currentBalance + " days");
            }
        } else if (dto.getLeaveType() == LeaveType.SICK) {
            int currentBalance = employee.getLeaveBalanceSick();
            if (currentBalance < totalDays) {
                throw new BusinessException("Insufficient sick leave balance. Requested: " + totalDays + " days, Available: " + currentBalance + " days");
            }
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(dto.getLeaveType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalDays(totalDays)
                .reason(dto.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return LeaveResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponseDto> getMyLeaveRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(LeaveResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveResponseDto> getPendingLeaveRequests(Long managerId) {
        return leaveRequestRepository.findPendingRequestsForManager(LeaveStatus.PENDING, managerId).stream()
                .map(LeaveResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveResponseDto processLeaveRequest(Long requestId, LeaveApprovalDto dto, Long approverId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", requestId));

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Leave request is already processed with status: " + request.getStatus());
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver employee", approverId));

        if (dto.getStatus() == LeaveStatus.APPROVED) {
            Employee employee = request.getEmployee();
            int days = request.getTotalDays();

            if (request.getLeaveType() == LeaveType.ANNUAL) {
                int balance = employee.getLeaveBalanceAnnual();
                employee.setLeaveBalanceAnnual(Math.max(0, balance - days));
            } else if (request.getLeaveType() == LeaveType.SICK) {
                int balance = employee.getLeaveBalanceSick();
                employee.setLeaveBalanceSick(Math.max(0, balance - days));
            }
            employeeRepository.save(employee);

            request.setStatus(LeaveStatus.APPROVED);
            request.setApprovedBy(approver);
        } else if (dto.getStatus() == LeaveStatus.REJECTED) {
            request.setStatus(LeaveStatus.REJECTED);
            request.setRejectionReason(dto.getRejectionReason());
            request.setApprovedBy(approver);
        } else {
            throw new BusinessException("Invalid processing status: " + dto.getStatus());
        }

        LeaveRequest updated = leaveRequestRepository.save(request);
        return LeaveResponseDto.fromEntity(updated);
    }
}
