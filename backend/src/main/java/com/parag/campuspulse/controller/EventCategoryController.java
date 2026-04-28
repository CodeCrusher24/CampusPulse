package com.parag.campuspulse.controller;

import com.parag.campuspulse.dto.ApiResponse;
import com.parag.campuspulse.model.EventCategory;
import com.parag.campuspulse.repository.EventCategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Event Categories", description = "Read-only category listing — seeded categories")
public class EventCategoryController {

    @Autowired
    private EventCategoryRepository categoryRepository;

    @Operation(
            summary = "Get all event categories",
            description = "Returns all pre-loaded categories (Technical, Cultural, Sports, etc.)."
    )
    @GetMapping
    public ResponseEntity<List<EventCategory>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @Operation(summary = "Get a single category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body(new ApiResponse.Error("Category not found")));
    }
}