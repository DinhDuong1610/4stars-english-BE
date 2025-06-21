package com.fourstars.FourStars.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fourstars.FourStars.domain.key.UserVocabularyId;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_vocabularies", indexes = {
        @Index(name = "idx_uservocab_nextreview", columnList = "next_review_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVocabulary {

    @EmbeddedId
    private UserVocabularyId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vocabularyId")
    @JoinColumn(name = "vocabulary_id", insertable = false, updatable = false)
    @JsonIgnore
    private Vocabulary vocabulary;

    @NotNull(message = "Learning level cannot be null")
    @Min(value = 1, message = "Level must be between 1 and 5")
    @Max(value = 5, message = "Level must be between 1 and 5")
    @Column(nullable = false)
    private int level = 1;

    // --- Các trường nội bộ cho thuật toán SM-2 ---

    /**
     * Số lần lặp lại (n trong SM-2).
     */
    @NotNull(message = "Repetitions count cannot be null")
    @Min(value = 0, message = "Repetitions count must be non-negative")
    @Column(nullable = false)
    private int repetitions = 0;

    /**
     * Hệ số dễ (Ease Factor - EF). Bắt đầu từ 2.5.
     */
    @NotNull(message = "Ease factor cannot be null")
    @Column(name = "ease_factor", nullable = false)
    private double easeFactor = 2.5;

    /**
     * Khoảng thời gian giữa các lần ôn tập (tính bằng ngày).
     */
    @NotNull(message = "Interval cannot be null")
    @Min(value = 0, message = "Interval must be non-negative")
    @Column(name = "reivew_interval", nullable = false)
    private int reviewInterval = 0; // Bắt đầu từ 0, lần đầu tiên sẽ là 1 ngày

    // --- Các trường thời gian ---

    @Column(name = "last_reviewed_at")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss a", timezone = "GMT+7")
    private Instant lastReviewedAt;

    @Column(name = "next_review_at")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss a", timezone = "GMT+7")
    private Instant nextReviewAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;

    @PrePersist
    public void handleBeforeCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.lastReviewedAt == null) {
            this.lastReviewedAt = now;
        }

        if (this.nextReviewAt == null) {
            this.nextReviewAt = this.lastReviewedAt.plus(1, ChronoUnit.DAYS);
        }
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
    }

    public UserVocabulary(User user, Vocabulary vocabulary) {
        this.user = user;
        this.vocabulary = vocabulary;
        this.id = new UserVocabularyId(user.getId(), vocabulary.getId());

        this.level = 1;
        this.repetitions = 0;
        this.easeFactor = 2.5;
        this.reviewInterval = 0;
    }
}
