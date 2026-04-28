package com.parag.campuspulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean enabled = true;

    // forces user to change their default password before they can log in
    @Column(nullable = false)
    private Boolean mustChangePassword = true;

    private LocalDateTime lastPasswordChange;

    // soft delete timestamp - null means the account is active
    private LocalDateTime deletedAt;

    @Column(name = "created_by")
    private Long createdBy;

    // student-specific fields below - null for non-students

    // extracted from email, e.g. "23bcs060" from 23bcs060@smvdu.ac.in
    private String entryNumber;

    private Integer admissionYear;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Program program;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    private Integer currentYear;

    @Column(name = "alumni", nullable = false)
    private Boolean isAlumni = false;

    // for integrated programs where the student left before full duration
    @Column(nullable = false)
    private Boolean optedEarlyExit = false;

    public User() {
        this.createdAt = LocalDateTime.now();
        this.enabled = true;
        this.isAlumni = false;
        this.optedEarlyExit = false;
        this.mustChangePassword = true;
    }

    public User(String email, String password, UserRole role) {
        this();
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean isActive() {
        return enabled && deletedAt == null;
    }

    public void deactivate() {
        this.enabled = false;
        this.deletedAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.enabled = true;
        this.deletedAt = null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public LocalDateTime getLastPasswordChange() { return lastPasswordChange; }
    public void setLastPasswordChange(LocalDateTime lastPasswordChange) {
        this.lastPasswordChange = lastPasswordChange;
    }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }

    public Integer getAdmissionYear() {
        return admissionYear;
    }

    public void setAdmissionYear(Integer admissionYear) {
        this.admissionYear = admissionYear;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public Integer getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(Integer currentYear) {
        this.currentYear = currentYear;
    }

    public Boolean getIsAlumni() {
        return isAlumni;
    }

    public void setIsAlumni(Boolean isAlumni) {
        this.isAlumni = isAlumni;
    }

    public Boolean getOptedEarlyExit() {
        return optedEarlyExit;
    }

    public void setOptedEarlyExit(Boolean optedEarlyExit) {
        this.optedEarlyExit = optedEarlyExit;
    }
}
