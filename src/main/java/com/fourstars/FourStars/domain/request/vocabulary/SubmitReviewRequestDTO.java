package com.fourstars.FourStars.domain.request.vocabulary;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubmitReviewRequestDTO {

    @NotNull(message = "Vocabulary ID cannot be null")
    private Long vocabularyId;

    /**
     * Chất lượng của lần trả lời, theo thang điểm của SM-2 (0-5)
     * 0: Hoàn toàn không nhớ.
     * 1: Sai, nhưng có chút ký ức mờ nhạt.
     * 2: Sai, nhưng nhớ lại được ngay sau khi xem đáp án.
     * 3: Đúng, nhưng rất khó khăn.
     * 4: Đúng, có chút do dự.
     * 5: Đúng, hoàn toàn dễ dàng.
     */
    @NotNull(message = "Quality score cannot be null")
    @Min(value = 0, message = "Quality score must be at least 0")
    @Max(value = 5, message = "Quality score must be at most 5")
    private int quality;
}
