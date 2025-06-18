package com.fourstars.FourStars.controller.client;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.dashboard.DashboardResponseDTO;
import com.fourstars.FourStars.service.UserService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/users/me")
public class DashboardController {
    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Get the user's learning progress dashboard")
    public ResponseEntity<DashboardResponseDTO> getUserDashboard() {
        DashboardResponseDTO dashboardData = userService.getUserDashboard();
        return ResponseEntity.ok(dashboardData);
    }
}
