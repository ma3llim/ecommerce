package org.ecommerce.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecommerce.auth.Dtos.request.RegisterUserRequestDto;
import org.ecommerce.auth.Dtos.response.UserResponseDto;
import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.OtpPurpose;
import org.ecommerce.auth.enums.OtpStatus;
import org.ecommerce.auth.repository.OtpVerificationRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.auth.utils.PasswordUtils;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.ecommerce.common.notification.dtos.NotificationRequest;
import org.ecommerce.common.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceRegisterUserTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthService authService;

    private RegisterUserRequestDto registerRequest;
    private User user;
    private OtpVerification otpVerification;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterUserRequestDto(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );

        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("9876543210")
                .password("encoded-password")
                .build();

        otpVerification = OtpVerification.builder()
                .userId(user.getId())
                .otpCode("456456")
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .status(OtpStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.findByEmail(registerRequest.email()))
                .thenReturn(Optional.of(user));

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.registerUser(registerRequest));

        verify(userRepository, never()).save(any(User.class));
        verify(otpVerificationRepository, never()).save(any(OtpVerification.class));
        verify(notificationService, never()).send(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("Should register user successfully when email does not exist")
    void shouldRegisterUserSuccessfullyWhenEmailDoesNotExist() {
        UserResponseDto userResponseDto = new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getAccountStatus(),
                user.getRole()
        );
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());

        when(passwordUtils.encode(registerRequest.password())).thenReturn("encoded-password");

        when(userRepository.save(any(User.class))).thenReturn(user);

        when(otpVerificationRepository.save(any(OtpVerification.class))).thenReturn(otpVerification);

        when(objectMapper.convertValue(any(User.class), eq(UserResponseDto.class))).thenReturn(userResponseDto);

        UserResponseDto result = authService.registerUser(registerRequest);

        assertNotNull(result);

        verify(passwordUtils).encode(registerRequest.password());

        verify(userRepository).save(any(User.class));

        verify(otpVerificationRepository).save(any(OtpVerification.class));

        verify(notificationService).send(any(NotificationRequest.class));
    }
}