package com.fourstars.FourStars.repository;

import com.fourstars.FourStars.domain.Notification;
import com.fourstars.FourStars.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    long countByRecipientAndIsReadFalse(User recipient);
}