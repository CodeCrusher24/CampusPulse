package com.parag.campuspulse.service;

import com.parag.campuspulse.model.User;
import com.parag.campuspulse.model.UserRole;
import com.parag.campuspulse.repository.UserRepository;
import com.parag.campuspulse.util.EmailValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentParserService studentParserService;

    @Autowired
    public AdminService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        StudentParserService studentParserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.studentParserService = studentParserService;
    }

    // 🔐 GET CURRENT USER FROM JWT
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new RuntimeException("Unauthorized");
        }

        return (User) auth.getPrincipal();
    }

    // ✅ CREATE USER
    @Transactional
    public User createUser(String email) {

        User admin = getCurrentUser();

        if (admin.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new RuntimeException("Only admins can create users");
        }

        EmailValidator.validate(email);

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        String defaultPassword = EmailValidator.getDefaultPassword(email);
        String hashedPassword = passwordEncoder.encode(defaultPassword);

        UserRole role = studentParserService.isStudentEmail(email)
                ? UserRole.STUDENT
                : UserRole.EVENT_COORDINATOR;

        User user = new User(email, hashedPassword, role);
        user.setMustChangePassword(true);
        user.setCreatedBy(admin.getId());

        if (role == UserRole.STUDENT) {
            studentParserService.parseAndFillStudentData(user);
        }

        return userRepository.save(user);
    }

    // ✅ CREATE ADMIN
    @Transactional
    public User createAdmin(String email) {

        User admin = getCurrentUser();

        if (admin.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new RuntimeException("Only admins can create admins");
        }

        EmailValidator.validate(email);

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        if (studentParserService.isStudentEmail(email)) {
            throw new RuntimeException("Student cannot be admin");
        }

        String defaultPassword = EmailValidator.getDefaultPassword(email);
        String hashedPassword = passwordEncoder.encode(defaultPassword);

        User user = new User(email, hashedPassword, UserRole.SYSTEM_ADMIN);
        user.setMustChangePassword(true);
        user.setCreatedBy(admin.getId());

        return userRepository.save(user);
    }

    // ✅ GET USERS
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // ✅ GET USER
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ ROLE CHANGE (FINAL LOGIC)
    @Transactional
    public User changeUserRole(Long userId, UserRole newRole) {

        User admin = getCurrentUser();

        if (admin.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new RuntimeException("Only admins can change roles");
        }

        User user = getUserById(userId);
        UserRole currentRole = user.getRole();

        // ❌ Cannot modify admin
        if (currentRole == UserRole.SYSTEM_ADMIN) {
            throw new RuntimeException("Cannot modify SYSTEM_ADMIN");
        }

        // ❌ Cannot assign admin
        if (newRole == UserRole.SYSTEM_ADMIN) {
            throw new RuntimeException("Cannot assign SYSTEM_ADMIN");
        }

        // ❌ Students locked
        if (currentRole == UserRole.STUDENT) {
            throw new RuntimeException("Students cannot be promoted");
        }

        // ✅ EC → FACULTY
        if (currentRole == UserRole.EVENT_COORDINATOR &&
                newRole == UserRole.FACULTY_AUTHORITY) {

            user.setRole(UserRole.FACULTY_AUTHORITY);
        }

        // ✅ FACULTY → EC (DEMOTE)
        else if (currentRole == UserRole.FACULTY_AUTHORITY &&
                newRole == UserRole.EVENT_COORDINATOR) {

            user.setRole(UserRole.EVENT_COORDINATOR);
        }

        else {
            throw new RuntimeException("Invalid role transition");
        }

        return userRepository.save(user);
    }

    // ✅ RESET PASSWORD
    @Transactional
    public void resetPasswordToDefault(Long userId) {
        User user = getUserById(userId);

        String defaultPassword = EmailValidator.getDefaultPassword(user.getEmail());
        String hashedPassword = passwordEncoder.encode(defaultPassword);

        user.setPassword(hashedPassword);
        user.setMustChangePassword(true);
        user.setLastPasswordChange(LocalDateTime.now());

        userRepository.save(user);
    }

    // ✅ DEACTIVATE
    @Transactional
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);

        if (!user.isActive()) {
            throw new RuntimeException("Already deactivated");
        }

        user.deactivate();
        userRepository.save(user);
    }

    // ✅ REACTIVATE
    @Transactional
    public void reactivateUser(Long userId) {
        User user = getUserById(userId);

        if (user.isActive()) {
            throw new RuntimeException("Already active");
        }

        user.reactivate();
        userRepository.save(user);
    }
}