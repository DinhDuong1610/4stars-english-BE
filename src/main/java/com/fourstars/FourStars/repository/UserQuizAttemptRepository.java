package com.fourstars.FourStars.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Quiz;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.UserQuizAttempt;
import com.fourstars.FourStars.util.constant.QuizStatus;

@Repository
public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {
    Optional<UserQuizAttempt> findByUserAndQuizAndStatus(User user, Quiz quiz, QuizStatus status);

    Page<UserQuizAttempt> findByUserId(long userId, Pageable pageable);
}