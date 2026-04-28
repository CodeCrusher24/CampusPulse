package com.parag.campuspulse.dto;

import java.time.LocalDateTime;

public class EventDTOs {

    public static class CreateEventRequest {
        private String title;
        private String description;
        private LocalDateTime eventDateTime;
        private LocalDateTime registrationDeadline;
        private Long venueId;
        private String customLocation;
        private Integer capacity;
        private Long primaryCategoryId;
        private Long secondaryCategoryId;
        private Long tertiaryCategoryId;
        private String tags;
        private String imageUrl;

        public String getTitle()                               { return title; }
        public void setTitle(String title)                     { this.title = title; }
        public String getDescription()                         { return description; }
        public void setDescription(String description)         { this.description = description; }
        public LocalDateTime getEventDateTime()                { return eventDateTime; }
        public void setEventDateTime(LocalDateTime dt)         { this.eventDateTime = dt; }
        public LocalDateTime getRegistrationDeadline()         { return registrationDeadline; }
        public void setRegistrationDeadline(LocalDateTime rd)  { this.registrationDeadline = rd; }
        public Long getVenueId()                               { return venueId; }
        public void setVenueId(Long venueId)                   { this.venueId = venueId; }
        public String getCustomLocation()                      { return customLocation; }
        public void setCustomLocation(String customLocation)   { this.customLocation = customLocation; }
        public Integer getCapacity()                           { return capacity; }
        public void setCapacity(Integer capacity)              { this.capacity = capacity; }
        public Long getPrimaryCategoryId()                     { return primaryCategoryId; }
        public void setPrimaryCategoryId(Long id)              { this.primaryCategoryId = id; }
        public Long getSecondaryCategoryId()                   { return secondaryCategoryId; }
        public void setSecondaryCategoryId(Long id)            { this.secondaryCategoryId = id; }
        public Long getTertiaryCategoryId()                    { return tertiaryCategoryId; }
        public void setTertiaryCategoryId(Long id)             { this.tertiaryCategoryId = id; }
        public String getTags()                                { return tags; }
        public void setTags(String tags)                       { this.tags = tags; }
        public String getImageUrl()                            { return imageUrl; }
        public void setImageUrl(String imageUrl)               { this.imageUrl = imageUrl; }
    }

    public static class UpdateEventRequest {
        private String title;
        private String description;
        private LocalDateTime eventDateTime;
        private LocalDateTime registrationDeadline;
        private Long venueId;
        private String customLocation;
        private Integer capacity;
        private Long primaryCategoryId;
        private Long secondaryCategoryId;
        private Long tertiaryCategoryId;
        private String tags;
        private String imageUrl;

        public String getTitle()                               { return title; }
        public void setTitle(String title)                     { this.title = title; }
        public String getDescription()                         { return description; }
        public void setDescription(String description)         { this.description = description; }
        public LocalDateTime getEventDateTime()                { return eventDateTime; }
        public void setEventDateTime(LocalDateTime dt)         { this.eventDateTime = dt; }
        public LocalDateTime getRegistrationDeadline()         { return registrationDeadline; }
        public void setRegistrationDeadline(LocalDateTime rd)  { this.registrationDeadline = rd; }
        public Long getVenueId()                               { return venueId; }
        public void setVenueId(Long venueId)                   { this.venueId = venueId; }
        public String getCustomLocation()                      { return customLocation; }
        public void setCustomLocation(String customLocation)   { this.customLocation = customLocation; }
        public Integer getCapacity()                           { return capacity; }
        public void setCapacity(Integer capacity)              { this.capacity = capacity; }
        public Long getPrimaryCategoryId()                     { return primaryCategoryId; }
        public void setPrimaryCategoryId(Long id)              { this.primaryCategoryId = id; }
        public Long getSecondaryCategoryId()                   { return secondaryCategoryId; }
        public void setSecondaryCategoryId(Long id)            { this.secondaryCategoryId = id; }
        public Long getTertiaryCategoryId()                    { return tertiaryCategoryId; }
        public void setTertiaryCategoryId(Long id)             { this.tertiaryCategoryId = id; }
        public String getTags()                                { return tags; }
        public void setTags(String tags)                       { this.tags = tags; }
        public String getImageUrl()                            { return imageUrl; }
        public void setImageUrl(String imageUrl)               { this.imageUrl = imageUrl; }
    }

    // approve=true passes the gate, approve=false rejects the entire event
    public static class VenueApprovalRequest {
        private Boolean approve;
        private String remarks;

        public Boolean getApprove()        { return approve; }
        public void setApprove(Boolean a)  { this.approve = a; }
        public String getRemarks()         { return remarks; }
        public void setRemarks(String r)   { this.remarks = r; }
    }

    // same as VenueApprovalRequest but for the event content approval gate
    public static class EventApprovalRequest {
        private Boolean approve;
        private String remarks;

        public Boolean getApprove()        { return approve; }
        public void setApprove(Boolean a)  { this.approve = a; }
        public String getRemarks()         { return remarks; }
        public void setRemarks(String r)   { this.remarks = r; }
    }
}