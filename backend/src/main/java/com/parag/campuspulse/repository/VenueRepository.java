package com.parag.campuspulse.repository;

import com.parag.campuspulse.model.Venue;
import com.parag.campuspulse.model.VenueType;
import com.parag.campuspulse.model.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    Optional<Venue> findByName(String name);

    List<Venue> findByType(VenueType type);

    List<Venue> findBySchool(School school);

    List<Venue> findByEnabledTrue();

    boolean existsByName(String name);
}