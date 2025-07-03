package com.fourstars.FourStars.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Badge;
import com.fourstars.FourStars.domain.request.badge.BadgeRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.badge.BadgeResponseDTO;
import com.fourstars.FourStars.repository.BadgeRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceInUseException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

@Service
@CacheConfig(cacheNames = "badges")
public class BadgeService {
    private static final Logger logger = LoggerFactory.getLogger(BadgeService.class);

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;

    public BadgeService(BadgeRepository badgeRepository, UserRepository userRepository) {
        this.badgeRepository = badgeRepository;
        this.userRepository = userRepository;
    }

    private BadgeResponseDTO convertToBadgeResponseDTO(Badge badge) {
        if (badge == null)
            return null;
        BadgeResponseDTO dto = new BadgeResponseDTO();
        dto.setId(badge.getId());
        dto.setName(badge.getName());
        dto.setImage(badge.getImage());
        dto.setPoint(badge.getPoint());
        dto.setDescription(badge.getDescription());
        dto.setCreatedAt(badge.getCreatedAt());
        dto.setUpdatedAt(badge.getUpdatedAt());
        dto.setCreatedBy(badge.getCreatedBy());
        dto.setUpdatedBy(badge.getUpdatedBy());
        return dto;
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public BadgeResponseDTO createBadge(BadgeRequestDTO badgeRequestDTO) throws DuplicateResourceException {
        logger.info("Request to create new badge with name: '{}'", badgeRequestDTO.getName());

        if (badgeRepository.existsByName(badgeRequestDTO.getName())) {
            throw new DuplicateResourceException("Badge name '" + badgeRequestDTO.getName() + "' already exists.");
        }

        Badge badge = new Badge();
        badge.setName(badgeRequestDTO.getName());
        badge.setImage(badgeRequestDTO.getImage());
        badge.setPoint(badgeRequestDTO.getPoint());
        badge.setDescription(badgeRequestDTO.getDescription());

        Badge savedBadge = badgeRepository.save(badge);

        logger.info("Successfully created new badge with ID: {}", savedBadge.getId());

        return convertToBadgeResponseDTO(savedBadge);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public BadgeResponseDTO fetchBadgeById(long id) throws ResourceNotFoundException {
        logger.debug("Request to fetch badge with ID: {}", id);

        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found with id: " + id));
        return convertToBadgeResponseDTO(badge);
    }

    @Transactional
    @CacheEvict(key = "#id")
    public BadgeResponseDTO updateBadge(long id, BadgeRequestDTO badgeRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        logger.info("Request to update badge with ID: {}", id);

        Badge badgeDB = badgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found with id: " + id));

        if (!badgeDB.getName().equalsIgnoreCase(badgeRequestDTO.getName()) &&
                badgeRepository.existsByNameAndIdNot(badgeRequestDTO.getName(), id)) {
            throw new DuplicateResourceException(
                    "Badge name '" + badgeRequestDTO.getName() + "' already exists for another badge.");
        }

        badgeDB.setName(badgeRequestDTO.getName());
        badgeDB.setImage(badgeRequestDTO.getImage());
        badgeDB.setPoint(badgeRequestDTO.getPoint());
        badgeDB.setDescription(badgeRequestDTO.getDescription());

        Badge updatedBadge = badgeRepository.save(badgeDB);

        logger.info("Successfully updated badge with ID: {}", updatedBadge.getId());

        return convertToBadgeResponseDTO(updatedBadge);
    }

    @Transactional
    @CacheEvict(key = "#id")
    public void deleteBadge(long id) throws ResourceNotFoundException, ResourceInUseException {
        logger.info("Request to delete badge with ID: {}", id);

        Badge badgeToDelete = badgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found with id: " + id));

        if (userRepository.existsByBadgeId(id)) {
            throw new ResourceInUseException(
                    "Badge '" + badgeToDelete.getName() + "' is currently assigned to users and cannot be deleted.");
        }

        badgeRepository.delete(badgeToDelete);

        logger.info("Successfully deleted badge with ID: {}", id);

    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<BadgeResponseDTO> fetchAllBadges(Pageable pageable, String name,
            LocalDate startCreatedAt, LocalDate endCreatedAt) {
        logger.debug("Request to fetch all badges, page: {}, size: {}", pageable.getPageNumber(),
                pageable.getPageSize());

        Specification<Badge> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + name.trim().toLowerCase() + "%"));
            }

            if (startCreatedAt != null) {
                Instant startInstant = startCreatedAt.atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startInstant));
            }

            if (endCreatedAt != null) {
                Instant endInstant = endCreatedAt.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endInstant));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Badge> pageBadge = badgeRepository.findAll(spec, pageable);
        List<BadgeResponseDTO> badgeDTOs = pageBadge.getContent().stream()
                .map(this::convertToBadgeResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pageBadge.getTotalPages(),
                pageBadge.getTotalElements());

        logger.debug("Found {} badges on page {}/{}", badgeDTOs.size(), meta.getPage(), meta.getPages());

        return new ResultPaginationDTO<>(meta, badgeDTOs);
    }

}
