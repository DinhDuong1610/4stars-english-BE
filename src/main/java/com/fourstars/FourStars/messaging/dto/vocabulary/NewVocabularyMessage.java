package com.fourstars.FourStars.messaging.dto.vocabulary;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewVocabularyMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long newVocabularyId;
}