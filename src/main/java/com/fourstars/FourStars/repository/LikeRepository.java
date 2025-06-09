package com.fourstars.FourStars.repository;

import com.fourstars.FourStars.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndPostId(long userId, long postId);

    long countByPostId(long postId);
}
