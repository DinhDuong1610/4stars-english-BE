package com.fourstars.FourStars.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Badge;
import com.fourstars.FourStars.domain.request.badge.BadgeRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.badge.BadgeResponseDTO;
import com.fourstars.FourStars.repository.BadgeRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class BadgeService {
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
    public BadgeResponseDTO createBadge(BadgeRequestDTO badgeRequestDTO) throws DuplicateResourceException {
        if (badgeRepository.existsByName(badgeRequestDTO.getName())) {
            throw new DuplicateResourceException("Badge name '" + badgeRequestDTO.getName() + "' already exists.");
        }

        Badge badge = new Badge();
        badge.setName(badgeRequestDTO.getName());
        badge.setImage(badgeRequestDTO.getImage());
        badge.setPoint(badgeRequestDTO.getPoint());
        badge.setDescription(badgeRequestDTO.getDescription());

        Badge savedBadge = badgeRepository.save(badge);
        return convertToBadgeResponseDTO(savedBadge);
    }

}
