package com.intrahub.portal.employee;

import com.intrahub.portal.common.BusinessException;
import com.intrahub.portal.common.ResourceNotFoundException;
import com.intrahub.portal.employee.dto.DepartmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(dept -> {
                    long count = employeeRepository.countByDepartmentId(dept.getId());
                    return DepartmentDto.fromEntity(dept, count);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        long count = employeeRepository.countByDepartmentId(id);
        return DepartmentDto.fromEntity(dept, count);
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new BusinessException("Department already exists with name: " + dto.getName());
        }

        Department department = Department.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .headName(dto.getHeadName())
                .build();

        Department saved = departmentRepository.save(department);
        return DepartmentDto.fromEntity(saved, 0);
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        if (departmentRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new BusinessException("Another department already exists with name: " + dto.getName());
        }

        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setHeadName(dto.getHeadName());

        Department updated = departmentRepository.save(department);
        long count = employeeRepository.countByDepartmentId(id);
        return DepartmentDto.fromEntity(updated, count);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        long count = employeeRepository.countByDepartmentId(id);
        if (count > 0) {
            throw new BusinessException("Cannot delete department with assigned employees. Reassign employees first.");
        }

        departmentRepository.delete(department);
    }
}
