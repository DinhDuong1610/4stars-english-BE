package com.fourstars.FourStars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId AND c.parentComment IS NULL ORDER BY c.createdAt DESC", countQuery = "SELECT count(c) FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL")
    Page<Comment> findByPostIdAndParentCommentIsNull(long postId, Pageable pageable);

    long countByPostId(long postId);

    void deleteByPostId(long postId);
}
