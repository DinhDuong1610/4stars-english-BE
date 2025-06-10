package com.fourstars.FourStars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByPostId(long postId);

    Page<Comment> findByPostIdAndParentCommentIsNull(long postId, Pageable pageable);

    void deleteByPostId(long postId);
}
