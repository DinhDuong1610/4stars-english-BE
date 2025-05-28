package com.fourstars.FourStars.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Badge;
import com.fourstars.FourStars.domain.Role;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.request.auth.RegisterRequestDTO;
import com.fourstars.FourStars.domain.request.user.CreateUserRequestDTO;
import com.fourstars.FourStars.domain.request.user.UpdateUserRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.auth.ResCreateUserDTO;
import com.fourstars.FourStars.domain.response.user.UserResponseDTO;
import com.fourstars.FourStars.repository.BadgeRepository;
import com.fourstars.FourStars.repository.RoleRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class UserService implements UserDetailsService {
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

    @Transactional(readOnly = true)
    public UserResponseDTO fetchUserById(long id) throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToUserResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(long id) throws ResourceNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public UserResponseDTO updateUser(long id, UpdateUserRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        User userDB = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (requestDTO.getEmail() != null && !userDB.getEmail().equalsIgnoreCase(requestDTO.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
                throw new DuplicateResourceException(
                        "Email '" + requestDTO.getEmail() + "' already exists for another user.");
            }
            userDB.setEmail(requestDTO.getEmail());
        }

        if (requestDTO.getName() != null) {
            userDB.setName(requestDTO.getName());
        }
        if (requestDTO.getActive() != null) {
            userDB.setActive(requestDTO.getActive());
        }
        if (requestDTO.getPoint() != null) {
            userDB.setPoint(requestDTO.getPoint());
        }

        if (requestDTO.getRoleId() != null) {
            Role role = roleRepository.findById(requestDTO.getRoleId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Role not found with id: " + requestDTO.getRoleId()));
            userDB.setRole(role);
        }

        if (requestDTO.getBadgeId() != null) {
            Badge badge = badgeRepository.findById(requestDTO.getBadgeId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Badge not found with id: " + requestDTO.getBadgeId()));
            userDB.setBadge(badge);
        }

        User updatedUser = userRepository.save(userDB);

        return convertToUserResponseDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(long id) throws ResourceNotFoundException {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(userToDelete);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<UserResponseDTO> fetchAllUsers(Pageable pageable) {
        Page<User> pageUser = userRepository.findAll(pageable);
        List<UserResponseDTO> userDTOs = pageUser.getContent().stream()
                .map(this::convertToUserResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pageUser.getTotalPages(),
                pageUser.getTotalElements());
        return new ResultPaginationDTO<>(meta, userDTOs);
    }

    @Transactional(readOnly = true)
    public User getUserEntityByEmail(String email) throws ResourceNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                true,
                authorities);
    }

    @Transactional(readOnly = true)
    public User handleGetUsername(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean isEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User handleCreateUser(User user) {
        if (user.getRole() != null && user.getRole().getId() != 0) {
            Role role = this.roleRepository.findById(user.getRole().getId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Role not found with ID: " + user.getRole().getId()));
            user.setRole(role);
        } else if (user.getRole() != null && user.getRole().getName() != null) {
            Role role = this.roleRepository.findByName(user.getRole().getName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Role not found with name: " + user.getRole().getName()));
            user.setRole(role);
        }

        return this.userRepository.save(user);
    }

    @Transactional
    public void updateUserToken(String refreshToken, String email) throws ResourceNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email + " for updating token."));
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getRefreshToken() != null && user.getRefreshToken().equals(refreshToken)) {
            return user;
        }
        return null;
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        if (user == null)
            return null;
        ResCreateUserDTO res = new ResCreateUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO fetchUserResponseById(long id) throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToUserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO registerNewUser(RegisterRequestDTO registerDTO)
            throws DuplicateResourceException, ResourceNotFoundException {
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new DuplicateResourceException("Email '" + registerDTO.getEmail() + "' already exists.");
        }

        User newUser = new User();
        newUser.setName(registerDTO.getName());
        newUser.setEmail(registerDTO.getEmail());

        newUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        newUser.setActive(true);
        newUser.setPoint(0);

        Long roleIdToAssign = registerDTO.getRoleId();
        Role assignedRole;

        if (roleIdToAssign == null) {
            assignedRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Default role 'USER' not found. Please ensure it exists in the database."));
        } else {

            assignedRole = roleRepository.findById(roleIdToAssign)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleIdToAssign));
        }
        newUser.setRole(assignedRole);

        User savedUser = userRepository.save(newUser);

        return convertToUserResponseDTO(savedUser);
    }

}
