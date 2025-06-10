package com.fourstars.FourStars.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Like;
import com.fourstars.FourStars.domain.Post;
import com.fourstars.FourStars.domain.PostAttachment;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.request.post.PostRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.post.PostResponseDTO;
import com.fourstars.FourStars.repository.CommentRepository;
import com.fourstars.FourStars.repository.LikeRepository;
import com.fourstars.FourStars.repository.PostRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.util.SecurityUtil;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
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
                attDto.setOriginalFileName(att.getOriginalFileName());
                attDto.setFileSize(att.getFileSize());
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
                attachment.setOriginalFileName(attDTO.getOriginalFileName());
                attachment.setFileSize(attDTO.getFileSize());
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

    @Transactional
    public void deletePost(long id) throws ResourceNotFoundException, BadRequestException {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated.");
        }

        Post postToDelete = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        // Chỉ chủ sở hữu hoặc admin mới có quyền xóa
        // if (postToDelete.getUser().getId() != currentUser.getId() &&
        // !SecurityUtil.isAdmin(authentication)) {
        // throw new BadRequestException("You do not have permission to delete this
        // post.");
        // }
        // Hiện tại chỉ check chủ sở hữu
        if (postToDelete.getUser().getId() != currentUser.getId()) {
            throw new BadRequestException("You do not have permission to delete this post.");
        }

        postRepository.delete(postToDelete);
    }

    @Transactional(readOnly = true)
    public PostResponseDTO fetchPostById(long id) throws ResourceNotFoundException {
        User currentUser = getCurrentAuthenticatedUser();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        return convertToPostResponseDTO(post, currentUser);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<PostResponseDTO> fetchAllPosts(Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();

        Page<Post> pagePost = postRepository.findAll(pageable);
        List<PostResponseDTO> postDTOs = pagePost.getContent().stream()
                .map(post -> convertToPostResponseDTO(post, currentUser))
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pagePost.getTotalPages(),
                pagePost.getTotalElements());
        return new ResultPaginationDTO<>(meta, postDTOs);
    }

    @Transactional
    public void handleLikePost(long postId) throws ResourceNotFoundException, DuplicateResourceException {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (likeRepository.findByUserIdAndPostId(currentUser.getId(), postId).isPresent()) {
            throw new DuplicateResourceException("You have already liked this post.");
        }

        Like newLike = new Like(currentUser, post);
        likeRepository.save(newLike);
    }

    @Transactional
    public void handleUnlikePost(long postId) throws ResourceNotFoundException {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated.");
        }

        Like like = likeRepository.findByUserIdAndPostId(currentUser.getId(), postId)
                .orElseThrow(() -> new ResourceNotFoundException("You have not liked this post."));

        likeRepository.delete(like);
    }
}
