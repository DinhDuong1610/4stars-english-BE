package com.fourstars.FourStars.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
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

        List<Vocabulary> findByCategoryId(Long categoryId);

        @Override
        @EntityGraph(attributePaths = { "category" })
        Page<Vocabulary> findAll(Specification<Vocabulary> spec, Pageable pageable);

        @Query("SELECT v FROM Vocabulary v JOIN v.userLearningProgress uv " +
                        "JOIN FETCH v.category " +
                        "WHERE uv.user.id = :userId AND uv.nextReviewAt <= :now " +
                        "ORDER BY uv.nextReviewAt ASC")
        List<Vocabulary> findVocabulariesForReview(
                        @Param("userId") Long userId,
                        @Param("now") Instant now,
                        Pageable pageable);

        @Query(value = "SELECT * FROM vocabularies " +
                        "WHERE id != :excludeId " +
                        "AND word != :excludeWord " +
                        "AND part_of_speech = :pos " +
                        "ORDER BY RAND() LIMIT 3", nativeQuery = true)
        List<Vocabulary> findRandomWords(
                        @Param("excludeId") Long excludeId,
                        @Param("excludeWord") String excludeWord,
                        @Param("pos") String partOfSpeech);

}
