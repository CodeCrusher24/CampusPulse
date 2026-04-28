package com.parag.campuspulse.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

// Events go through a dual approval workflow:
// 1. venue authority approves the venue booking
// 2. faculty/HOD approves the event itself
// Both must pass for status to move to APPROVED.
// Admins skip this entirely - their events go straight to PUBLISHED.
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_date",       columnList = "event_date_time"),
        @Index(name = "idx_event_status",     columnList = "status"),
        @Index(name = "idx_primary_category", columnList = "primary_category_id"),
        @Index(name = "idx_status_date",      columnList = "status, event_date_time")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "event_date_time", nullable = false)
    private LocalDateTime eventDateTime;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;

    // used when the event isn't at a registered venue
    @Column(name = "custom_location", length = 200)
    private String customLocation;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer registeredCount = 0;

    @Column(nullable = false)
    private Integer waitlistCount = 0;

    private LocalDateTime registrationDeadline;

    @ManyToOne
    @JoinColumn(name = "primary_category_id", nullable = false)
    private EventCategory primaryCategory;

    @ManyToOne
    @JoinColumn(name = "secondary_category_id")
    private EventCategory secondaryCategory;

    @ManyToOne
    @JoinColumn(name = "tertiary_category_id")
    private EventCategory tertiaryCategory;

    @Column(length = 500)
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    // dual approval gate 1: venue booking approved?
    // auto-set to true for OPEN_SPACE venues and admin-created events
    @Column(nullable = false)
    private Boolean venueApproved = false;

    @ManyToOne
    @JoinColumn(name = "venue_approved_by")
    private User venueApprovedBy;

    private LocalDateTime venueApprovedAt;

    // dual approval gate 2: event content approved by faculty/HOD?
    // auto-set to true for admin-created events
    @Column(nullable = false)
    private Boolean eventApproved = false;

    @ManyToOne
    @JoinColumn(name = "event_approved_by")
    private User eventApprovedBy;

    private LocalDateTime eventApprovedAt;

    // filled by whichever authority rejects the event
    @Column(length = 500)
    private String rejectionReason;

    // stores the second approver (the one who completed both gates)
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private LocalDateTime approvedAt;

    @Column(length = 500)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public Event() {
        this.createdAt = LocalDateTime.now();
        this.status = EventStatus.DRAFT;
        this.registeredCount = 0;
        this.waitlistCount = 0;
        this.venueApproved = false;
        this.eventApproved = false;
    }

    public boolean hasVenue()          { return venue != null; }
    public boolean hasCustomLocation() { return customLocation != null && !customLocation.isEmpty(); }

    public String getLocationDisplay() {
        if (venue != null)           return venue.getName();
        if (customLocation != null)  return customLocation;
        return "TBD";
    }

    public boolean isFull() { return registeredCount >= capacity; }

    public boolean isRegistrationOpen() {
        if (status != EventStatus.PUBLISHED) return false;
        if (registrationDeadline != null && LocalDateTime.now().isAfter(registrationDeadline)) return false;
        return true;
    }

    public boolean canBeEdited()    { return status == EventStatus.DRAFT || status == EventStatus.REJECTED; }
    public boolean canBeSubmitted() { return status == EventStatus.DRAFT; }
    public boolean canBeApproved()  { return status == EventStatus.PENDING; }
    public boolean canBePublished() { return status == EventStatus.APPROVED; }

    // true when both approval gates have passed
    public boolean isBothApproved()  { return Boolean.TRUE.equals(venueApproved) && Boolean.TRUE.equals(eventApproved); }

    public boolean isOpenSpaceVenue() {
        return venue != null && venue.getType() == VenueType.OPEN_SPACE;
    }

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }
    public String getTitle()                       { return title; }
    public void setTitle(String title)             { this.title = title; }
    public String getDescription()                 { return description; }
    public void setDescription(String d)           { this.description = d; }
    public LocalDateTime getEventDateTime()        { return eventDateTime; }
    public void setEventDateTime(LocalDateTime dt) { this.eventDateTime = dt; }
    public Venue getVenue()                        { return venue; }
    public void setVenue(Venue venue)              { this.venue = venue; }
    public String getCustomLocation()              { return customLocation; }
    public void setCustomLocation(String cl)       { this.customLocation = cl; }
    public Integer getCapacity()                   { return capacity; }
    public void setCapacity(Integer c)             { this.capacity = c; }
    public Integer getRegisteredCount()            { return registeredCount; }
    public void setRegisteredCount(Integer rc)     { this.registeredCount = rc; }
    public Integer getWaitlistCount()              { return waitlistCount; }
    public void setWaitlistCount(Integer wc)       { this.waitlistCount = wc; }
    public LocalDateTime getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDateTime rd) { this.registrationDeadline = rd; }
    public EventCategory getPrimaryCategory()              { return primaryCategory; }
    public void setPrimaryCategory(EventCategory c)        { this.primaryCategory = c; }
    public EventCategory getSecondaryCategory()            { return secondaryCategory; }
    public void setSecondaryCategory(EventCategory c)      { this.secondaryCategory = c; }
    public EventCategory getTertiaryCategory()             { return tertiaryCategory; }
    public void setTertiaryCategory(EventCategory c)       { this.tertiaryCategory = c; }
    public String getTags()                        { return tags; }
    public void setTags(String tags)               { this.tags = tags; }
    public EventStatus getStatus()                 { return status; }
    public void setStatus(EventStatus status)      { this.status = status; }

    public Boolean getVenueApproved()                      { return venueApproved; }
    public void setVenueApproved(Boolean venueApproved)    { this.venueApproved = venueApproved; }
    public User getVenueApprovedBy()                       { return venueApprovedBy; }
    public void setVenueApprovedBy(User venueApprovedBy)   { this.venueApprovedBy = venueApprovedBy; }
    public LocalDateTime getVenueApprovedAt()              { return venueApprovedAt; }
    public void setVenueApprovedAt(LocalDateTime t)        { this.venueApprovedAt = t; }

    public Boolean getEventApproved()                      { return eventApproved; }
    public void setEventApproved(Boolean eventApproved)    { this.eventApproved = eventApproved; }
    public User getEventApprovedBy()                       { return eventApprovedBy; }
    public void setEventApprovedBy(User eventApprovedBy)   { this.eventApprovedBy = eventApprovedBy; }
    public LocalDateTime getEventApprovedAt()              { return eventApprovedAt; }
    public void setEventApprovedAt(LocalDateTime t)        { this.eventApprovedAt = t; }

    public String getRejectionReason()                     { return rejectionReason; }
    public void setRejectionReason(String r)               { this.rejectionReason = r; }
    public User getApprovedBy()                            { return approvedBy; }
    public void setApprovedBy(User approvedBy)             { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt()                   { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt)    { this.approvedAt = approvedAt; }
    public String getImageUrl()                            { return imageUrl; }
    public void setImageUrl(String imageUrl)               { this.imageUrl = imageUrl; }
    public User getCreatedBy()                             { return createdBy; }
    public void setCreatedBy(User createdBy)               { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt()                    { return createdAt; }
    public LocalDateTime getUpdatedAt()                    { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)      { this.updatedAt = updatedAt; }
    public Long getVersion()                               { return version; }
}