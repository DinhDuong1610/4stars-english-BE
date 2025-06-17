package com.fourstars.FourStars.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    void deleteByQuizId(long quizId);
}
