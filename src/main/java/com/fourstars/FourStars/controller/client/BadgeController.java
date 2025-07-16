package com.fourstars.FourStars.controller.client;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.badge.BadgeResponseDTO;
import com.fourstars.FourStars.service.BadgeService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("ClientBadgeController")
@RequestMapping("/api/v1/badges")
@Tag(name = "Client - Badge Management API", description = "APIs for managing user achievement badges")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @Operation(summary = "Get a badge by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved badge"),
            @ApiResponse(responseCode = "404", description = "Badge not found")
    })
    @GetMapping("/{id}")
    @ApiMessage("Fetch a badge by its ID")
    public ResponseEntity<BadgeResponseDTO> getBadgeById(@PathVariable long id) throws ResourceNotFoundException {
        BadgeResponseDTO badge = badgeService.fetchBadgeById(id);
        return ResponseEntity.ok(badge);
    }

    @Operation(summary = "Get all badges with pagination")
    @GetMapping
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
