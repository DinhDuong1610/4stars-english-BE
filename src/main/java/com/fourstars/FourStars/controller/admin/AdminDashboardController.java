package com.fourstars.FourStars.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.dashboard.AdminDashboardResponseDTO;
import com.fourstars.FourStars.service.DashboardService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Admin - Dashboard API", description = "APIs for administrator dashboard statistics")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Get admin dashboard statistics", description = "Retrieves a summary of key metrics for the entire application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @ApiMessage("Get admin dashboard statistics")
    public ResponseEntity<AdminDashboardResponseDTO> getDashboardStats() {
        AdminDashboardResponseDTO stats = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
