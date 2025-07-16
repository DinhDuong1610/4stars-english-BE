package com.fourstars.FourStars.controller.client;

import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.user.UserResponseDTO;
import com.fourstars.FourStars.service.UserService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leaderboard")
@Tag(name = "User Engagement API", description = "APIs for user statistics, progress, and leaderboards")
public class LeaderboardController {

    private final UserService userService;

    public LeaderboardController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get the user leaderboard", description = "Publicly available endpoint to retrieve a paginated list of top users, sorted by points.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the leaderboard")
    })
    @GetMapping
    @ApiMessage("Fetch the user leaderboard, sorted by points")
    public ResponseEntity<ResultPaginationDTO<UserResponseDTO>> getLeaderboard(
            Pageable pageable,
            @RequestParam(name = "badgeId", required = false) Long badgeId) {
        ResultPaginationDTO<UserResponseDTO> leaderboardData = userService.getLeaderboard(pageable, badgeId);
        return ResponseEntity.ok(leaderboardData);
    }
}
