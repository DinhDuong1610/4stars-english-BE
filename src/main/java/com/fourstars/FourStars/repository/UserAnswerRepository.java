package com.fourstars.FourStars.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.UserAnswer;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
}
