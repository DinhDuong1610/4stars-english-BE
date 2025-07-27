package com.fourstars.FourStars.domain.response.dictation;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NlpAnalysisResponse {

    private int score;
    private List<Diff> diffs;
    private List<String> explanations;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Diff {
        private String type;
        private String text;
    }
}