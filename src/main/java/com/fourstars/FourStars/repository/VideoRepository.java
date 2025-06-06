package com.fourstars.FourStars.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long>, JpaSpecificationExecutor<Video> {
    boolean existsByCategoryId(Long categoryId);
}
