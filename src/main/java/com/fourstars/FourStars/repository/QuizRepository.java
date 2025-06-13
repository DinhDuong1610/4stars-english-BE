package com.fourstars.FourStars.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
