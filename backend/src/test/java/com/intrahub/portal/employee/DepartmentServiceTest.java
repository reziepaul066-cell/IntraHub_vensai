package com.intrahub.portal.employee;

import com.intrahub.portal.common.BusinessException;
import com.intrahub.portal.employee.dto.DepartmentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department dept;

    @BeforeEach
    void setUp() {
        dept = Department.builder()
                .id(1L)
                .name("Engineering")
                .description("Software Development")
                .headName("Alice Tech")
                .build();
    }

    @Test
    void getAllDepartments_Success() {
        when(departmentRepository.findAll()).thenReturn(List.of(dept));
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(5L);

        List<DepartmentDto> result = departmentService.getAllDepartments();

        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0).getName());
        assertEquals(5L, result.get(0).getEmployeeCount());
    }

    @Test
    void createDepartment_Success() {
        DepartmentDto request = DepartmentDto.builder()
                .name("HR")
                .description("Human Resources")
                .build();

        when(departmentRepository.existsByName("HR")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(i -> {
            Department d = i.getArgument(0);
            d.setId(2L);
            return d;
        });

        DepartmentDto result = departmentService.createDepartment(request);

        assertNotNull(result);
        assertEquals("HR", result.getName());
    }

    @Test
    void createDepartment_DuplicateName_ThrowsException() {
        DepartmentDto request = DepartmentDto.builder().name("Engineering").build();
        when(departmentRepository.existsByName("Engineering")).thenReturn(true);

        assertThrows(BusinessException.class, () -> departmentService.createDepartment(request));
    }

    @Test
    void deleteDepartment_WithEmployees_ThrowsException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(3L);

        assertThrows(BusinessException.class, () -> departmentService.deleteDepartment(1L));
    }
}
