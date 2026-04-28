package com.parag.campuspulse.controller;

import com.parag.campuspulse.dto.ApiResponse;
import com.parag.campuspulse.dto.EventDTOs.*;
import com.parag.campuspulse.model.User;
import com.parag.campuspulse.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Full event lifecycle — create, submit, dual approval, publish")
public class EventController {

    @Autowired
    private EventService eventService;

    // ── Create ────────────────────────────────────────────────

    @Operation(
            summary = "Create a new event",
            description = """
            Creates an event in DRAFT status (or PUBLISHED if creator is SYSTEM_ADMIN).

            **Who can call this:** EVENT_COORDINATOR, FACULTY_AUTHORITY, SYSTEM_ADMIN

            **Admin shortcut:** events created by SYSTEM_ADMIN skip all approvals
            and are immediately PUBLISHED.
            """
    )
    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(eventService.createEvent(request, currentUser.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Update ────────────────────────────────────────────────

    @Operation(
            summary = "Update an event",
            description = "Only the creator can update. Only DRAFT or REJECTED events can be edited."
    )
    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long eventId,
            @RequestBody UpdateEventRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(eventService.updateEvent(eventId, request, currentUser.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Submit for approval ───────────────────────────────────

    @Operation(
            summary = "Submit event for approval",
            description = """
            Moves event from DRAFT → PENDING.

            **Auto-approvals on submit:**
            - If venue is OPEN_SPACE → venue gate is pre-approved (no venue authority needed)
            - If using a custom location → venue gate is pre-approved

            After this call, the event waits for FACULTY_AUTHORITY to call the
            venue approval and/or event approval endpoints.
            """
    )
    @PostMapping("/{eventId}/submit")
    public ResponseEntity<?> submitForApproval(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(eventService.submitForApproval(eventId, currentUser.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Dual approval: Gate 1 — Venue ─────────────────────────

    @Operation(
            summary = "Venue authority: approve or reject venue booking",
            description = """
            **Who can call this:** FACULTY_AUTHORITY or SYSTEM_ADMIN

            Set `approve: true` to grant the venue.
            Set `approve: false` + `remarks` to reject — this immediately rejects the entire event.

            Once BOTH venue and event gates are approved, status automatically advances to APPROVED.
            """
    )
    @PostMapping("/{eventId}/approve/venue")
    public ResponseEntity<?> approveVenue(
            @PathVariable Long eventId,
            @RequestBody VenueApprovalRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(eventService.approveVenue(eventId, request, currentUser.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Dual approval: Gate 2 — Event ─────────────────────────

    @Operation(
            summary = "Faculty/HOD: approve or reject the event itself",
            description = """
            **Who can call this:** FACULTY_AUTHORITY or SYSTEM_ADMIN

            Set `approve: true` to approve the event.
            Set `approve: false` + `remarks` to reject — this immediately rejects the entire event.

            Once BOTH venue and event gates are approved, status automatically advances to APPROVED.
            """
    )
    @PostMapping("/{eventId}/approve/event")
    public ResponseEntity<?> approveEvent(
            @PathVariable Long eventId,
            @RequestBody EventApprovalRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(eventService.approveEvent(eventId, request, currentUser.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Publish ───────────────────────────────────────────────

    @Operation(
            summary = "Publish an approved event",
            description = "Moves event from APPROVED → PUBLISHED. Students can now see and register. Only creator or SYSTEM_ADMIN."
    )
    @PostMapping("/{eventId}/publish")
    public ResponseEntity<?> publishEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User currentUser) {
        try {
            return ResponseEntity.ok(eventService.publishEvent(eventId, currentUser.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Delete ────────────────────────────────────────────────

    @Operation(
            summary = "Delete an event",
            description = "Only DRAFT or REJECTED events can be deleted. Creator or SYSTEM_ADMIN."
    )
    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User currentUser) {
        try {
            eventService.deleteEvent(eventId, currentUser.getId());
            return ResponseEntity.ok(new ApiResponse.Success("Event deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    // ── Queries ───────────────────────────────────────────────

    @Operation(
            summary = "Get all events (paginated)",
            description = "Admin/Faculty view — returns every event regardless of status."
    )
    @GetMapping
    public ResponseEntity<?> getAllEvents(
            @RequestParam(defaultValue = "0")             int page,
            @RequestParam(defaultValue = "10")            int size,
            @RequestParam(defaultValue = "eventDateTime") String sortBy,
            @RequestParam(defaultValue = "DESC")          String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("ASC")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            return ResponseEntity.ok(eventService.getAllEvents(pageable));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    @Operation(
            summary = "Get published events",
            description = "Student-facing feed — only PUBLISHED events, sorted by date ascending."
    )
    @GetMapping("/published")
    public ResponseEntity<?> getPublishedEvents(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("eventDateTime").ascending());
            return ResponseEntity.ok(eventService.getPublishedEvents(pageable));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    @Operation(
            summary = "Get my events",
            description = "Returns events created by the currently authenticated user."
    )
    @GetMapping("/my-events")
    public ResponseEntity<?> getMyEvents(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            return ResponseEntity.ok(eventService.getMyEvents(currentUser.getId(), pageable));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    @Operation(
            summary = "Get pending approvals",
            description = "Returns all events currently in PENDING status. For FACULTY_AUTHORITY to action."
    )
    @GetMapping("/pending-approvals")
    public ResponseEntity<?> getPendingApprovals(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
            return ResponseEntity.ok(eventService.getPendingApprovals(pageable));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    @Operation(summary = "Get a single event by ID")
    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEventById(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        try {
            return ResponseEntity.ok(eventService.getEventById(eventId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }
}