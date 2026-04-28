package com.parag.campuspulse.service;

import com.parag.campuspulse.model.School;
import com.parag.campuspulse.model.Program;
import com.parag.campuspulse.model.User;
import com.parag.campuspulse.repository.SchoolRepository;
import com.parag.campuspulse.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



/**
 * Parses student email to extract admission info and auto-fills user data.
 *
 * Email format: YYcccNNN@smvdu.ac.in
 * Examples:
 * - 23bcs060@smvdu.ac.in → 2023, CSE, BTech CS
 * - 24bbb012@smvdu.ac.in → 2024, Business, BBA (4-year)
 * - 25ibb045@smvdu.ac.in → 2025, Business, Integrated BBA-MBA
 */
@Service
public class StudentParserService {

    private final SchoolRepository schoolRepository;
    private final ProgramRepository programRepository;

    /**
     * Current session year from application.properties.
     *
     * For session 2025-2026 → value should be 2026
     * For session 2026-2027 → value should be 2027
     *
     * Admin updates this value when new session starts.
     */
    @Value("${app.session.current-year}")
    private int currentSessionYear;
    @Autowired
    public StudentParserService(SchoolRepository schoolRepository,
                                ProgramRepository programRepository) {
        this.schoolRepository = schoolRepository;
        this.programRepository = programRepository;
    }

    /**
     * Check if email matches student pattern.
     * Pattern: 2 digits + 3 letters + 3 digits
     */
    public boolean isStudentEmail(String email) {
        String username = email.split("@")[0];
        return username.matches("\\d{2}[a-zA-Z]{3}\\d{3}");
    }

    /**
     * Parse email and fill all student data.
     */
    public void parseAndFillStudentData(User user) {
        String email = user.getEmail();
        String username = email.split("@")[0];

        // Validate format
        if (!isStudentEmail(email)) {
            throw new RuntimeException("Invalid student email format: " + email);
        }

        // ==========================================
        // PARSE EMAIL COMPONENTS
        // ==========================================

        // Extract parts: YYcccNNN
        String yearCode = username.substring(0, 2);          // 23
        String programCode = username.substring(2, 5);       // bcs
        String rollNumber = username.substring(5);           // 060

        // Convert year code to full year
        int admissionYear = 2000 + Integer.parseInt(yearCode);  // 2023

        // ==========================================
        // LOOKUP PROGRAM
        // ==========================================

        Program program = programRepository.findByCode(programCode.toLowerCase())
                .orElseThrow(() -> new RuntimeException(
                        "Unknown program code: " + programCode
                ));
        // Validate program availability
        validateProgramAvailability(program, admissionYear, programCode);

        // Validate program is available for this admission year
        if (program.getAvailableFromYear() != null &&
                admissionYear < program.getAvailableFromYear()) {
            throw new RuntimeException(
                    "Program " + programCode + " not available for admission year " + admissionYear
            );
        }

        // ==========================================
        // GET SCHOOL
        // ==========================================

        School school = program.getSchool();

        // ==========================================
        // CALCULATE CURRENT YEAR
        // ==========================================

        int currentYear = calculateCurrentYear(admissionYear, program);

        // ==========================================
        // CALCULATE ALUMNI STATUS
        // ==========================================

        boolean isAlumni = calculateAlumniStatus(admissionYear, program, user);

        // ==========================================
        // POPULATE USER FIELDS
        // ==========================================

        user.setEntryNumber(username);
        user.setAdmissionYear(admissionYear);
        user.setProgram(program);
        user.setSchool(school);
        user.setCurrentYear(currentYear);
        user.setIsAlumni(isAlumni);

        System.out.println("📧 Parsed: " + email);
        System.out.println("   Program: " + program.getName());
        System.out.println("   School: " + school.getName());
        System.out.println("   Year: " + currentYear + " (Session " + currentSessionYear + ")");
    }

    /**
     * Validate if program is available for admission year.
     */
    private void validateProgramAvailability(Program program, int admissionYear, String programCode) {
        // Check availableFromYear
        if (program.getAvailableFromYear() != null &&
                admissionYear < program.getAvailableFromYear()) {
            throw new RuntimeException(
                    "Program " + programCode + " not available for admission year " + admissionYear +
                            ". Started from " + program.getAvailableFromYear() + " onwards."
            );
        }
        // Check availableUntilYear
        if (program.getAvailableUntilYear() != null &&
                admissionYear > program.getAvailableUntilYear()) {
            throw new RuntimeException(
                    "Program " + programCode + " not available for admission year " + admissionYear +
                            ". Last batch was " + program.getAvailableUntilYear() + "."
            );
        }
    }
        /**
         * Calculate current academic year using session year from properties.
         *
         * Formula: currentSessionYear - admissionYear
         *
         * Examples (with currentSessionYear = 2026, meaning session 2025-2026):
         *
         * Student 23bcs060 (admitted 2023):
         * yearsPassed = 2026 - 2023 + 1 = 4
         * But program duration is 4, so: currentYear = min(4, 4) = 4
         * Student is in 4th year ❌ WAIT, this is wrong!
         *
         * CORRECT FORMULA: currentSessionYear - admissionYear
         *
         * Student 23bcs060 (admitted 2023):
         * yearsPassed = 2026 - 2023 = 3
         * currentYear = min(3, 4) = 3 ✅ CORRECT!
         *
         * When session changes to 2026-2027:
         * currentSessionYear = 2027 (admin updates application.properties)
         * yearsPassed = 2027 - 2023 = 4
         * currentYear = min(4, 4) = 4 ✅ CORRECT!
         */
    private int calculateCurrentYear(int admissionYear, Program program) {
        // Calculate years passed since admission
        // Session year - admission year gives us the correct year
        int yearsPassed = currentSessionYear - admissionYear;

        // Cap at program duration
        int studentYear = Math.min(yearsPassed, program.getDurationYears());

        // Ensure at least year 1 (for new admissions in current session)
        studentYear = Math.max(studentYear, 1);

        return studentYear;
    }

    /**
     * Calculate if student has graduated (alumni status).
     *
     * Considers:
     * 1. Regular completion (years passed > program duration)
     * 2. Early exit from integrated programs
     */
    private boolean calculateAlumniStatus(int admissionYear, Program program, User user) {
        int yearsPassed = currentSessionYear - admissionYear;

        // Check if opted for early exit (integrated programs)
        if (user.getOptedEarlyExit() && program.getHasEarlyExit()) {
            // Alumni if passed early exit year
            return yearsPassed > program.getEarlyExitYear();
        }

        // Regular: alumni if passed program duration
        return yearsPassed > program.getDurationYears();
    }
}