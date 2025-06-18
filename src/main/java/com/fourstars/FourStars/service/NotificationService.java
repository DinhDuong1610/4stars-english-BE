package com.fourstars.FourStars.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Notification;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.repository.NotificationRepository;
import com.fourstars.FourStars.util.constant.NotificationType;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void createNotification(User recipient, User actor, NotificationType type, String message, String link) {
        if (recipient.equals(actor)) {
            return;
        }

        Notification notification = new Notification(recipient, actor, type, message, link);
        notificationRepository.save(notification);
    }

}