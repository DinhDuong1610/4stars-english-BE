package com.fourstars.FourStars.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Vocabulary;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long>, JpaSpecificationExecutor<Vocabulary> {
    boolean existsByWordAndCategoryId(String word, Long categoryId);

    boolean existsByWordAndCategoryIdAndIdNot(String word, Long categoryId, Long id);

    boolean existsByCategoryId(Long categoryId);

    // @Query(value = "SELECT v.* FROM vocabularies v " +
    // "JOIN user_vocabularies uv ON v.id = uv.vocabulary_id " +
    // "WHERE uv.user_id = :userId AND uv.next_review_at <= NOW() " +
    // "ORDER BY uv.next_review_at ASC " +
    // "LIMIT :limit", nativeQuery = true)
    // List<Vocabulary> findVocabulariesForReview(@Param("userId") Long userId,
    // @Param("limit") int limit);

    @Query("SELECT v FROM Vocabulary v JOIN v.userLearningProgress uv " +
            "WHERE uv.user.id = :userId AND uv.nextReviewAt <= :now " +
            "ORDER BY uv.nextReviewAt ASC")
    List<Vocabulary> findVocabulariesForReview(
            @Param("userId") Long userId,
            @Param("now") Instant now,
            Pageable pageable);

}
