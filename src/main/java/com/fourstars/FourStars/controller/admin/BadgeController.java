package com.fourstars.FourStars.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.badge.BadgeRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.badge.BadgeResponseDTO;
import com.fourstars.FourStars.service.BadgeService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceInUseException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/badges") // Endpoint chung cho badges
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @PostMapping
    @ApiMessage("Create a new badge")
    public ResponseEntity<BadgeResponseDTO> createBadge(@Valid @RequestBody BadgeRequestDTO badgeRequestDTO)
            throws DuplicateResourceException {
        BadgeResponseDTO createdBadge = badgeService.createBadge(badgeRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBadge);
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch a badge by its ID")
    public ResponseEntity<BadgeResponseDTO> getBadgeById(@PathVariable long id) throws ResourceNotFoundException {
        BadgeResponseDTO badge = badgeService.fetchBadgeById(id);
        return ResponseEntity.ok(badge);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update an existing badge")
    public ResponseEntity<BadgeResponseDTO> updateBadge(
            @PathVariable long id,
            @Valid @RequestBody BadgeRequestDTO badgeRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        BadgeResponseDTO updatedBadge = badgeService.updateBadge(id, badgeRequestDTO);
        return ResponseEntity.ok(updatedBadge);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a badge")
    public ResponseEntity<Void> deleteBadge(@PathVariable long id)
            throws ResourceNotFoundException, ResourceInUseException {
        badgeService.deleteBadge(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @ApiMessage("Fetch all badges with pagination")
    public ResponseEntity<ResultPaginationDTO<BadgeResponseDTO>> getAllBadges(Pageable pageable) {
        ResultPaginationDTO<BadgeResponseDTO> result = badgeService.fetchAllBadges(pageable);
        return ResponseEntity.ok(result);
    }
}
