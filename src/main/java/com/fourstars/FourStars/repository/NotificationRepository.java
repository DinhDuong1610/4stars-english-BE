package com.fourstars.FourStars.repository;

import com.fourstars.FourStars.domain.Notification;
import com.fourstars.FourStars.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.recipient = :recipient ORDER BY n.createdAt DESC", countQuery = "SELECT count(n) FROM Notification n WHERE n.recipient = :recipient")
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    long countByRecipientAndIsReadFalse(User recipient);
}