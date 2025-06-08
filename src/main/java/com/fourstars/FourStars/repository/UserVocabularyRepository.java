package com.fourstars.FourStars.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.UserVocabulary;

@Repository
public interface UserVocabularyRepository
        extends JpaRepository<UserVocabulary, Long>, JpaSpecificationExecutor<UserVocabulary> {
    @Modifying
    @Query("DELETE FROM UserVocabulary uv WHERE uv.id.vocabularyId = :vocabularyId")
    void deleteByVocabularyId(@Param("vocabularyId") Long vocabularyId);
}
