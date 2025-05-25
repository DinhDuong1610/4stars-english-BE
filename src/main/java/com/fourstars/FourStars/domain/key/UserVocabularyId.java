package com.fourstars.FourStars.domain.key;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVocabularyId implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "vocabulary_id", nullable = false)
    private Long vocabularyId;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserVocabularyId that = (UserVocabularyId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(vocabularyId, that.vocabularyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, vocabularyId);
    }
}