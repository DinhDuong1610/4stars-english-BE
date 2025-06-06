package com.fourstars.FourStars.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Vocabulary;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long>, JpaSpecificationExecutor<Vocabulary> {
    boolean existsByCategoryId(Long categoryId);
}
