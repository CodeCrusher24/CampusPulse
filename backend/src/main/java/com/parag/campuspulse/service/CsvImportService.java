package com.parag.campuspulse.service;

import com.parag.campuspulse.model.User;
import com.parag.campuspulse.model.UserRole;
import com.parag.campuspulse.repository.UserRepository;
import com.parag.campuspulse.util.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing users from CSV file.
 *
 * CSV Format: Just emails, one per line
 * Example:
 * 23bcs060@smvdu.ac.in
 * dean@smvdu.ac.in
 * prishthabhoomi.bca@smvdu.ac.in
 */
@Service
public class CsvImportService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentParserService studentParserService;

    @Autowired
    public CsvImportService(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            StudentParserService studentParserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.studentParserService = studentParserService;
    }

    /**
     * Import users from CSV file.
     *
     * Process:
     * 1. Parse CSV and validate all emails
     * 2. Check for duplicates
     * 3. If any errors -> throw exception (fail entire import)
     * 4. If all valid -> create all users (transaction)
     *
     * @param file CSV file
     * @param createdBy Admin user ID who is importing
     * @return Import result with success/error details
     */
    @Transactional
    public CsvImportResult importUsers(MultipartFile file, Long createdBy) {
        List<String> emails = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            // ==========================================
            // STEP 1: PARSE CSV
            // ==========================================

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream())
            );

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                // Skip header if exists
                if (lineNumber == 1 && line.toLowerCase().contains("email")) {
                    continue;
                }

                emails.add(line);
            }

            reader.close();

            // ==========================================
            // STEP 2: VALIDATE ALL EMAILS
            // ==========================================

            for (int i = 0; i < emails.size(); i++) {
                String email = emails.get(i);
                int csvLine = i + 1;

                // Validate email format and domain
                try {
                    EmailValidator.validate(email);
                } catch (RuntimeException e) {
                    errors.add("Line " + csvLine + ": " + e.getMessage());
                    continue;
                }

                // Check for duplicates in CSV itself
                for (int j = i + 1; j < emails.size(); j++) {
                    if (emails.get(j).equalsIgnoreCase(email)) {
                        errors.add("Line " + csvLine + ": Duplicate email in CSV - " + email);
                        break;
                    }
                }

                // Check if already exists in database
                if (userRepository.existsByEmail(email)) {
                    errors.add("Line " + csvLine + ": Email already exists in database - " + email);
                }
            }

            // ==========================================
            // STEP 3: IF ERRORS, FAIL ENTIRE IMPORT
            // ==========================================

            if (!errors.isEmpty()) {
                return new CsvImportResult(false, 0, errors);
            }

            // ==========================================
            // STEP 4: CREATE ALL USERS
            // ==========================================

            List<User> createdUsers = new ArrayList<>();

            for (String email : emails) {
                User user = createUserFromEmail(email, createdBy);
                createdUsers.add(userRepository.save(user));
            }

            // ==========================================
            // STEP 5: RETURN SUCCESS
            // ==========================================

            List<String> successMessages = new ArrayList<>();
            successMessages.add("Successfully imported " + createdUsers.size() + " users");

            return new CsvImportResult(true, createdUsers.size(), successMessages);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        }
    }

    /**
     * Create user from email.
     * Auto-detect role based on email pattern.
     */
    private User createUserFromEmail(String email, Long createdBy) {
        // Generate default password (email prefix)
        String defaultPassword = EmailValidator.getDefaultPassword(email);
        String hashedPassword = passwordEncoder.encode(defaultPassword);

        // Detect role
        UserRole role;
        if (studentParserService.isStudentEmail(email)) {
            role = UserRole.STUDENT;
        } else {
            role = UserRole.EVENT_COORDINATOR;
        }

        // Create user
        User user = new User(email, hashedPassword, role);
        user.setMustChangePassword(true);
        user.setCreatedBy(createdBy);

        // If student, parse and fill data
        if (role == UserRole.STUDENT) {
            studentParserService.parseAndFillStudentData(user);
        }

        return user;
    }

    /**
     * Result of CSV import operation
     */
    public static class CsvImportResult {
        private boolean success;
        private int usersCreated;
        private List<String> messages;

        public CsvImportResult(boolean success, int usersCreated, List<String> messages) {
            this.success = success;
            this.usersCreated = usersCreated;
            this.messages = messages;
        }

        public boolean isSuccess() { return success; }
        public int getUsersCreated() { return usersCreated; }
        public List<String> getMessages() { return messages; }
    }
}