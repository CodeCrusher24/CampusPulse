package com.parag.campuspulse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "programs")
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // short code like bcs, bec, ibb
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // UG, PG, or INTEGRATED
    @Column(nullable = false, length = 20)
    private String programType;

    @Column(nullable = false)
    private Integer durationYears;

    // for integrated programs that allow exit before full duration
    @Column(nullable = false)
    private Boolean hasEarlyExit = false;

    private Integer earlyExitYear;

    // null means available for all years, otherwise the first year this program was offered
    private Integer availableFromYear;

    // null means still active, otherwise the last admission year
    private Integer availableUntilYear;

    public Program() {}

    public Program(String code, String name, School school,
                   String programType, Integer durationYears) {
        this.code = code;
        this.name = name;
        this.school = school;
        this.programType = programType;
        this.durationYears = durationYears;
        this.hasEarlyExit = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public String getProgramType() {
        return programType;
    }

    public void setProgramType(String programType) {
        this.programType = programType;
    }

    public Integer getDurationYears() {
        return durationYears;
    }

    public void setDurationYears(Integer durationYears) {
        this.durationYears = durationYears;
    }

    public Boolean getHasEarlyExit() {
        return hasEarlyExit;
    }

    public void setHasEarlyExit(Boolean hasEarlyExit) {
        this.hasEarlyExit = hasEarlyExit;
    }

    public Integer getEarlyExitYear() {
        return earlyExitYear;
    }

    public void setEarlyExitYear(Integer earlyExitYear) {
        this.earlyExitYear = earlyExitYear;
    }

    public Integer getAvailableFromYear() {
        return availableFromYear;
    }

    public void setAvailableFromYear(Integer availableFromYear) {
        this.availableFromYear = availableFromYear;
    }

    public Integer getAvailableUntilYear() { return availableUntilYear; }
    public void setAvailableUntilYear(Integer availableUntilYear) {
        this.availableUntilYear = availableUntilYear;
    }
}