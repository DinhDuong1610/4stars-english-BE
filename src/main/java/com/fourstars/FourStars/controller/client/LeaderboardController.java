package com.fourstars.FourStars.controller.client;

import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.user.UserResponseDTO;
import com.fourstars.FourStars.service.UserService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final UserService userService;

    public LeaderboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ApiMessage("Fetch the user leaderboard, sorted by points")
    public ResponseEntity<ResultPaginationDTO<UserResponseDTO>> getLeaderboard(Pageable pageable) {
        ResultPaginationDTO<UserResponseDTO> leaderboardData = userService.getLeaderboard(pageable);
        return ResponseEntity.ok(leaderboardData);
    }
}
