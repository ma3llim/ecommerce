package org.ecommerce.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecommerce.auth.Dtos.request.RegisterUserRequestDto;
import org.ecommerce.auth.Dtos.request.VerifyEmailRequestDto;
import org.ecommerce.auth.Dtos.response.UserAndTokenResponseDto;
import org.ecommerce.auth.Dtos.response.UserResponseDto;
import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.entities.RefreshToken;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.enums.OtpPurpose;
import org.ecommerce.auth.enums.OtpStatus;
import org.ecommerce.auth.repository.OtpVerificationRepository;
import org.ecommerce.auth.repository.RefreshTokenRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.auth.security.JwtService;
import org.ecommerce.auth.utils.PasswordUtils;
import org.ecommerce.common.config.properties.JwtProperties;
import org.ecommerce.common.constants.AppConstants;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceRegistrationTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    @Spy
    private AuthService authService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    private RegisterUserRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new RegisterUserRequestDto(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );
    }

    // registerUser()
    @Test
    @DisplayName("Should throw exception when email already exists")
    void registerUser_whenEmailAlreadyExists_throwsResourceAlreadyExistsException() {
        User existingUser = new User();

        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.of(existingUser));

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.registerUser(requestDto));

        verify(userRepository).findByEmail(requestDto.email());
        verify(passwordUtils, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(otpVerificationRepository, never()).save(any());
        verifyNoInteractions(objectMapper);
    }


    @Test
    @DisplayName("Should create user and return response for valid registration")
    void registerUser_whenValidRequest_createsUserAndReturnsResponse() {
        UUID userId = UUID.randomUUID();
        String encodedPassword = "encoded-password";
        UserResponseDto expectedResponse = mock(UserResponseDto.class);

        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.empty());
        when(passwordUtils.encode(requestDto.password())).thenReturn(encodedPassword);

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        }).when(userRepository).save(any(User.class));

        doNothing().when(authService)
                .sendOtpMail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        when(objectMapper.convertValue(
                any(User.class),
                eq(UserResponseDto.class)
        )).thenReturn(expectedResponse);

        UserResponseDto result =
                authService.registerUser(requestDto);

        assertSame(expectedResponse, result);

        verify(userRepository).findByEmail(requestDto.email());

        verify(passwordUtils).encode(requestDto.password());

        verify(userRepository).save(any(User.class));

        verify(otpVerificationRepository).save(any(OtpVerification.class));

        verify(objectMapper)
                .convertValue(
                        any(User.class),
                        eq(UserResponseDto.class)
                );
    }

    @Test
    @DisplayName("Should save user with correct details for valid registration")
    void registerUser_whenValidRequest_savesUserWithCorrectDetails() {

        UUID userId = UUID.randomUUID();

        String encodedPassword = "encoded-password";

        when(userRepository.findByEmail(requestDto.email()))
                .thenReturn(Optional.empty());

        when(passwordUtils.encode(requestDto.password()))
                .thenReturn(encodedPassword);

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        }).when(userRepository).save(any(User.class));

        doNothing().when(authService)
                .sendOtpMail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        when(objectMapper.convertValue(
                any(User.class),
                eq(UserResponseDto.class)
        )).thenReturn(mock(UserResponseDto.class));

        authService.registerUser(requestDto);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(requestDto.firstName(), savedUser.getFirstName());
        assertEquals(requestDto.lastName(), savedUser.getLastName());
        assertEquals(requestDto.email(), savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPassword());
    }

    @Test
    @DisplayName("Should save pending email verification OTP for valid registration")
    void registerUser_whenValidRequest_savesPendingEmailVerificationOtp() {

        UUID userId = UUID.randomUUID();

        String encodedPassword = "encoded-password";

        when(userRepository.findByEmail(requestDto.email()))
                .thenReturn(Optional.empty());

        when(passwordUtils.encode(requestDto.password()))
                .thenReturn(encodedPassword);

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        }).when(userRepository).save(any(User.class));

        doNothing().when(authService)
                .sendOtpMail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        when(objectMapper.convertValue(
                any(User.class),
                eq(UserResponseDto.class)
        )).thenReturn(mock(UserResponseDto.class));

        authService.registerUser(requestDto);

        ArgumentCaptor<OtpVerification> otpCaptor =
                ArgumentCaptor.forClass(OtpVerification.class);

        verify(otpVerificationRepository)
                .save(otpCaptor.capture());

        OtpVerification savedOtp = otpCaptor.getValue();

        assertEquals(
                userId,
                savedOtp.getUserId()
        );

        assertEquals(OtpPurpose.EMAIL_VERIFICATION, savedOtp.getPurpose());

        assertEquals(OtpStatus.PENDING, savedOtp.getStatus());

        assertNotNull(savedOtp.getOtpCode());

        assertTrue(
                savedOtp.getOtpCode().matches("\\d{6}")
        );

        assertNotNull(savedOtp.getExpiresAt());

        assertTrue(savedOtp.getExpiresAt().isAfter(java.time.Instant.now()));
    }

    @Test
    @DisplayName("Should send OTP email for valid registration")
    void registerUser_whenValidRequest_sendsOtpMail() {
        UUID userId = UUID.randomUUID();

        String encodedPassword = "encoded-password";

        when(userRepository.findByEmail(requestDto.email()))
                .thenReturn(Optional.empty());

        when(passwordUtils.encode(requestDto.password()))
                .thenReturn(encodedPassword);

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        }).when(userRepository).save(any(User.class));

        doNothing().when(authService)
                .sendOtpMail(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                );

        when(objectMapper.convertValue(
                any(User.class),
                eq(UserResponseDto.class)
        )).thenReturn(mock(UserResponseDto.class));

        authService.registerUser(requestDto);

        verify(authService).sendOtpMail(
                anyString(),
                anyString(),
                anyString(),
                eq(requestDto.email())
        );
    }

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void verifyEmail_whenUserDoesNotExist_throwsResourceNotFoundException() {

        UUID userId = UUID.randomUUID();

        VerifyEmailRequestDto request = new VerifyEmailRequestDto(userId, "123456");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.verifyEmail(request));

        verify(userRepository).findById(userId);

        verifyNoInteractions(otpVerificationRepository);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should throw exception when pending OTP does not exist")
    void verifyEmail_whenPendingOtpDoesNotExist_throwsBadCredentialsException() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        VerifyEmailRequestDto request = new VerifyEmailRequestDto(
                userId,
                "123456"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.verifyEmail(request));

        verify(otpVerificationRepository)
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.PENDING
                );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should throw exception when maximum OTP attempts are exceeded")
    void verifyEmail_whenMaximumOtpAttemptsExceeded_throwsBadCredentialsException() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setAttemptCount(AppConstants.MAX_OTP_ATTEMPTS);
        otpVerification.setExpiresAt(
                Instant.now().plusSeconds(300)
        );

        VerifyEmailRequestDto request = new VerifyEmailRequestDto(
                userId,
                "123456"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.of(otpVerification));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.verifyEmail(request)
        );

        verify(otpVerificationRepository, never())
                .save(any(OtpVerification.class));

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should mark OTP as expired and throw exception when OTP is expired")
    void verifyEmail_whenOtpIsExpired_marksOtpExpiredAndThrowsException() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        OtpVerification otpVerification = new OtpVerification();

        otpVerification.setAttemptCount(0);
        otpVerification.setOtpCode("123456");
        otpVerification.setStatus(OtpStatus.PENDING);
        otpVerification.setExpiresAt(
                Instant.now().minusSeconds(60)
        );

        VerifyEmailRequestDto request = new VerifyEmailRequestDto(
                userId,
                "123456"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.of(otpVerification));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.verifyEmail(request)
        );

        assertEquals(
                OtpStatus.EXPIRED,
                otpVerification.getStatus()
        );

        verify(otpVerificationRepository)
                .save(otpVerification);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should increment OTP attempt count and throw exception when OTP is incorrect")
    void verifyEmail_whenOtpIsIncorrect_incrementsAttemptCountAndThrowsException() {

        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        OtpVerification otpVerification = new OtpVerification();

        otpVerification.setAttemptCount(2);
        otpVerification.setOtpCode("123456");
        otpVerification.setStatus(OtpStatus.PENDING);
        otpVerification.setExpiresAt(
                Instant.now().plusSeconds(300)
        );

        VerifyEmailRequestDto request = new VerifyEmailRequestDto(
                userId,
                "999999"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.of(otpVerification));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.verifyEmail(request)
        );

        assertEquals(
                3,
                otpVerification.getAttemptCount()
        );

        assertEquals(
                OtpStatus.PENDING,
                otpVerification.getStatus()
        );

        verify(otpVerificationRepository)
                .save(otpVerification);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should activate user and return tokens when OTP is correct")
    void verifyEmail_whenOtpIsCorrect_activatesUserAndReturnsTokens() {

        UUID userId = UUID.randomUUID();

        String otp = "123456";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");
        user.setEmailVerified(false);
        user.setAccountStatus(AccountStatus.PENDING);

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setAttemptCount(0);
        otpVerification.setOtpCode(otp);
        otpVerification.setStatus(OtpStatus.PENDING);
        otpVerification.setExpiresAt(
                Instant.now().plusSeconds(300)
        );

        VerifyEmailRequestDto request = new VerifyEmailRequestDto(
                userId,
                otp
        );

        UserResponseDto userResponse = mock(UserResponseDto.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.of(otpVerification));

        when(jwtService.generateAccessToken(user))
                .thenReturn(accessToken);

        when(jwtService.generateRefreshToken(
                eq(user),
                anyString()
        )).thenReturn(refreshToken);

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        when(objectMapper.convertValue(
                user,
                UserResponseDto.class
        )).thenReturn(userResponse);

        UserAndTokenResponseDto result =
                authService.verifyEmail(request);

        assertNotNull(result);

        assertEquals(
                accessToken,
                result.accessToken()
        );

        assertEquals(
                refreshToken,
                result.refreshToken()
        );

        assertSame(
                userResponse,
                result.userResponseDto()
        );

        assertTrue(user.isEmailVerified());

        assertEquals(
                AccountStatus.ACTIVE,
                user.getAccountStatus()
        );

        assertEquals(
                OtpStatus.VERIFIED,
                otpVerification.getStatus()
        );

        assertNotNull(
                otpVerification.getVerifiedAt()
        );

        verify(jwtService)
                .generateAccessToken(user);

        verify(jwtService)
                .generateRefreshToken(
                        eq(user),
                        anyString()
                );

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should save refresh token with correct details when OTP is correct")
    void verifyEmail_whenOtpIsCorrect_savesRefreshTokenWithCorrectDetails() {

        UUID userId = UUID.randomUUID();

        String otp = "123456";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");
        user.setEmailVerified(false);
        user.setAccountStatus(AccountStatus.PENDING);

        OtpVerification otpVerification = new OtpVerification();

        otpVerification.setAttemptCount(0);
        otpVerification.setOtpCode(otp);
        otpVerification.setStatus(OtpStatus.PENDING);
        otpVerification.setExpiresAt(
                Instant.now().plusSeconds(300)
        );

        VerifyEmailRequestDto request = new VerifyEmailRequestDto(
                userId,
                otp
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(otpVerificationRepository
                .findByUserIdAndPurposeAndStatus(
                        userId,
                        OtpPurpose.EMAIL_VERIFICATION,
                        OtpStatus.PENDING
                ))
                .thenReturn(Optional.of(otpVerification));

        when(jwtService.generateAccessToken(user))
                .thenReturn(accessToken);

        when(jwtService.generateRefreshToken(
                eq(user),
                anyString()
        )).thenReturn(refreshToken);

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        when(objectMapper.convertValue(
                user,
                UserResponseDto.class
        )).thenReturn(mock(UserResponseDto.class));

        authService.verifyEmail(request);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository)
                .save(refreshTokenCaptor.capture());

        RefreshToken savedRefreshToken =
                refreshTokenCaptor.getValue();

        assertNotNull(savedRefreshToken.getId());

        assertEquals(
                userId,
                savedRefreshToken.getUserId()
        );

        assertEquals(
                refreshToken,
                savedRefreshToken.getRefreshToken()
        );

        assertNotNull(
                savedRefreshToken.getExpiresAt()
        );

        assertTrue(
                savedRefreshToken.getExpiresAt()
                        .isAfter(Instant.now())
        );
    }

}
