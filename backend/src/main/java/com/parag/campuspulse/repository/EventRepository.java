package com.parag.campuspulse.repository;

import com.parag.campuspulse.model.Event;
import com.parag.campuspulse.model.User;
import com.parag.campuspulse.model.EventCategory;
import com.parag.campuspulse.model.EventStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    List<Event> findByStatus(EventStatus status);

    // matches against primary, secondary, or tertiary category
    @Query("""
        SELECT e
        FROM Event e
        WHERE e.primaryCategory = :category
           OR e.secondaryCategory = :category
           OR e.tertiaryCategory = :category
    """)
    Page<Event> findByAnyCategory(@Param("category") EventCategory category,
                                  Pageable pageable);

    Page<Event> findByCreatedBy(User user, Pageable pageable);

    List<Event> findByCreatedBy(User user);

    Page<Event> findByStatusOrderByEventDateTimeDesc(EventStatus status,
                                                     Pageable pageable);

    @Query("""
        SELECT e
        FROM Event e
        WHERE e.status = 'PUBLISHED'
          AND e.eventDateTime > :now
        ORDER BY e.eventDateTime ASC
    """)
    List<Event> findUpcomingPublishedEvents(@Param("now") LocalDateTime now);

    @Query("""
        SELECT e
        FROM Event e
        WHERE e.status = :status
          AND (
                e.primaryCategory = :category
             OR e.secondaryCategory = :category
             OR e.tertiaryCategory = :category
          )
    """)
    Page<Event> findByStatusAndAnyCategory(@Param("status") EventStatus status,
                                           @Param("category") EventCategory category,
                                           Pageable pageable);

    Page<Event> findByCreatedByAndStatus(User user,
                                         EventStatus status,
                                         Pageable pageable);
}