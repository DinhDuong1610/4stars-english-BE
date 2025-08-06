package com.fourstars.FourStars.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.domain.Like;
import com.fourstars.FourStars.domain.Post;
import com.fourstars.FourStars.domain.PostAttachment;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.request.post.PostRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.post.PostResponseDTO;
import com.fourstars.FourStars.messaging.dto.notification.NewLikeMessage;
import com.fourstars.FourStars.messaging.dto.post.PostLikeUpdateMessage;
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
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final RabbitTemplate rabbitTemplate;

    public PostService(PostRepository postRepository, UserRepository userRepository,
            LikeRepository likeRepository, CommentRepository commentRepository,
            RabbitTemplate rabbitTemplate) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.rabbitTemplate = rabbitTemplate;
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
        logger.info("User '{}' creating a new post.", currentUser.getEmail());

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
                post.addAttachment(attachment);
            }
        }

        Post savedPost = postRepository.save(post);
        logger.info("Successfully created new post with ID: {}", savedPost.getId());

        return convertToPostResponseDTO(savedPost, currentUser);
    }

    @Transactional
    public PostResponseDTO updatePost(long id, PostRequestDTO requestDTO)
            throws ResourceNotFoundException, BadRequestException {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated.");
        }
        logger.info("User '{}' attempting to update post with ID: {}", currentUser.getEmail(), id);

        Post postDB = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        if (postDB.getUser().getId() != currentUser.getId()) {
            throw new BadRequestException("You do not have permission to update this post.");
        }

        postDB.setCaption(requestDTO.getCaption());

        Post updatedPost = postRepository.save(postDB);
        logger.info("Successfully updated post with ID: {}", updatedPost.getId());

        return convertToPostResponseDTO(updatedPost, currentUser);
    }

    @Transactional
    public void deletePost(long id) throws ResourceNotFoundException, BadRequestException {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated.");
        }
        logger.info("User '{}' attempting to delete post with ID: {}", currentUser.getEmail(), id);

        Post postToDelete = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        if (postToDelete.getUser().getId() != currentUser.getId() && !currentUser.getRole().getName().equals("ADMIN")) {
            throw new BadRequestException("You do not have permission to delete this post.");
        }

        postRepository.delete(postToDelete);
        logger.info("Successfully deleted post with ID: {}", id);

    }

    @Transactional(readOnly = true)
    public PostResponseDTO fetchPostById(long id) throws ResourceNotFoundException {
        logger.debug("Fetching post by ID: {}", id);

        User currentUser = getCurrentAuthenticatedUser();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        return convertToPostResponseDTO(post, currentUser);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<PostResponseDTO> fetchAllPosts(Pageable pageable) {
        logger.debug("Fetching all posts for page: {}", pageable.getPageNumber());

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
        logger.debug("Found {} posts on page {}/{}", postDTOs.size(), meta.getPage(), meta.getPages());

        return new ResultPaginationDTO<>(meta, postDTOs);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<PostResponseDTO> fetchAllMyPosts(Pageable pageable) {
        logger.debug("Fetching all posts for page: {}", pageable.getPageNumber());

        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated.");
        }
        logger.info("User '{}' attempting to fetch their posts.", currentUser.getEmail());

        Page<Post> pagePost = postRepository.findAllByUserId(currentUser.getId(), pageable);
        List<PostResponseDTO> postDTOs = pagePost.getContent().stream()
                .map(post -> convertToPostResponseDTO(post, currentUser))
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pagePost.getTotalPages(),
                pagePost.getTotalElements());
        logger.debug("Found {} posts on page {}/{}", postDTOs.size(), meta.getPage(), meta.getPages());

        return new ResultPaginationDTO<>(meta, postDTOs);
    }

    @Transactional
    public void handleLikePost(long postId) throws ResourceNotFoundException, DuplicateResourceException {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated.");
        }
        logger.info("User '{}' attempting to like post ID: {}", currentUser.getEmail(), postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (likeRepository.findByUserIdAndPostId(currentUser.getId(), postId).isPresent()) {
            throw new DuplicateResourceException("You have already liked this post.");
        }

        Like newLike = new Like(currentUser, post);
        likeRepository.save(newLike);
        logger.info("User '{}' successfully liked post ID: {}", currentUser.getEmail(), postId);

        NewLikeMessage message = new NewLikeMessage(
                post.getUser().getId(),
                currentUser.getId(),
                post.getId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "notification.new.like",
                message);
        logger.info("Sent new_like notification message to RabbitMQ for recipient ID: {}", post.getUser().getId());

        publishLikeUpdateEvent(post, true);
    }

    @Transactional
    public void handleUnlikePost(long postId) throws ResourceNotFoundException {
        User currentUser = getCurrentAuthenticatedUser();

        if (currentUser == null) {
            throw new ResourceNotFoundException("User not authenticated.");
        }
        logger.info("User '{}' attempting to unlike post ID: {}", currentUser.getEmail(), postId);

        Like like = likeRepository.findByUserIdAndPostId(currentUser.getId(), postId)
                .orElseThrow(() -> new ResourceNotFoundException("You have not liked this post."));

        Post post = like.getPost();

        likeRepository.delete(like);
        logger.info("User '{}' successfully unliked post ID: {}", currentUser.getEmail(), postId);

        publishLikeUpdateEvent(post, false);
    }

    private void publishLikeUpdateEvent(Post post, boolean isLiked) {
        long totalLikes = likeRepository.countByPostId(post.getId());
        PostLikeUpdateMessage updateMessage = new PostLikeUpdateMessage(post.getId(), totalLikes, isLiked);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.POST_BROADCAST_EXCHANGE,
                RabbitMQConfig.POST_LIKE_UPDATE_ROUTING_KEY,
                updateMessage);
        logger.info("Published like update event for post ID {}. New count: {}", post.getId(), totalLikes);
    }
}
