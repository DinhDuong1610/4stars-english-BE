package com.fourstars.FourStars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Quiz> findAll(Specification<Quiz> spec, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);
}
