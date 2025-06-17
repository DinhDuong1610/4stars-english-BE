package com.fourstars.FourStars.domain.response.vocabulary;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class UserVocabularyResponseDTO {
    private long userId;
    private long vocabularyId;
    private int level;
    private int repetitions;
    private double easeFactor;
    private int reviewInterval;
    private Instant lastReviewedAt;
    private Instant nextReviewAt;
}