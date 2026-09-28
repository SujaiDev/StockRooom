package com.stockroom.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stockroom.dto.ApiResponse;
import com.stockroom.dto.AuthDto;
import com.stockroom.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> signup(@Valid @RequestBody AuthDto.SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(auth.signup(request)));
    }

    @PostMapping("/login")
    public ApiResponse<AuthDto.AuthResponse> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return ApiResponse.ok(auth.login(request));
    }

    @PostMapping("/otp/request")
    public ApiResponse<AuthDto.OtpRequestResult> requestOtp(@Valid @RequestBody AuthDto.OtpRequest request) {
        return ApiResponse.ok(auth.requestPasswordReset(request));
    }

    @PostMapping("/otp/verify")
    public ApiResponse<Void> verifyOtp(@Valid @RequestBody AuthDto.OtpVerifyRequest request) {
        auth.verifyPasswordReset(request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    public ApiResponse<AuthDto.UserDto> me(Authentication authentication) {
        return ApiResponse.ok(auth.currentUser(authentication.getName()));
    }

    @PutMapping("/me")
    public ApiResponse<AuthDto.UserDto> updateMe(Authentication authentication,
                                                 @Valid @RequestBody AuthDto.UpdateProfileRequest request) {
        return ApiResponse.ok(auth.updateProfile(authentication.getName(), request));
    }
}