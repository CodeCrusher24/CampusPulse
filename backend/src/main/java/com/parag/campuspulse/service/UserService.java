package com.parag.campuspulse.service;

import com.parag.campuspulse.model.User;
import com.parag.campuspulse.repository.UserRepository;
import com.parag.campuspulse.util.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * UserService handles authentication-related operations only.
 *
 * NOTE: User CREATION is handled exclusively by AdminService.
 * There is no self-registration on CampusPulse — all accounts are
 * provisioned by a SYSTEM_ADMIN, either individually or via CSV bulk import.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Login — verify credentials and return the User entity.
     *
     * The caller (AuthController) is responsible for generating the JWT token.
     *
     * Rules:
     *  - Account must be active (enabled + not soft-deleted)
     *  - Password must match
     *  - If mustChangePassword is true, login is BLOCKED until the user
     *    first changes their default password via /api/auth/change-password
     */
    public User login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is disabled. Contact your system administrator.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Block login until default password is changed
        if (user.getMustChangePassword()) {
            throw new RuntimeException(
                    "You must change your default password before logging in. " +
                            "Use POST /api/auth/change-password with your current default password."
            );
        }

        return user;
    }

    /**
     * Change password.
     *
     * Used in two scenarios:
     *  1. First login: user changes their admin-assigned default password
     *  2. Regular password change at any time
     *
     * After a successful change:
     *  - mustChangePassword is cleared → login is now allowed
     *  - lastPasswordChange is stamped
     */
    public void changePassword(String email, String oldPassword, String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is disabled.");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate complexity (min 8, max 20, upper + lower + digit + special)
        PasswordValidator.validate(newPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.setLastPasswordChange(LocalDateTime.now());

        userRepository.save(user);
    }
}