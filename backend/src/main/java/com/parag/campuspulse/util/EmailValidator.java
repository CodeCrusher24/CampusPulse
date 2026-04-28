package com.parag.campuspulse.util;

/**
 * Email validation utility for SMVDU domain.
 */
public class EmailValidator {

    private static final String SMVDU_DOMAIN = "@smvdu.ac.in";

    /**
     * Validate that email ends with @smvdu.ac.in
     */
    public static void validate(String email) {
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email cannot be empty");
        }

        if (!email.toLowerCase().endsWith(SMVDU_DOMAIN)) {
            throw new RuntimeException(
                    "Email must end with " + SMVDU_DOMAIN
            );
        }

        // Check basic format
        if (!email.contains("@") || email.indexOf("@") == 0) {
            throw new RuntimeException("Invalid email format");
        }
    }

    /**
     * Check if email is valid
     */
    public static boolean isValid(String email) {
        try {
            validate(email);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Extract default password from email (part before @)
     */
    public static String getDefaultPassword(String email) {
        return email.split("@")[0];
    }
}