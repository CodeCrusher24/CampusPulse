package com.parag.campuspulse.service;

import com.parag.campuspulse.model.Venue;
import com.parag.campuspulse.model.VenueType;
import com.parag.campuspulse.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for venue operations.
 */
@Service
public class VenueService {

    private final VenueRepository venueRepository;

    @Autowired
    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    /**
     * Get all active venues.
     */
    public List<Venue> getAllVenues() {
        return venueRepository.findByEnabledTrue();
    }

    /**
     * Get venues by type (MANAGED or OPEN_SPACE).
     */
    public List<Venue> getVenuesByType(VenueType type) {
        return venueRepository.findByType(type);
    }

    /**
     * Get venue by ID.
     */
    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found with ID: " + id));
    }

    /**
     * Get venue by name.
     */
    public Venue getVenueByName(String name) {
        return venueRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Venue not found: " + name));
    }
}