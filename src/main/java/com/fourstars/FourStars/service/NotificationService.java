package com.fourstars.FourStars.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Notification;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.notification.NotificationResponseDTO;
import com.fourstars.FourStars.repository.NotificationRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.util.SecurityUtil;
import com.fourstars.FourStars.util.constant.NotificationType;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private User getCurrentAuthenticatedUser() {
        return SecurityUtil.getCurrentUserLogin()
                .flatMap(userRepository::findByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated. Please login."));
    }

    private NotificationResponseDTO convertToResponseDTO(Notification notification) {
        if (notification == null)
            return null;

        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setLink(notification.getLink());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());

        if (notification.getActor() != null) {
            NotificationResponseDTO.ActorDTO actorDTO = new NotificationResponseDTO.ActorDTO();
            actorDTO.setId(notification.getActor().getId());
            actorDTO.setName(notification.getActor().getName());
            dto.setActor(actorDTO);
        }

        return dto;
    }

    // @Async
    @Transactional
    public void createNotification(User recipient, User actor, NotificationType type, String message, String link) {
        String recipientEmail = (recipient != null) ? recipient.getEmail() : "N/A";
        String actorEmail = (actor != null) ? actor.getEmail() : "SYSTEM";

        logger.info("Attempting to create notification. Type: {}, Recipient: {}, Actor: {}", type, recipientEmail,
                actorEmail);

        if (recipient != null && actor != null && recipient.getId() == actor.getId()) {
            logger.warn("Skipping notification creation: recipient and actor are the same user (ID: {})",
                    recipient.getId());
            return;
        }

        Notification notification = new Notification(recipient, actor, type, message, link);
        Notification savedNotification = notificationRepository.save(notification);
        logger.info("Notification with ID {} saved to database.", savedNotification.getId());

        // try {
        // NotificationResponseDTO notificationDTO =
        // convertToResponseDTO(savedNotification);
        // logger.debug("Pushing real-time notification ID {} to user '{}'",
        // savedNotification.getId(),
        // recipient.getEmail());

        // if (recipient != null) {
        // messagingTemplate.convertAndSendToUser(
        // recipient.getEmail(),
        // "/queue/notifications",
        // notificationDTO);
        // }
        // } catch (Exception e) {
        // logger.error("Error sending notification via WebSocket for recipient {}: {}",
        // recipientEmail,
        // e.getMessage());
        // }

        try {
            if (recipient != null) {
                NotificationResponseDTO notificationDTO = convertToResponseDTO(savedNotification);

                // === THAY ĐỔI CÁCH GỬI WEBSOCKET ===
                // Tạo một destination động, riêng biệt cho mỗi user
                String destination = "/topic/notifications." + recipient.getId();
                logger.debug("Pushing real-time notification ID {} to destination '{}'", savedNotification.getId(),
                        destination);

                // Gửi message đến destination đó
                messagingTemplate.convertAndSend(destination, notificationDTO);
            }
        } catch (Exception e) {
            logger.error("Error sending notification via WebSocket for recipient ID {}: {}", recipient.getId(),
                    e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<NotificationResponseDTO> getNotificationsForCurrentUser(Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.debug("Fetching notifications for user: {}", currentUser.getEmail());

        Page<Notification> notificationPage = notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser,
                pageable);

        Page<NotificationResponseDTO> dtoPage = notificationPage.map(this::convertToResponseDTO);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                dtoPage.getNumber() + 1,
                dtoPage.getSize(),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements());

        logger.debug("Found {} notifications on page {} for user {}", dtoPage.getNumberOfElements(), meta.getPage(),
                currentUser.getEmail());

        return new ResultPaginationDTO<>(meta, dtoPage.getContent());
    }

    @Transactional
    public void markAsRead(long notificationId) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User '{}' is marking notification ID {} as read", currentUser.getEmail(), notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipient().equals(currentUser)) {
            throw new AccessDeniedException("You do not have permission to read this notification.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        logger.info("Successfully marked notification ID {} as read.", notificationId);

    }

    @Transactional(readOnly = true)
    public long getUnreadCountForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        logger.debug("Fetching unread notification count for user '{}'", currentUser.getEmail());

        return notificationRepository.countByRecipientAndIsReadFalse(currentUser);
    }
}