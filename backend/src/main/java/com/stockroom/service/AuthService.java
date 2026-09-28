package com.stockroom.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockroom.dto.AuthDto;
import com.stockroom.exception.ConflictException;
import com.stockroom.exception.ResourceNotFoundException;
import com.stockroom.model.InventoryUser;
import com.stockroom.model.PasswordResetOtp;
import com.stockroom.repository.InventoryUserRepository;
import com.stockroom.repository.PasswordResetOtpRepository;

@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DEMO_ADMIN_USERNAME = "admin";
    private static final String DEMO_ADMIN_PASSWORD = "1234";
    private final InventoryUserRepository users;
    private final PasswordResetOtpRepository otps;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final boolean exposeOtpCode;

    public AuthService(InventoryUserRepository users, PasswordResetOtpRepository otps,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       @Value("${app.auth.otp.expose-code:false}") boolean exposeOtpCode) {
        this.users = users;
        this.otps = otps;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.exposeOtpCode = exposeOtpCode;
    }

    @Transactional
    public AuthDto.AuthResponse signup(AuthDto.SignupRequest request) {
        throw new ConflictException("Account registration is disabled until database-backed user setup is configured");
    }

    @Transactional(readOnly = true)
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        if (DEMO_ADMIN_USERNAME.equalsIgnoreCase(request.email().trim())
                && DEMO_ADMIN_PASSWORD.equals(request.password())) {
            return new AuthDto.AuthResponse(jwtService.createToken(DEMO_ADMIN_USERNAME),
                    new AuthDto.UserDto(0L, "Demo Administrator", DEMO_ADMIN_USERNAME, "INVENTORY_MANAGER"));
        }
        if (!request.email().contains("@")) {
            throw new BadCredentialsException("Invalid login ID or password");
        }
        InventoryUser user = users.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return tokenResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthDto.UserDto currentUser(String email) {
        if (DEMO_ADMIN_USERNAME.equalsIgnoreCase(email)) {
            return new AuthDto.UserDto(0L, "Demo Administrator", DEMO_ADMIN_USERNAME, "INVENTORY_MANAGER");
        }
        return toUser(users.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User account not found")));
    }

    @Transactional
    public AuthDto.UserDto updateProfile(String email, AuthDto.UpdateProfileRequest request) {
        if (DEMO_ADMIN_USERNAME.equalsIgnoreCase(email)) {
            throw new ConflictException("The built-in demo administrator profile is fixed");
        }
        InventoryUser user = users.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));
        if (request.name() != null && !request.name().isBlank()) user.setName(request.name().trim());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return toUser(users.save(user));
    }

    @Transactional
    public AuthDto.OtpRequestResult requestPasswordReset(AuthDto.OtpRequest request) {
        var user = users.findByEmailIgnoreCase(normalizeEmail(request.email()));
        if (user.isEmpty()) return new AuthDto.OtpRequestResult("If the account exists, a reset code has been issued", null);

        LocalDateTime now = LocalDateTime.now();
        otps.findByUser_IdAndConsumedAtIsNull(user.get().getId()).forEach(active -> active.setConsumedAt(now));
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        PasswordResetOtp otp = new PasswordResetOtp();
        otp.setUser(user.get());
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setCreatedAt(now);
        otp.setExpiresAt(now.plusMinutes(10));
        otps.save(otp);
        return new AuthDto.OtpRequestResult("If the account exists, a reset code has been issued",
                exposeOtpCode ? code : null);
    }

    @Transactional
    public void verifyPasswordReset(AuthDto.OtpVerifyRequest request) {
        InventoryUser user = users.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset code"));
        PasswordResetOtp otp = otps.findFirstByUser_IdAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset code"));
        if (otp.getExpiresAt().isBefore(LocalDateTime.now()) || !passwordEncoder.matches(request.otp(), otp.getCodeHash())) {
            throw new BadCredentialsException("Invalid or expired reset code");
        }
        otp.setConsumedAt(LocalDateTime.now());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        users.save(user);
        otps.save(otp);
    }

    private AuthDto.AuthResponse tokenResponse(InventoryUser user) {
        return new AuthDto.AuthResponse(jwtService.createToken(user.getEmail()), toUser(user));
    }

    private AuthDto.UserDto toUser(InventoryUser user) {
        return new AuthDto.UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}