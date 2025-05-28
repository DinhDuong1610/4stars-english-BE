package com.fourstars.FourStars.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Badge;
import com.fourstars.FourStars.domain.Role;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.request.user.CreateUserRequestDTO;
import com.fourstars.FourStars.domain.request.user.UpdateUserRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.user.UserResponseDTO;
import com.fourstars.FourStars.repository.BadgeRepository;
import com.fourstars.FourStars.repository.RoleRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BadgeRepository badgeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            RoleRepository roleRepository,
            BadgeRepository badgeRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.badgeRepository = badgeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserResponseDTO convertToUserResponseDTO(User user) {
        if (user == null)
            return null;
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setActive(user.isActive());
        dto.setPoint(user.getPoint());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setUpdatedBy(user.getUpdatedBy());

        if (user.getRole() != null) {
            UserResponseDTO.RoleInfoDTO roleInfo = new UserResponseDTO.RoleInfoDTO();
            roleInfo.setId(user.getRole().getId());
            roleInfo.setName(user.getRole().getName());
            dto.setRole(roleInfo);
        }

        if (user.getBadge() != null) {
            UserResponseDTO.BadgeInfoDTO badgeInfo = new UserResponseDTO.BadgeInfoDTO();
            badgeInfo.setId(user.getBadge().getId());
            badgeInfo.setName(user.getBadge().getName());
            badgeInfo.setImage(user.getBadge().getImage());
            dto.setBadge(badgeInfo);
        }

        return dto;
    }

    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO requestDTO)
            throws DuplicateResourceException, ResourceNotFoundException {
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Email '" + requestDTO.getEmail() + "' already exists.");
        }

        User user = new User();
        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setActive(requestDTO.isActive());
        user.setPoint(requestDTO.getPoint());

        Role role = roleRepository.findById(requestDTO.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + requestDTO.getRoleId()));
        user.setRole(role);

        if (requestDTO.getBadgeId() != null) {
            Badge badge = badgeRepository.findById(requestDTO.getBadgeId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Badge not found with id: " + requestDTO.getBadgeId()));
            user.setBadge(badge);
        }

        User savedUser = userRepository.save(user);

        return convertToUserResponseDTO(savedUser);
    }

}
