package com.parag.campuspulse.util;

/**
 * Password validation utility.
 *
 * Requirements:
 * - Minimum 8 characters, maximum 20
 * - At least 1 uppercase letter
 * - At least 1 lowercase letter
 * - At least 1 number
 * - At least 1 special character
 */
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 20;

    /**
     * Validate password against complexity requirements.
     *
     * @param password Password to validate
     * @throws RuntimeException if password doesn't meet requirements
     */
    public static void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }

        // Check length
        if (password.length() < MIN_LENGTH) {
            throw new RuntimeException(
                    "Password must be at least " + MIN_LENGTH + " characters long"
            );
        }

        if (password.length() > MAX_LENGTH) {
            throw new RuntimeException(
                    "Password must not exceed " + MAX_LENGTH + " characters"
            );
        }

        // Check for uppercase
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException(
                    "Password must contain at least one uppercase letter"
            );
        }

        // Check for lowercase
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException(
                    "Password must contain at least one lowercase letter"
            );
        }

        // Check for digit
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException(
                    "Password must contain at least one number"
            );
        }

        // Check for special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new RuntimeException(
                    "Password must contain at least one special character (!@#$%^&* etc.)"
            );
        }
    }

    /**
     * Check if password is valid (doesn't throw exception)
     */
    public static boolean isValid(String password) {
        try {
            validate(password);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Get validation error message (for user-friendly display)
     */
    public static String getValidationError(String password) {
        try {
            validate(password);
            return null;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }
}