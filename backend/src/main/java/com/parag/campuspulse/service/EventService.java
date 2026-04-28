package com.parag.campuspulse.service;

import com.parag.campuspulse.dto.EventDTOs.*;
import com.parag.campuspulse.model.*;
import com.parag.campuspulse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EventService {

    @Autowired private EventRepository         eventRepository;
    @Autowired private VenueRepository         venueRepository;
    @Autowired private EventCategoryRepository  categoryRepository;
    @Autowired private UserRepository          userRepository;

    // =========================================================
    // CREATE EVENT
    // =========================================================

    /**
     * Creates an event in DRAFT status.
     *
     * Admin shortcut: if the creator is a SYSTEM_ADMIN, both approval gates
     * are pre-set to true and the event is immediately set to PUBLISHED —
     * bypassing the entire approval workflow.
     */
    @Transactional
    public Event createEvent(CreateEventRequest request, Long creatorId) {

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("Event title is required");
        }
        if (request.getEventDateTime() == null) {
            throw new RuntimeException("Event date/time is required");
        }
        if (request.getCapacity() == null || request.getCapacity() < 1) {
            throw new RuntimeException("Capacity must be at least 1");
        }
        if (request.getPrimaryCategoryId() == null) {
            throw new RuntimeException("Primary category is required");
        }

        EventCategory primaryCategory = categoryRepository.findById(request.getPrimaryCategoryId())
                .orElseThrow(() -> new RuntimeException("Primary category not found"));

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDateTime(request.getEventDateTime());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setCapacity(request.getCapacity());
        event.setPrimaryCategory(primaryCategory);
        event.setTags(request.getTags());
        event.setImageUrl(request.getImageUrl());
        event.setCreatedBy(creator);

        // Venue or custom location — one is required
        if (request.getVenueId() != null) {
            Venue venue = venueRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new RuntimeException("Venue not found"));
            if (!Boolean.TRUE.equals(venue.getEnabled())) {
                throw new RuntimeException("Venue is not currently available");
            }
            event.setVenue(venue);
        } else if (request.getCustomLocation() != null && !request.getCustomLocation().isBlank()) {
            event.setCustomLocation(request.getCustomLocation());
        } else {
            throw new RuntimeException("Either a venue or a custom location must be provided");
        }

        // Optional secondary / tertiary categories
        if (request.getSecondaryCategoryId() != null) {
            event.setSecondaryCategory(categoryRepository.findById(request.getSecondaryCategoryId())
                    .orElseThrow(() -> new RuntimeException("Secondary category not found")));
        }
        if (request.getTertiaryCategoryId() != null) {
            event.setTertiaryCategory(categoryRepository.findById(request.getTertiaryCategoryId())
                    .orElseThrow(() -> new RuntimeException("Tertiary category not found")));
        }

        // ── Admin bypass ────────────────────────────────────────────
        // Admin-created events skip the approval workflow entirely.
        if (creator.getRole() == UserRole.SYSTEM_ADMIN) {
            event.setVenueApproved(true);
            event.setEventApproved(true);
            event.setVenueApprovedBy(creator);
            event.setVenueApprovedAt(LocalDateTime.now());
            event.setEventApprovedBy(creator);
            event.setEventApprovedAt(LocalDateTime.now());
            event.setApprovedBy(creator);
            event.setApprovedAt(LocalDateTime.now());
            event.setStatus(EventStatus.PUBLISHED);
        } else {
            event.setStatus(EventStatus.DRAFT);
        }
        // ────────────────────────────────────────────────────────────

        return eventRepository.save(event);
    }

    // =========================================================
    // UPDATE EVENT
    // =========================================================

    @Transactional
    public Event updateEvent(Long eventId, UpdateEventRequest request, Long userId) {

        Event event = getEventById(eventId);

        if (!event.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Only the event creator can update this event");
        }
        if (!event.canBeEdited()) {
            throw new RuntimeException("Event cannot be edited in status: " + event.getStatus());
        }

        if (request.getTitle() != null)               event.setTitle(request.getTitle());
        if (request.getDescription() != null)         event.setDescription(request.getDescription());
        if (request.getEventDateTime() != null)       event.setEventDateTime(request.getEventDateTime());
        if (request.getRegistrationDeadline() != null) event.setRegistrationDeadline(request.getRegistrationDeadline());
        if (request.getCapacity() != null)            event.setCapacity(request.getCapacity());
        if (request.getTags() != null)                event.setTags(request.getTags());
        if (request.getImageUrl() != null)            event.setImageUrl(request.getImageUrl());

        if (request.getVenueId() != null) {
            Venue venue = venueRepository.findById(request.getVenueId())
                    .orElseThrow(() -> new RuntimeException("Venue not found"));
            event.setVenue(venue);
            event.setCustomLocation(null);
        } else if (request.getCustomLocation() != null) {
            event.setCustomLocation(request.getCustomLocation());
            event.setVenue(null);
        }

        if (request.getPrimaryCategoryId() != null) {
            event.setPrimaryCategory(categoryRepository.findById(request.getPrimaryCategoryId())
                    .orElseThrow(() -> new RuntimeException("Primary category not found")));
        }
        if (request.getSecondaryCategoryId() != null) {
            event.setSecondaryCategory(categoryRepository.findById(request.getSecondaryCategoryId())
                    .orElseThrow(() -> new RuntimeException("Secondary category not found")));
        }
        if (request.getTertiaryCategoryId() != null) {
            event.setTertiaryCategory(categoryRepository.findById(request.getTertiaryCategoryId())
                    .orElseThrow(() -> new RuntimeException("Tertiary category not found")));
        }

        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // =========================================================
    // SUBMIT FOR APPROVAL
    // =========================================================

    /**
     * Moves event from DRAFT → PENDING.
     *
     * If the venue is OPEN_SPACE, venue approval is pre-granted here
     * so only the event authority needs to act.
     */
    @Transactional
    public Event submitForApproval(Long eventId, Long userId) {

        Event event = getEventById(eventId);

        if (!event.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Only the event creator can submit for approval");
        }
        if (!event.canBeSubmitted()) {
            throw new RuntimeException("Event cannot be submitted in status: " + event.getStatus());
        }

        // Pre-approve venue if it's an open space (no permission needed)
        if (event.isOpenSpaceVenue()) {
            event.setVenueApproved(true);
        }
        // Pre-approve venue if using a custom location (no venue authority to consult)
        if (event.hasCustomLocation() && !event.hasVenue()) {
            event.setVenueApproved(true);
        }

        event.setStatus(EventStatus.PENDING);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // =========================================================
    // DUAL APPROVAL — GATE 1: VENUE
    // =========================================================

    /**
     * Venue authority approves or rejects the venue booking.
     *
     * Approve:
     *   venueApproved = true
     *   If eventApproved is also true → status moves to APPROVED automatically
     *
     * Reject:
     *   status → REJECTED immediately, rejection reason stored
     */
    @Transactional
    public Event approveVenue(Long eventId, VenueApprovalRequest request, Long approverId) {

        Event event = getEventById(eventId);
        User approver = getUserById(approverId);

        if (!event.canBeApproved()) {
            throw new RuntimeException("Event is not in PENDING status — cannot be approved");
        }

        requireApprovalRole(approver);

        if (Boolean.TRUE.equals(request.getApprove())) {
            event.setVenueApproved(true);
            event.setVenueApprovedBy(approver);
            event.setVenueApprovedAt(LocalDateTime.now());

            // If event gate already cleared → fully approved
            if (event.isBothApproved()) {
                fullyApprove(event, approver);
            }
        } else {
            // One rejection is enough to reject the whole event
            reject(event, request.getRemarks());
        }

        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // =========================================================
    // DUAL APPROVAL — GATE 2: EVENT
    // =========================================================

    /**
     * Faculty/HOD approves or rejects the event itself.
     *
     * Approve:
     *   eventApproved = true
     *   If venueApproved is also true → status moves to APPROVED automatically
     *
     * Reject:
     *   status → REJECTED immediately, rejection reason stored
     */
    @Transactional
    public Event approveEvent(Long eventId, EventApprovalRequest request, Long approverId) {

        Event event = getEventById(eventId);
        User approver = getUserById(approverId);

        if (!event.canBeApproved()) {
            throw new RuntimeException("Event is not in PENDING status — cannot be approved");
        }

        requireApprovalRole(approver);

        if (Boolean.TRUE.equals(request.getApprove())) {
            event.setEventApproved(true);
            event.setEventApprovedBy(approver);
            event.setEventApprovedAt(LocalDateTime.now());

            // If venue gate already cleared → fully approved
            if (event.isBothApproved()) {
                fullyApprove(event, approver);
            }
        } else {
            reject(event, request.getRemarks());
        }

        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // =========================================================
    // PUBLISH EVENT
    // =========================================================

    /**
     * Moves event from APPROVED → PUBLISHED.
     * Only the creator or a SYSTEM_ADMIN can do this.
     */
    @Transactional
    public Event publishEvent(Long eventId, Long userId) {

        Event event = getEventById(eventId);
        User user   = getUserById(userId);

        if (!event.getCreatedBy().getId().equals(userId) &&
                user.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new RuntimeException("Only the event creator or a system admin can publish this event");
        }
        if (!event.canBePublished()) {
            throw new RuntimeException("Event cannot be published in status: " + event.getStatus());
        }

        event.setStatus(EventStatus.PUBLISHED);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    // =========================================================
    // DELETE EVENT
    // =========================================================

    @Transactional
    public void deleteEvent(Long eventId, Long userId) {

        Event event = getEventById(eventId);
        User user   = getUserById(userId);

        if (!event.getCreatedBy().getId().equals(userId) &&
                user.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new RuntimeException("Only the event creator or a system admin can delete this event");
        }
        if (!event.canBeEdited()) {
            throw new RuntimeException("Only DRAFT or REJECTED events can be deleted");
        }

        eventRepository.delete(event);
    }

    // =========================================================
    // QUERIES
    // =========================================================

    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    public Page<Event> getPublishedEvents(Pageable pageable) {
        return eventRepository.findByStatus(EventStatus.PUBLISHED, pageable);
    }

    public Page<Event> getMyEvents(Long userId, Pageable pageable) {
        User user = getUserById(userId);
        return eventRepository.findByCreatedBy(user, pageable);
    }

    public Page<Event> getPendingApprovals(Pageable pageable) {
        return eventRepository.findByStatus(EventStatus.PENDING, pageable);
    }

    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    private void requireApprovalRole(User user) {
        if (user.getRole() != UserRole.FACULTY_AUTHORITY &&
                user.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new RuntimeException("Only a FACULTY_AUTHORITY or SYSTEM_ADMIN can approve events");
        }
    }

    /** Both gates passed — advance status to APPROVED. */
    private void fullyApprove(Event event, User finalApprover) {
        event.setStatus(EventStatus.APPROVED);
        event.setApprovedBy(finalApprover);
        event.setApprovedAt(LocalDateTime.now());
    }

    /** One rejection kills the event. */
    private void reject(Event event, String reason) {
        event.setStatus(EventStatus.REJECTED);
        event.setRejectionReason(reason != null ? reason : "No reason provided");
    }
}