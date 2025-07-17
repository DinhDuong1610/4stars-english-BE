package com.fourstars.FourStars.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.UserVocabulary;
import com.fourstars.FourStars.domain.key.UserVocabularyId;

@Repository
public interface UserVocabularyRepository
        extends JpaRepository<UserVocabulary, UserVocabularyId>, JpaSpecificationExecutor<UserVocabulary> {

    UserVocabulary findByUserIdAndVocabularyId(Long userId, Long vocabularyId);

    Optional<UserVocabulary> findById(UserVocabularyId id);

    List<UserVocabulary> findByUser(User user);

    int countByUser(User user);

    long countByUserAndNextReviewAtBefore(User user, Instant now);

    @Modifying
    @Query("DELETE FROM UserVocabulary uv WHERE uv.id.vocabularyId = :vocabularyId")
    void deleteByVocabularyId(@Param("vocabularyId") Long vocabularyId);

    Page<UserVocabulary> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<UserVocabulary> findByUserAndLevelOrderByCreatedAtDesc(User user, Integer level, Pageable pageable);

    /**
     * Tìm danh sách những User duy nhất có từ vựng cần ôn tập
     * (có nextReviewAt nhỏ hơn hoặc bằng thời điểm hiện tại).
     * 
     * @param now Thời điểm hiện tại.
     * @return Danh sách các User.
     */
    @Query("SELECT DISTINCT uv.user FROM UserVocabulary uv WHERE uv.nextReviewAt <= :now")
    List<User> findUsersWithPendingReviews(@Param("now") Instant now);
}
