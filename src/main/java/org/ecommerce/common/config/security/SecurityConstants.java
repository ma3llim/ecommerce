package org.ecommerce.common.config.security;

public final class SecurityConstants {
    public static final String AUTH_REGISTER = "/api/v1/auth/register";
    public static final String AUTH_LOGIN = "/api/v1/auth/login";
    public static final String AUTH_REFRESH_TOKEN = "/api/v1/auth/refresh-token";
    public static final String AUTH_VERIFY_EMAIL = "/api/v1/auth/verify-email";
    public static final String AUTH_FORGET_PASSWORD = "/api/v1/auth/forgot-password";
    public static final String AUTH_RESET_PASSWORD = "/api/v1/auth/reset-password";
    public static final String AUTH_RESEND_VERIFICATION = "/api/v1/auth/resend-verification";
    public static final String SWAGGER_UI = "/swagger-ui.html";
    public static final String SWAGGER_UI_RESOURCES = "/swagger-ui/**";
    public static final String OPEN_API_DOCS = "/v3/api-docs/**";

    private SecurityConstants() {
    }
}
