package com.intrahub.portal.auth;

import com.intrahub.portal.auth.dto.AuthResponse;
import com.intrahub.portal.auth.dto.LoginRequest;
import com.intrahub.portal.auth.dto.RegisterRequest;
import com.intrahub.portal.common.BusinessException;
import com.intrahub.portal.common.Role;
import com.intrahub.portal.employee.Employee;
import com.intrahub.portal.employee.EmployeeRepository;
import com.intrahub.portal.security.JwtUtil;
import com.intrahub.portal.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        mockEmployee = Employee.builder()
                .id(1L)
                .email("john.doe@intrahub.com")
                .passwordHash("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .employeeCode("EMP-1001")
                .role(Role.EMPLOYEE)
                .active(true)
                .locked(false)
                .build();
    }

    @Test
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("john.doe@intrahub.com")
                .password("password123")
                .build();

        UserPrincipal principal = new UserPrincipal(mockEmployee);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtUtil.generateToken(principal)).thenReturn("mocked-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getAccessToken());
        assertEquals("John", response.getUser().getFirstName());
        assertEquals("john.doe@intrahub.com", response.getUser().getEmail());
    }

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("jane.doe@intrahub.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Doe")
                .build();

        when(employeeRepository.existsByEmail("jane.doe@intrahub.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(2L);
            return emp;
        });
        when(jwtUtil.generateToken(any(UserPrincipal.class))).thenReturn("mocked-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getAccessToken());
        assertEquals("Jane", response.getUser().getFirstName());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("john.doe@intrahub.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(employeeRepository.existsByEmail("john.doe@intrahub.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(request));
        verify(employeeRepository, never()).save(any());
    }
}
