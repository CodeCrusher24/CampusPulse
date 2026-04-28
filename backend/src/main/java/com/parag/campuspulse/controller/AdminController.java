package com.parag.campuspulse.controller;

import com.parag.campuspulse.dto.AdminDTOs.*;
import com.parag.campuspulse.dto.ApiResponse;
import com.parag.campuspulse.model.User;
import com.parag.campuspulse.model.UserRole;
import com.parag.campuspulse.service.AdminService;
import com.parag.campuspulse.service.CsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin — User Management", description = "SYSTEM_ADMIN only. Create, manage, and deactivate user accounts.")
public class AdminController {

    private final AdminService adminService;
    private final CsvImportService csvImportService;

    @Autowired
    public AdminController(AdminService adminService,
                           CsvImportService csvImportService) {
        this.adminService = adminService;
        this.csvImportService = csvImportService;
    }

    // ── Auth check ───────────────────────────────────────────

    private User getAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new RuntimeException("Unauthorized");
        }
        User admin = (User) auth.getPrincipal();
        if (admin.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new RuntimeException("Only admins allowed");
        }
        return admin;
    }

    // ── Create user ──────────────────────────────────────────

    @Operation(
            summary = "Create a single user account",
            description = """
            Auto-detects role from email pattern:
            - Student pattern (e.g. `23bcs060@smvdu.ac.in`) → STUDENT
            - Any other SMVDU email → EVENT_COORDINATOR

            Default password = email prefix (e.g. `23bcs060`).
            User must change it before logging in.
            """
    )
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            getAdmin();
            User user = adminService.createUser(request.getEmail());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Create admin ─────────────────────────────────────────

    @Operation(
            summary = "Create a SYSTEM_ADMIN account",
            description = "Student emails are rejected — only non-student SMVDU emails can be made admin."
    )
    @PostMapping("/users/admin")
    public ResponseEntity<?> createAdmin(@RequestBody CreateAdminRequest request) {
        try {
            getAdmin();
            User admin = adminService.createAdmin(request.getEmail());
            return ResponseEntity.ok(admin);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Bulk CSV import ──────────────────────────────────────

    @Operation(
            summary = "Bulk import users from CSV",
            description = """
            CSV format: one email per line (header row optional).
            All emails must end with `@smvdu.ac.in`.
            If any email is invalid or already exists, the entire import is rejected.
            """
    )
    @PostMapping("/users/bulk")
    public ResponseEntity<?> bulkImport(@RequestParam("file") MultipartFile file) {
        try {
            User admin = getAdmin();

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse.Error("File is empty"));
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !filename.endsWith(".csv")) {
                return ResponseEntity.badRequest().body(new ApiResponse.Error("File must be CSV format"));
            }

            CsvImportService.CsvImportResult result =
                    csvImportService.importUsers(file, admin.getId());

            if (result.isSuccess()) {
                return ResponseEntity.ok(new CsvImportResponse(
                        true,
                        result.getUsersCreated(),
                        "Successfully imported " + result.getUsersCreated() + " users"
                ));
            } else {
                return ResponseEntity.badRequest().body(new CsvImportResponse(
                        false,
                        0,
                        "Import failed: " + String.join(", ", result.getMessages())
                ));
            }

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Get all users ────────────────────────────────────────

    @Operation(summary = "Get all users (paginated)")
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        getAdmin();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    // ── Get single user ──────────────────────────────────────

    @Operation(summary = "Get a user by ID")
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            getAdmin();
            return ResponseEntity.ok(adminService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Change role ──────────────────────────────────────────

    @Operation(
            summary = "Change a user's role",
            description = """
            Allowed transitions:
            - EVENT_COORDINATOR → FACULTY_AUTHORITY (promote)
            - FACULTY_AUTHORITY → EVENT_COORDINATOR (demote)

            Not allowed: touching STUDENT or SYSTEM_ADMIN accounts.
            """
    )
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long id,
            @RequestBody ChangeRoleRequest request) {
        try {
            getAdmin();
            User user = adminService.changeUserRole(id, request.getNewRole());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Reset password ───────────────────────────────────────

    @Operation(
            summary = "Reset a user's password to default",
            description = "Resets to email-prefix password and sets mustChangePassword = true."
    )
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        try {
            getAdmin();
            adminService.resetPasswordToDefault(id);
            return ResponseEntity.ok(new ApiResponse.Success("Password reset. User must change password before next login."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Deactivate ───────────────────────────────────────────

    @Operation(
            summary = "Deactivate a user account",
            description = "Soft delete — account is disabled but not removed from the database."
    )
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            getAdmin();
            adminService.deactivateUser(id);
            return ResponseEntity.ok(new ApiResponse.Success("User deactivated successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Reactivate ───────────────────────────────────────────

    @Operation(summary = "Reactivate a deactivated user account")
    @PutMapping("/users/{id}/reactivate")
    public ResponseEntity<?> reactivateUser(@PathVariable Long id) {
        try {
            getAdmin();
            adminService.reactivateUser(id);
            return ResponseEntity.ok(new ApiResponse.Success("User reactivated successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }
}