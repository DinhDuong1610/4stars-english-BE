package com.fourstars.FourStars.controller.client;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.dashboard.DashboardResponseDTO;
import com.fourstars.FourStars.domain.response.user.UserResponseDTO;
import com.fourstars.FourStars.service.UserService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/users/me")
@Tag(name = "User Engagement API", description = "APIs for user statistics, progress, and leaderboards")
public class DashboardController {
    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get my progress dashboard", description = "Retrieves a summary of the authenticated user's learning progress, including vocabulary stats, quiz stats, streak, and subscription status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Get the user's learning progress dashboard")
    public ResponseEntity<DashboardResponseDTO> getUserDashboard() {
        DashboardResponseDTO dashboardData = userService.getUserDashboard();
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch a user by their ID")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable long id) throws ResourceNotFoundException {
        UserResponseDTO user = userService.fetchUserById(id);
        return ResponseEntity.ok(user);
    }
}
