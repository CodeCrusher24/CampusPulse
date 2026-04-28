package com.parag.campuspulse.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "event_categories")
public class EventCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(length = 20)
    private String colorCode;

    @Column(length = 50)
    private String icon;

    public EventCategory() {}

    public EventCategory(String name, String description, String colorCode, String icon) {
        this.name = name;
        this.description = description;
        this.colorCode = colorCode;
        this.icon = icon;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}