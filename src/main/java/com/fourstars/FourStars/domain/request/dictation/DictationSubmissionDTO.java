package com.fourstars.FourStars.domain.request.dictation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictationSubmissionDTO {
    private long sentenceId;
    private String userText;
}