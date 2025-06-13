package com.fourstars.FourStars.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.QuestionChoice;

@Repository
public interface QuestionChoiceRepository extends JpaRepository<QuestionChoice, Long> {
}
