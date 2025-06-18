package com.fourstars.FourStars.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public void createNotification(User recipient, User actor, NotificationType type, String message, String link) {
        if (recipient.equals(actor)) {
            return;
        }

        Notification notification = new Notification(recipient, actor, type, message, link);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<NotificationResponseDTO> getNotificationsForCurrentUser(Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        Page<Notification> notificationPage = notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser,
                pageable);

        Page<NotificationResponseDTO> dtoPage = notificationPage.map(this::convertToResponseDTO);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                dtoPage.getNumber() + 1,
                dtoPage.getSize(),
                dtoPage.getTotalPages(),
                dtoPage.getTotalElements());

        return new ResultPaginationDTO<>(meta, dtoPage.getContent());
    }

}