package com.fourstars.FourStars.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Comment;
import com.fourstars.FourStars.domain.CommentAttachment;
import com.fourstars.FourStars.domain.Post;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.request.comment.CommentRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.comment.CommentResponseDTO;
import com.fourstars.FourStars.domain.response.post.PostResponseDTO;
import com.fourstars.FourStars.repository.CommentRepository;
import com.fourstars.FourStars.repository.PostRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.util.SecurityUtil;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    private CommentResponseDTO convertToCommentResponseDTO(Comment comment) {
        if (comment == null)
            return null;
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        if (comment.getUser() != null) {
            PostResponseDTO.UserInfoDTO userInfo = new PostResponseDTO.UserInfoDTO();
            userInfo.setId(comment.getUser().getId());
            userInfo.setName(comment.getUser().getName());
            dto.setUser(userInfo);
        }

        if (comment.getAttachments() != null) {
            List<PostResponseDTO.AttachmentInfoDTO> attachments = comment.getAttachments().stream().map(att -> {
                PostResponseDTO.AttachmentInfoDTO attDto = new PostResponseDTO.AttachmentInfoDTO();
                attDto.setId(att.getId());
                attDto.setFileUrl(att.getFileUrl());
                attDto.setOriginalFileName(att.getOriginalFileName());
                attDto.setFileSize(att.getFileSize());
                if (att.getFileType() != null) {
                    attDto.setFileType(att.getFileType().name());
                }
                return attDto;
            }).collect(Collectors.toList());
            dto.setAttachments(attachments);
        }

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            List<CommentResponseDTO> replies = comment.getReplies().stream()
                    .map(this::convertToCommentResponseDTO)
                    .collect(Collectors.toList());
            dto.setReplies(replies);
        } else {
            dto.setReplies(new ArrayList<>());
        }

        return dto;
    }

    private User getCurrentAuthenticatedUser() {
        return SecurityUtil.getCurrentUserLogin()
                .flatMap(userRepository::findByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated. Please login."));
    }

    @Transactional
    public CommentResponseDTO createComment(CommentRequestDTO requestDTO)
            throws ResourceNotFoundException, BadRequestException {
        User currentUser = getCurrentAuthenticatedUser();

        Post post = postRepository.findById(requestDTO.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + requestDTO.getPostId()));

        Comment comment = new Comment();
        comment.setUser(currentUser);
        comment.setPost(post);
        comment.setContent(requestDTO.getContent());

        if (requestDTO.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(requestDTO.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent comment not found with id: " + requestDTO.getParentCommentId()));

            if (parentComment.getPost().getId() != post.getId()) {
                throw new BadRequestException("Parent comment does not belong to the same post.");
            }
            comment.setParentComment(parentComment);
        }

        if (requestDTO.getAttachments() != null && !requestDTO.getAttachments().isEmpty()) {
            for (var attDTO : requestDTO.getAttachments()) {
                CommentAttachment attachment = new CommentAttachment();
                attachment.setFileUrl(attDTO.getFileUrl());
                attachment.setFileType(attDTO.getFileType());
                attachment.setOriginalFileName(attDTO.getOriginalFileName());
                attachment.setFileSize(attDTO.getFileSize());
                comment.addAttachment(attachment);
            }
        }

        Comment savedComment = commentRepository.save(comment);
        return convertToCommentResponseDTO(savedComment);
    }

    @Transactional
    public CommentResponseDTO updateComment(long id, CommentRequestDTO requestDTO)
            throws ResourceNotFoundException, BadRequestException {
        User currentUser = getCurrentAuthenticatedUser();

        Comment commentDB = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        if (commentDB.getUser().getId() != currentUser.getId()) {
            throw new BadRequestException("You do not have permission to update this comment.");
        }

        commentDB.setContent(requestDTO.getContent());
        Comment updatedComment = commentRepository.save(commentDB);
        return convertToCommentResponseDTO(updatedComment);
    }

    @Transactional
    public void deleteComment(long id) throws ResourceNotFoundException, BadRequestException {
        User currentUser = getCurrentAuthenticatedUser();

        Comment commentToDelete = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        if (commentToDelete.getUser().getId() != currentUser.getId() &&
                commentToDelete.getPost().getUser().getId() != currentUser.getId()) {
            throw new BadRequestException("You do not have permission to delete this comment.");
        }

        commentRepository.delete(commentToDelete);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<CommentResponseDTO> fetchCommentsByPost(long postId, Pageable pageable) {
        Page<Comment> topLevelCommentsPage = commentRepository.findByPostIdAndParentCommentIsNull(postId, pageable);

        List<CommentResponseDTO> commentDTOs = topLevelCommentsPage.getContent().stream()
                .map(this::convertToCommentResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                topLevelCommentsPage.getTotalPages(),
                topLevelCommentsPage.getTotalElements());

        return new ResultPaginationDTO<>(meta, commentDTOs);
    }
}
