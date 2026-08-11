package org.ecommerce.auth.Dtos.response;

public record UserAndTokenResponseDto(String accessToken, String refreshToken,
                                      UserResponseDto userResponseDto) {
}
