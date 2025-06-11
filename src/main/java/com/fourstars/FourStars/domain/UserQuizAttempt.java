package com.fourstars.FourStars.domain;

import com.fourstars.FourStars.util.constant.QuizStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_quiz_attempts")
@Getter
@Setter
public class UserQuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // Trạng thái của lần làm bài
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QuizStatus status;

    private int score = 0; // Tổng điểm đạt được

    private Instant startedAt;

    private Instant completedAt;

    // Một lần làm bài sẽ có nhiều câu trả lời của người dùng
    @OneToMany(mappedBy = "userQuizAttempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserAnswer> userAnswers = new HashSet<>();
}
