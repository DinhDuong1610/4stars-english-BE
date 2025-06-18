package com.fourstars.FourStars.controller.client;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
    @ApiMessage("Get notifications for the current user")
    public ResponseEntity<ResultPaginationDTO<NotificationResponseDTO>> getNotifications(Pageable pageable) {
        ResultPaginationDTO<NotificationResponseDTO> result = notificationService
                .getNotificationsForCurrentUser(pageable);
        return ResponseEntity.ok(result);
    }
}
