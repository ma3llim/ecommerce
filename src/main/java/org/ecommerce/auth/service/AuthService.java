package org.ecommerce.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.ecommerce.auth.utils.TokenUtils;
import org.ecommerce.common.config.properties.JwtProperties;
import org.ecommerce.common.constants.AppConstants;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.common.notification.dtos.NotificationRequest;
import org.ecommerce.common.notification.enums.channel.NotificationChannel;
import org.ecommerce.common.notification.enums.channel.NotificationEvent;
import org.ecommerce.common.notification.service.NotificationService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordUtils passwordUtils;
    private final NotificationService notificationService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserResponseDto registerUser(RegisterUserRequestDto requestDto) {
        // check user is existed or not
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            log.warn("User registration rejected: email already exists");
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        // encode the password
        String hashPassword = passwordUtils.encode(requestDto.getPassword());
        // save user
        User user = User.builder()
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .password(hashPassword)
                .build();

        userRepository.save(user);
        log.info("User created successfully: userId={}", user.getId());

        // Generate Otp and Verification Code
        String otp = TokenUtils.generateOtp();

        // save otp
        OtpVerification otpVerification = OtpVerification.builder()
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .userId(user.getId())
                .otpCode(otp)
                .expiresAt(Instant.now().plus(AppConstants.OTP_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                .status(OtpStatus.PENDING)
                .build();

        otpVerificationRepository.save(otpVerification);
        log.info(
                "Email verification initialized successfully: userId={}, purpose={}",
                user.getId(),
                OtpPurpose.EMAIL_VERIFICATION
        );

        // send mail
        sendOtpMail(user.getFullName(), otp, String.valueOf(AppConstants.OTP_TOKEN_EXPIRY_MINUTES), user.getEmail());

        // Last Log
        log.info("User registration completed successfully: userId={}", user.getId());

        return UserResponseDto.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .accountStatus(user.getAccountStatus())
                .role(user.getRole())
                .build();
    }

    public UserAndTokenResponseDto verifyEmail(VerifyEmailRequestDto verifyEmailRequest) {
        // check user is existed or not
        User user = userRepository.findById(verifyEmailRequest.getUserId()).orElseThrow(() -> {
            log.warn("User not found for email verification, userId={}", verifyEmailRequest.getUserId());
            return new ResourceNotFoundException("User not found");
        });

        OtpVerification otpVerification = otpVerificationRepository.findByUserIdAndPurposeAndStatus(
                user.getId(),
                OtpPurpose.EMAIL_VERIFICATION,
                OtpStatus.PENDING).orElseThrow(() -> {
            log.warn("Pending OTP not found for userId={}", user.getId());
            return new BadCredentialsException("Invalid OTP");
        });

        if (otpVerification.getAttemptCount() >= AppConstants.MAX_OTP_ATTEMPTS) {
            throw new BadCredentialsException("Maximum OTP attempts exceeded");
        }

        if (Instant.now().isAfter(otpVerification.getExpiresAt())) {
            otpVerification.setStatus(OtpStatus.EXPIRED);

            otpVerificationRepository.save(otpVerification);
            throw new BadCredentialsException("OTP has expired");
        }

        if (!verifyEmailRequest.getOtp().equals(otpVerification.getOtpCode())) {
            otpVerification.setAttemptCount(otpVerification.getAttemptCount() + 1);
            otpVerificationRepository.save(otpVerification);

            log.warn("Invalid OTP attempt for userId={}, attemptCount={}", user.getId(),
                    otpVerification.getAttemptCount());

            throw new BadCredentialsException("Invalid OTP");
        }
        // update the OTP
        otpVerification.setStatus(OtpStatus.VERIFIED);
        otpVerification.setVerifiedAt(Instant.now());
        // Set User as Active
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);

        // Generate Token
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, UUID.randomUUID().toString());
        log.info("Access and refresh tokens generated successfully for userId={}", user.getId());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .refreshToken(refreshToken)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpiration()))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
        UserResponseDto userResponseDto = UserResponseDto.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .accountStatus(user.getAccountStatus())
                .role(user.getRole())
                .build();

        return new UserAndTokenResponseDto(accessToken, refreshToken, userResponseDto);
    }

    // Notifications Functions
    // Send OTP to user
    private void sendOtpMail(String fullName, String otp, String expiryMinutes, String recipientEmail) {
        Map<String, Object> data = new HashMap<>();
        data.put("fullName", fullName);
        data.put("otp", otp);
        data.put("expiryMinutes", expiryMinutes);

        NotificationRequest request = NotificationRequest.builder()
                .channel(NotificationChannel.EMAIL)
                .event(NotificationEvent.OTP_VERIFICATION)
                .recipient(recipientEmail)
                .data(data)
                .build();

        notificationService.send(request);
    }
}
