package com.fourstars.FourStars.repository;

import java.util.List;
import java.util.Optional;

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
        extends JpaRepository<UserVocabulary, Long>, JpaSpecificationExecutor<UserVocabulary> {

    UserVocabulary findByUserIdAndVocabularyId(Long userId, Long vocabularyId);

    Optional<UserVocabulary> findById(UserVocabularyId id);

    List<UserVocabulary> findByUser(User user);

    int countByUser(User user);

    @Modifying
    @Query("DELETE FROM UserVocabulary uv WHERE uv.id.vocabularyId = :vocabularyId")
    void deleteByVocabularyId(@Param("vocabularyId") Long vocabularyId);
}
