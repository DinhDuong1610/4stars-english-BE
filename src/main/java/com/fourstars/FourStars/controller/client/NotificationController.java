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

import com.fourstars.FourStars.domain.Notification;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.notification.NotificationResponseDTO;
import com.fourstars.FourStars.service.NotificationService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Get notifications for the current user")
    public ResponseEntity<ResultPaginationDTO<NotificationResponseDTO>> getNotifications(Pageable pageable) {
        ResultPaginationDTO<NotificationResponseDTO> result = notificationService
                .getNotificationsForCurrentUser(pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Mark a specific notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Get the count of unread notifications for the current user")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCountForCurrentUser();
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}
