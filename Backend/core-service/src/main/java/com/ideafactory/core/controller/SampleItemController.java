package com.ideafactory.core.controller;

import com.ideafactory.core.dto.SampleItemDTO;
import com.ideafactory.core.service.SampleItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/core/items")
public class SampleItemController {

    private final SampleItemService sampleItemService;

    public SampleItemController(SampleItemService sampleItemService) {
        this.sampleItemService = sampleItemService;
    }

    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Default fallback for development/testing if no user context is provided
            return 1L;
        }
        // In JWT context, principal holds user email or details. 
        // For production, resolve user ID via token claims or user-service feign client.
        return 1L;
    }

    @GetMapping
    public ResponseEntity<Page<SampleItemDTO>> getItems(
            @RequestParam(required = false) Long userId,
            Pageable pageable) {
        Long targetUserId = userId != null ? userId : getAuthenticatedUserId();
        return ResponseEntity.ok(sampleItemService.getItemsByUser(targetUserId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SampleItemDTO> getItemById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        Long targetUserId = userId != null ? userId : getAuthenticatedUserId();
        return ResponseEntity.ok(sampleItemService.getItemById(id, targetUserId));
    }

    @PostMapping
    public ResponseEntity<SampleItemDTO> createItem(
            @RequestParam(required = false) Long userId,
            @Valid @RequestBody SampleItemDTO request) {
        Long targetUserId = userId != null ? userId : getAuthenticatedUserId();
        SampleItemDTO created = sampleItemService.createItem(targetUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SampleItemDTO> updateItem(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @Valid @RequestBody SampleItemDTO request) {
        Long targetUserId = userId != null ? userId : getAuthenticatedUserId();
        return ResponseEntity.ok(sampleItemService.updateItem(id, targetUserId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        Long targetUserId = userId != null ? userId : getAuthenticatedUserId();
        sampleItemService.deleteItem(id, targetUserId);
        return ResponseEntity.noContent().build();
    }
}
