package com.parag.campuspulse.controller;

import com.parag.campuspulse.dto.ApiResponse;
import com.parag.campuspulse.model.Venue;
import com.parag.campuspulse.model.VenueType;
import com.parag.campuspulse.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@Tag(name = "Venues", description = "Read-only venue listing — used when creating or updating events")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @Operation(
            summary = "Get all active venues",
            description = "Returns every enabled venue. Use this to populate the venue picker when creating an event."
    )
    @GetMapping
    public ResponseEntity<?> getAllVenues() {
        try {
            List<Venue> venues = venueService.getAllVenues();
            return ResponseEntity.ok(venues);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    @Operation(
            summary = "Get venues by type",
            description = "Filter by MANAGED (requires approval) or OPEN_SPACE (no approval needed)."
    )
    @GetMapping("/by-type")
    public ResponseEntity<?> getVenuesByType(@RequestParam VenueType type) {
        try {
            return ResponseEntity.ok(venueService.getVenuesByType(type));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }

    @Operation(summary = "Get a single venue by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getVenueById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(venueService.getVenueById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse.Error(e.getMessage()));
        }
    }
}