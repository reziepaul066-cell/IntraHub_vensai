package com.intrahub.portal.auth;

import com.intrahub.portal.auth.dto.AuthResponse;
import com.intrahub.portal.auth.dto.LoginRequest;
import com.intrahub.portal.auth.dto.RegisterRequest;
import com.intrahub.portal.auth.dto.UserProfileDto;
import com.intrahub.portal.common.BusinessException;
import com.intrahub.portal.common.ResourceNotFoundException;
import com.intrahub.portal.common.Role;
import com.intrahub.portal.employee.Employee;
import com.intrahub.portal.employee.EmployeeRepository;
import com.intrahub.portal.security.JwtUtil;
import com.intrahub.portal.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@SuppressWarnings("null")
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Employee employee = userPrincipal.getEmployee();

        if (!employee.isActive()) {
            throw new BusinessException("Account is deactivated. Please contact administrator.");
        }
        if (employee.isLocked()) {
            throw new BusinessException("Account is locked. Please contact administrator.");
        }

        String token = jwtUtil.generateToken(userPrincipal);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(expirationMs)
                .user(UserProfileDto.fromEntity(employee))
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email address is already in use: " + request.getEmail());
        }

        String empCode = request.getEmployeeCode();
        if (empCode != null && !empCode.trim().isEmpty()) {
            if (employeeRepository.existsByEmployeeCode(empCode)) {
                throw new BusinessException("Employee code is already registered: " + empCode);
            }
        } else {
            empCode = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Role role = request.getRole() != null ? request.getRole() : Role.EMPLOYEE;

        Employee employee = Employee.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .employeeCode(empCode)
                .phone(request.getPhone())
                .designation(request.getDesignation())
                .role(role)
                .active(true)
                .locked(false)
                .leaveBalanceAnnual(15)
                .leaveBalanceSick(10)
                .build();

        Employee saved = employeeRepository.save(employee);
        UserPrincipal userPrincipal = new UserPrincipal(saved);
        String token = jwtUtil.generateToken(userPrincipal);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(expirationMs)
                .user(UserProfileDto.fromEntity(saved))
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));
        return UserProfileDto.fromEntity(employee);
    }
}
