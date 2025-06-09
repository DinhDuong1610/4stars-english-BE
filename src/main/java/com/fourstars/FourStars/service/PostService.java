package com.fourstars.FourStars.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Post;
import com.fourstars.FourStars.domain.PostAttachment;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.request.post.PostRequestDTO;
import com.fourstars.FourStars.domain.response.post.PostResponseDTO;
import com.fourstars.FourStars.repository.CommentRepository;
import com.fourstars.FourStars.repository.LikeRepository;
import com.fourstars.FourStars.repository.PostRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.util.SecurityUtil;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository,
            LikeRepository likeRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
    }

    private PostResponseDTO convertToPostResponseDTO(Post post, User currentUser) {
        if (post == null)
            return null;
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setCaption(post.getCaption());
        dto.setActive(post.isActive());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());

        if (post.getUser() != null) {
            PostResponseDTO.UserInfoDTO userInfo = new PostResponseDTO.UserInfoDTO();
            userInfo.setId(post.getUser().getId());
            userInfo.setName(post.getUser().getName());
            dto.setUser(userInfo);
        }

        if (post.getAttachments() != null) {
            List<PostResponseDTO.AttachmentInfoDTO> attachments = post.getAttachments().stream().map(att -> {
                PostResponseDTO.AttachmentInfoDTO attDto = new PostResponseDTO.AttachmentInfoDTO();
                attDto.setId(att.getId());
                attDto.setFileUrl(att.getFileUrl());
                if (att.getFileType() != null) {
                    attDto.setFileType(att.getFileType().name());
                }
                return attDto;
            }).collect(Collectors.toList());
            dto.setAttachments(attachments);
        }

        dto.setLikeCount(likeRepository.countByPostId(post.getId()));
        dto.setCommentCount(commentRepository.countByPostId(post.getId()));

        if (currentUser != null) {
            dto.setLikedByCurrentUser(
                    likeRepository.findByUserIdAndPostId(currentUser.getId(), post.getId()).isPresent());
        } else {
            dto.setLikedByCurrentUser(false);
        }

        return dto;
    }

    private User getCurrentAuthenticatedUser() {
        return SecurityUtil.getCurrentUserLogin()
                .flatMap(userRepository::findByEmail)
                .orElse(null);
    }

    @Transactional
    public PostResponseDTO createPost(PostRequestDTO requestDTO) throws ResourceNotFoundException {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated. Please login to create a post.");
        }

        Post post = new Post();
        post.setCaption(requestDTO.getCaption());
        post.setUser(currentUser);

        if (requestDTO.getAttachments() != null && !requestDTO.getAttachments().isEmpty()) {
            for (var attDTO : requestDTO.getAttachments()) {
                PostAttachment attachment = new PostAttachment();
                attachment.setFileUrl(attDTO.getFileUrl());
                attachment.setFileType(attDTO.getFileType());
                // Thiết lập mối quan hệ 2 chiều
                post.addAttachment(attachment);
            }
        }

        Post savedPost = postRepository.save(post);
        return convertToPostResponseDTO(savedPost, currentUser);
    }

    @Transactional
    public PostResponseDTO updatePost(long id, PostRequestDTO requestDTO)
            throws ResourceNotFoundException, BadRequestException {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated.");
        }

        Post postDB = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        // Chỉ chủ sở hữu bài đăng mới có quyền sửa
        if (postDB.getUser().getId() != currentUser.getId()) {
            throw new BadRequestException("You do not have permission to update this post.");
        }

        postDB.setCaption(requestDTO.getCaption());

        Post updatedPost = postRepository.save(postDB);
        return convertToPostResponseDTO(updatedPost, currentUser);
    }

}
