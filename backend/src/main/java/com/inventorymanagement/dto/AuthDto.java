package com.inventorymanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class AuthDto {

    private AuthDto() {
    }

    public record SignupRequest(@NotBlank(message = "Name is required") String name,
                                @NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email,
                                @NotBlank(message = "Password is required")
                                @Size(min = 8, message = "Password must be at least 8 characters") String password) {
    }

    public record LoginRequest(@NotBlank(message = "Login ID is required") String email,
                               @NotBlank(message = "Password is required") String password) {
    }

    public record OtpRequest(@NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email) {
    }

    public record OtpVerifyRequest(@NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email,
                                   @NotBlank(message = "OTP is required") String otp,
                                   @JsonProperty("new_password")
                                   @NotBlank(message = "New password is required")
                                   @Size(min = 8, message = "Password must be at least 8 characters") String newPassword) {
    }

    public record UpdateProfileRequest(String name,
                                       @Size(min = 8, message = "Password must be at least 8 characters") String password) {
    }

    public record UserDto(Long id, String name, String email, String role) {
    }

    public record AuthResponse(String token, UserDto user) {
    }

    public record OtpRequestResult(String message, @JsonProperty("debug_otp") String debugOtp) {
    }
}