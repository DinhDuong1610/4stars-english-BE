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
}
