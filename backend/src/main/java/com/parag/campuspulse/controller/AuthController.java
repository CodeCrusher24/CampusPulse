package com.parag.campuspulse.controller;

import com.parag.campuspulse.dto.ApiResponse;
import com.parag.campuspulse.dto.JwtResponse;
import com.parag.campuspulse.model.User;
import com.parag.campuspulse.service.UserService;
import com.parag.campuspulse.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller.
 *
 * There is NO signup/register endpoint on CampusPulse.
 * All accounts are created by a SYSTEM_ADMIN via AdminController.
 *
 * Flow for a new user:
 *  1. Admin creates their account → default password = email prefix
 *  2. User calls POST /api/auth/change-password with their default password
 *  3. User calls POST /api/auth/login — login is now allowed
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and mandatory first-login password change. No self-registration.")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil     jwtUtil;

    // ── Login ─────────────────────────────────────────────────

    @Operation(
            summary = "Login",
            description = """
            Returns a JWT token on success.

            **Important:** If your account was just created by an admin, your default
            password is the part before the `@` in your email (e.g. `23bcs060`).
            You must call `/api/auth/change-password` BEFORE this endpoint will work —
            login is blocked until the default password is changed.
            """
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().toString(),
                    user.getId()
            );
            return ResponseEntity.ok(new JwtResponse(token, user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Change password ───────────────────────────────────────

    @Operation(
            summary = "Change password",
            description = """
            Used for two scenarios:
            1. **First login** — change the admin-assigned default password so login is unlocked
            2. **Regular** — change password at any time

            Password requirements: 8–20 characters, at least one uppercase, one lowercase,
            one digit, and one special character.
            """
    )
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(
                    request.getEmail(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(new ApiResponse.Success("Password changed successfully. You can now log in."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Request DTOs ──────────────────────────────────────────
    // Only REQUEST DTOs live here — responses use the shared ApiResponse class

    public static class LoginRequest {
        private String email;
        private String password;
        public String getEmail()            { return email; }
        public void setEmail(String email)  { this.email = email; }
        public String getPassword()         { return password; }
        public void setPassword(String pwd) { this.password = pwd; }
    }

    public static class ChangePasswordRequest {
        private String email;
        private String oldPassword;
        private String newPassword;
        public String getEmail()                       { return email; }
        public void setEmail(String email)             { this.email = email; }
        public String getOldPassword()                 { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword()                 { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}