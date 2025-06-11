package com.fourstars.FourStars.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_answers")
@Getter
@Setter
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Lần làm bài mà câu trả lời này thuộc về
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_quiz_attempt_id", nullable = false)
    private UserQuizAttempt userQuizAttempt;

    // Câu hỏi được trả lời
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Câu trả lời của người dùng (dạng chuỗi, đủ linh hoạt cho nhiều loại câu hỏi)
    @Column(columnDefinition = "TEXT")
    private String userAnswerText;

    // ID của lựa chọn mà người dùng đã chọn (cho câu hỏi trắc nghiệm)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_choice_id")
    private QuestionChoice selectedChoice;

    // Cờ xác định câu trả lời này có đúng hay không
    private boolean isCorrect;

    // Điểm nhận được cho câu trả lời này
    private int pointsAwarded = 0;
}
