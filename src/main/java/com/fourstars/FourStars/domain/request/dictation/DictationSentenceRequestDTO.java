package com.fourstars.FourStars.domain.request.dictation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationSentenceRequestDTO {
    @NotBlank(message = "Audio URL cannot be blank")
    private String audioUrl;

    @NotBlank(message = "Correct text cannot be blank")
    private String correctText;
    private int orderIndex;
}
