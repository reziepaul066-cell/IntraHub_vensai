package com.intrahub.portal.leave;

import com.intrahub.portal.common.BusinessException;
import com.intrahub.portal.employee.Employee;
import com.intrahub.portal.employee.EmployeeRepository;
import com.intrahub.portal.leave.dto.LeaveApprovalDto;
import com.intrahub.portal.leave.dto.LeaveRequestDto;
import com.intrahub.portal.leave.dto.LeaveResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;
    private Employee manager;

    @BeforeEach
    void setUp() {
        manager = Employee.builder()
                .id(1L)
                .firstName("Manager")
                .lastName("User")
                .email("manager@intrahub.com")
                .build();

        employee = Employee.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@intrahub.com")
                .leaveBalanceAnnual(15)
                .leaveBalanceSick(10)
                .manager(manager)
                .build();
    }

    @Test
    void applyForLeave_Success() {
        LeaveRequestDto dto = LeaveRequestDto.builder()
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .reason("Vacation")
                .build();

        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> {
            LeaveRequest req = i.getArgument(0);
            req.setId(10L);
            return req;
        });

        LeaveResponseDto result = leaveService.applyForLeave(2L, dto);

        assertNotNull(result);
        assertEquals(3, result.getTotalDays());
        assertEquals(LeaveStatus.PENDING, result.getStatus());
        assertEquals(LeaveType.ANNUAL, result.getLeaveType());
    }

    @Test
    void applyForLeave_InsufficientBalance_ThrowsException() {
        LeaveRequestDto dto = LeaveRequestDto.builder()
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(25)) // 25 days requested, balance is 15
                .reason("Long Trip")
                .build();

        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));

        assertThrows(BusinessException.class, () -> leaveService.applyForLeave(2L, dto));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void processLeaveRequest_Approve_DeductsBalance() {
        LeaveRequest pendingRequest = LeaveRequest.builder()
                .id(100L)
                .employee(employee)
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .totalDays(3)
                .status(LeaveStatus.PENDING)
                .build();

        LeaveApprovalDto approvalDto = LeaveApprovalDto.builder()
                .status(LeaveStatus.APPROVED)
                .build();

        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(pendingRequest));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArgument(0));

        LeaveResponseDto result = leaveService.processLeaveRequest(100L, approvalDto, 1L);

        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, result.getStatus());
        assertEquals(12, employee.getLeaveBalanceAnnual()); // 15 - 3 = 12
    }
}
