package com.parag.campuspulse.dto;

import com.parag.campuspulse.model.UserRole;

public class AdminDTOs {

    public static class CreateUserRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class CreateAdminRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ChangeRoleRequest {
        private UserRole newRole;
        public UserRole getNewRole() { return newRole; }
        public void setNewRole(UserRole newRole) { this.newRole = newRole; }
    }

    public static class CsvImportResponse {
        private boolean success;
        private int usersCreated;
        private String message;

        public CsvImportResponse(boolean success, int usersCreated, String message) {
            this.success = success;
            this.usersCreated = usersCreated;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public int getUsersCreated() { return usersCreated; }
        public String getMessage() { return message; }
    }
}