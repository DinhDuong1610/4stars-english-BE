package com.fourstars.FourStars.domain;

import com.fourstars.FourStars.util.constant.QuestionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_question_quizid", columnList = "quiz_id")
})
@Getter
@Setter
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestionType questionType;

    // Dùng cho hầu hết các loại: "What is this?", "Translate this sentence", ...
    @Column(columnDefinition = "TEXT")
    private String prompt;

    // Dùng cho FILL_IN_BLANK, chứa câu có chứa placeholder (ví dụ: "Hello, my ___
    // is John.")
    @Column(columnDefinition = "TEXT")
    private String textToFill;

    // Dùng cho ARRANGE_WORDS, chứa đáp án đúng để so sánh
    @Column(columnDefinition = "TEXT")
    private String correctSentence;

    // URL file audio cho các câu hỏi nghe
    @Column(length = 2048)
    private String audioUrl;

    // URL hình ảnh cho câu hỏi hình ảnh
    @Column(length = 2048)
    private String imageUrl;

    // Điểm cho câu hỏi này (mặc định là 10)
    private int points = 10;

    // Thứ tự của câu hỏi trong bài quiz
    private int questionOrder;

    // Một câu hỏi có nhiều lựa chọn trả lời (cho loại multiple choice)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<QuestionChoice> choices = new HashSet<>();

    // Một câu hỏi có thể liên quan đến một từ vựng cụ thể
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id")
    private Vocabulary relatedVocabulary;
}
