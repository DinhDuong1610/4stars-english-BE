package com.fourstars.FourStars.unit.messaging;

import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.messaging.NotificationConsumer;
import com.fourstars.FourStars.messaging.dto.notification.NewLikeMessage;
import com.fourstars.FourStars.messaging.dto.notification.NewReplyMessage;
import com.fourstars.FourStars.messaging.dto.notification.ReviewReminderMessage;
import com.fourstars.FourStars.service.NotificationService;
import com.fourstars.FourStars.service.UserService;
import com.fourstars.FourStars.util.constant.NotificationType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    @DisplayName("Khi nhận message like mới, phải tạo notification chính xác")
    void handleNewLike_whenValidMessage_shouldCreateCorrectNotification() {
        NewLikeMessage likeMessage = new NewLikeMessage(1L, 2L, 100L);
        User recipientUser = new User();
        recipientUser.setId(1L);
        recipientUser.setName("Tác giả");
        User actorUser = new User();
        actorUser.setId(2L);
        actorUser.setName("Người Like");

        when(userService.getUserEntityById(1L)).thenReturn(recipientUser);
        when(userService.getUserEntityById(2L)).thenReturn(actorUser);

        notificationConsumer.handleNewLike(likeMessage);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationService, times(1)).createNotification(
                any(User.class),
                any(User.class),
                eq(NotificationType.NEW_LIKE_ON_POST),
                messageCaptor.capture(),
                linkCaptor.capture());

        assertEquals("Người Like đã thích bài viết của bạn.", messageCaptor.getValue());
        assertEquals("/api/v1/posts/100", linkCaptor.getValue());
    }

    @Test
    @DisplayName("Khi nhận message reply mới, phải tạo notification chính xác")
    void handleNewReply_whenValidMessage_shouldCreateCorrectNotification() {
        NewReplyMessage replyMessage = new NewReplyMessage(1L, 2L, 100L, 500L);
        User recipientUser = new User();
        recipientUser.setId(1L);
        recipientUser.setName("Người bình luận gốc");
        User actorUser = new User();
        actorUser.setId(2L);
        actorUser.setName("Người trả lời");

        when(userService.getUserEntityById(1L)).thenReturn(recipientUser);
        when(userService.getUserEntityById(2L)).thenReturn(actorUser);

        notificationConsumer.handleNewReply(replyMessage);

        ArgumentCaptor<User> recipientCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationService, times(1)).createNotification(
                recipientCaptor.capture(),
                actorCaptor.capture(),
                eq(NotificationType.NEW_REPLY),
                messageCaptor.capture(),
                linkCaptor.capture());

        assertEquals(1L, recipientCaptor.getValue().getId());
        assertEquals(2L, actorCaptor.getValue().getId());
        assertEquals("Người trả lời đã trả lời bình luận của bạn.", messageCaptor.getValue());
        assertEquals("/api/v1/posts/100#comment-500", linkCaptor.getValue());
    }

    @Test
    @DisplayName("Khi nhận message nhắc nhở ôn tập, phải tạo notification hệ thống chính xác")
    void handleReviewReminder_whenValidMessage_shouldCreateSystemNotification() {
        ReviewReminderMessage reminderMessage = new ReviewReminderMessage(3L, "Học viên", 15L);
        User recipientUser = new User();
        recipientUser.setId(3L);
        recipientUser.setName("Học viên");

        when(userService.getUserEntityById(3L)).thenReturn(recipientUser);

        notificationConsumer.handleReviewReminder(reminderMessage);

        ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationService, times(1)).createNotification(
                any(User.class),
                actorCaptor.capture(),
                eq(NotificationType.REVIEW_REMINDER),
                messageCaptor.capture(),
                eq("/api/v1/review"));

        assertNull(actorCaptor.getValue());
        assertEquals("Bạn có 15 từ vựng cần ôn tập hôm nay. Vào học ngay thôi!", messageCaptor.getValue());
    }
}