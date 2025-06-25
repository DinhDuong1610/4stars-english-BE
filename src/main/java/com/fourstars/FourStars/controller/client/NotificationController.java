package com.fourstars.FourStars.controller.client;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.notification.NotificationResponseDTO;
import com.fourstars.FourStars.service.NotificationService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Client - Notification API", description = "APIs for managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get my notifications", description = "Retrieves a paginated list of notifications for the currently authenticated user, sorted by most recent first.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @GetMapping
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Get notifications for the current user")
    public ResponseEntity<ResultPaginationDTO<NotificationResponseDTO>> getNotifications(Pageable pageable) {
        ResultPaginationDTO<NotificationResponseDTO> result = notificationService
                .getNotificationsForCurrentUser(pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Mark a notification as read", description = "Marks a single notification as read for the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is trying to mark another user's notification as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PostMapping("/{id}/read")
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Mark a specific notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get unread notification count", description = "Gets the number of unread notifications for the current user, typically for displaying a badge.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the count"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @GetMapping("/unread-count")
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Get the count of unread notifications for the current user")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCountForCurrentUser();
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}
