package com.fourstars.FourStars.repository;

import com.fourstars.FourStars.domain.DictationTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DictationTopicRepository
        extends JpaRepository<DictationTopic, Long>, JpaSpecificationExecutor<DictationTopic> {
}