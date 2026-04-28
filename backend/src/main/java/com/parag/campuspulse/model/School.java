package com.parag.campuspulse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "schools")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    // e.g. Engineering, Sciences, Management, Humanities
    @Column(nullable = false, length = 100)
    private String faculty;

    public School() {}

    public School(String code, String name, String faculty) {
        this.code = code;
        this.name = name;
        this.faculty = faculty;
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

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
}