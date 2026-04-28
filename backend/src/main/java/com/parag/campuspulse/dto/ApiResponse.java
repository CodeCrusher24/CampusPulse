package com.parag.campuspulse.dto;

/**
 * Shared API response wrappers used across all controllers.
 *
 * Before: ErrorResponse and SuccessResponse were copy-pasted as static
 * inner classes inside EventController, AuthController, AdminController,
 * and AdminDTOs — four separate duplicates.
 *
 * Now: import these once per controller and use them everywhere.
 *
 * Usage:
 *   return ResponseEntity.badRequest().body(new ApiResponse.Error("Something went wrong"));
 *   return ResponseEntity.ok(new ApiResponse.Success("Operation completed"));
 */
public class ApiResponse {

    /**
     * Returned when a request fails.
     * HTTP status is set by the controller (usually 400).
     *
     * JSON: { "error": "message here" }
     */
    public static class Error {
        private String error;

        public Error(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    /**
     * Returned when a request succeeds but there's no data payload to return.
     * Used for actions like delete, deactivate, password reset, etc.
     *
     * JSON: { "message": "message here" }
     */
    public static class Success {
        private String message;

        public Success(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}