package com.fourstars.FourStars.domain.response.dictation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationSentenceResponseDTO {
    private long id;
    private String correctText;
    private String audioUrl;
    private int orderIndex;
}
