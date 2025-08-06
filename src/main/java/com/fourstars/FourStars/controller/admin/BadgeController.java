package com.fourstars.FourStars.controller.admin;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.badge.BadgeRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.badge.BadgeResponseDTO;
import com.fourstars.FourStars.service.BadgeService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceInUseException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/badges")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Badge Management API", description = "APIs for managing user achievement badges")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @Operation(summary = "Create a new badge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Badge created successfully"),
            @ApiResponse(responseCode = "409", description = "Badge name already exists")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Create a new badge")
    public ResponseEntity<BadgeResponseDTO> createBadge(@Valid @RequestBody BadgeRequestDTO badgeRequestDTO)
            throws DuplicateResourceException {
        BadgeResponseDTO createdBadge = badgeService.createBadge(badgeRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBadge);
    }

    @Operation(summary = "Get a badge by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved badge"),
            @ApiResponse(responseCode = "404", description = "Badge not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch a badge by its ID")
    public ResponseEntity<BadgeResponseDTO> getBadgeById(@PathVariable long id) throws ResourceNotFoundException {
        BadgeResponseDTO badge = badgeService.fetchBadgeById(id);
        return ResponseEntity.ok(badge);
    }

    @Operation(summary = "Update an existing badge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badge updated successfully"),
            @ApiResponse(responseCode = "404", description = "Badge not found"),
            @ApiResponse(responseCode = "409", description = "Badge name already exists for another badge")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Update an existing badge")
    public ResponseEntity<BadgeResponseDTO> updateBadge(
            @PathVariable long id,
            @Valid @RequestBody BadgeRequestDTO badgeRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        BadgeResponseDTO updatedBadge = badgeService.updateBadge(id, badgeRequestDTO);
        return ResponseEntity.ok(updatedBadge);
    }

    @Operation(summary = "Delete a badge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Badge deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Badge not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete badge, it is in use by users")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Delete a badge")
    public ResponseEntity<Void> deleteBadge(@PathVariable long id)
            throws ResourceNotFoundException, ResourceInUseException {
        badgeService.deleteBadge(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all badges with pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch All badges with pagination")
    public ResponseEntity<ResultPaginationDTO<BadgeResponseDTO>> getAllBadges(
            Pageable pageable,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "startCreatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startCreatedAt,
            @RequestParam(name = "endCreatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endCreatedAt) {
        ResultPaginationDTO<BadgeResponseDTO> result = badgeService.fetchAllBadges(pageable, name, startCreatedAt,
                endCreatedAt);
        return ResponseEntity.ok(result);
    }
}
