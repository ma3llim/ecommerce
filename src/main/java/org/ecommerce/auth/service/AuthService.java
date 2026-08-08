package org.ecommerce.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.Dtos.request.RegisterUserRequestDto;
import org.ecommerce.auth.Dtos.response.RegisterUserResponseDto;
import org.ecommerce.auth.entities.OtpVerification;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.OtpPurpose;
import org.ecommerce.auth.enums.OtpStatus;
import org.ecommerce.auth.repository.OtpVerificationRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.auth.utils.PasswordUtils;
import org.ecommerce.auth.utils.TokenUtils;
import org.ecommerce.common.constants.AppConstants;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordUtils passwordUtils;

    @Transactional
    public RegisterUserResponseDto registerUser(RegisterUserRequestDto requestDto) {
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
        String verificationToken = TokenUtils.generateRandomToken();

        // save otp
        OtpVerification otpVerification = OtpVerification.builder()
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .userId(user.getId())
                .otpCode(otp)
                .verificationToken(verificationToken)
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

        // Last Log
        log.info("User registration completed successfully: userId={}", user.getId());

        return RegisterUserResponseDto.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .accountStatus(user.getAccountStatus())
                .role(user.getRole())
                .build();
    }
}
