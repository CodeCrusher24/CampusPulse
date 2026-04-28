package com.parag.campuspulse.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VenueType type;

    private Integer capacity;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private VenueApprovalAuthority approvalAuthority;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 500)
    private String amenities;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Venue() {
        this.createdAt = LocalDateTime.now();
        this.enabled = true;
    }

    public Venue(String name, VenueType type, Integer capacity) {
        this();
        this.name = name;
        this.type = type;
        this.capacity = capacity;
    }

    // MANAGED venues need approval before booking, OPEN_SPACE ones don't
    public boolean needsApproval() { return type == VenueType.MANAGED; }

    // central venues aren't tied to any school
    public boolean isCentralVenue() { return school == null && type == VenueType.MANAGED; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public VenueType getType() { return type; }
    public void setType(VenueType type) { this.type = type; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }
    public VenueApprovalAuthority getApprovalAuthority() { return approvalAuthority; }
    public void setApprovalAuthority(VenueApprovalAuthority approvalAuthority) { this.approvalAuthority = approvalAuthority; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}