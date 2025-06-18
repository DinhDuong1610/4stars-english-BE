package com.fourstars.FourStars.service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.fourstars.FourStars.domain.Plan;
import com.fourstars.FourStars.domain.Role;
import com.fourstars.FourStars.domain.Subscription;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.UserVocabulary;
import com.fourstars.FourStars.domain.request.auth.RegisterRequestDTO;
import com.fourstars.FourStars.domain.request.user.CreateUserRequestDTO;
import com.fourstars.FourStars.domain.request.user.UpdateUserRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.auth.ResCreateUserDTO;
import com.fourstars.FourStars.domain.response.badge.BadgeResponseDTO;
import com.fourstars.FourStars.domain.response.dashboard.DashboardResponseDTO;
import com.fourstars.FourStars.domain.response.plan.PlanResponseDTO;
import com.fourstars.FourStars.domain.response.user.UserResponseDTO;
import com.fourstars.FourStars.repository.BadgeRepository;
import com.fourstars.FourStars.repository.PlanRepository;
import com.fourstars.FourStars.repository.RoleRepository;
import com.fourstars.FourStars.repository.SubscriptionRepository;
import com.fourstars.FourStars.repository.UserQuizAttemptRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.repository.UserVocabularyRepository;
import com.fourstars.FourStars.util.SecurityUtil;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BadgeRepository badgeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserVocabularyRepository userVocabularyRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SecurityUtil securityUtil;

    public UserService(UserRepository userRepository,
            RoleRepository roleRepository,
            BadgeRepository badgeRepository,
            PasswordEncoder passwordEncoder,
            UserVocabularyRepository userVocabularyRepository,
            UserQuizAttemptRepository userQuizAttemptRepository,
            PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            SecurityUtil securityUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.badgeRepository = badgeRepository;
        this.passwordEncoder = passwordEncoder;
        this.userVocabularyRepository = userVocabularyRepository;
        this.userQuizAttemptRepository = userQuizAttemptRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.securityUtil = securityUtil;
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
        dto.setStreakCount(user.getStreakCount());
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

    @Transactional(readOnly = true)
    public ResultPaginationDTO<UserResponseDTO> getLeaderboard(Pageable pageable) {
        Page<User> userPage = this.userRepository.findAllByOrderByPointDesc(pageable);

        List<UserResponseDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToUserResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                userPage.getNumber() + 1,
                userPage.getSize(),
                userPage.getTotalPages(),
                userPage.getTotalElements());

        return new ResultPaginationDTO<>(meta, userDTOs);
    }

    public DashboardResponseDTO getUserDashboard() {
        User currentUser = getCurrentAuthenticatedUser();
        DashboardResponseDTO dashboard = new DashboardResponseDTO();

        // 1. Tổng số từ vựng trong sổ tay
        dashboard.setTotalVocabulary(userVocabularyRepository.countByUser(currentUser));

        // 2. Số lượng từ ở mỗi cấp độ SM-2
        Map<Integer, Integer> levelCounts = new HashMap<>();
        List<UserVocabulary> userVocabularies = userVocabularyRepository.findByUser(currentUser);
        for (UserVocabulary uv : userVocabularies) {
            levelCounts.put(uv.getLevel(), levelCounts.getOrDefault(uv.getLevel(), 0) + 1);
        }
        dashboard.setVocabularyLevelCounts(levelCounts);

        // 3. Số bài quiz đã hoàn thành
        dashboard.setTotalQuizzesCompleted(userQuizAttemptRepository.countByUser(currentUser));

        // 4. Điểm số trung bình của các bài quiz
        Double averageScore = userQuizAttemptRepository.calculateAverageScoreByUser(currentUser);
        dashboard.setAverageQuizScore(averageScore != null ? averageScore : 0.0);

        // 5. Chuỗi ngày học hiện tại
        dashboard.setCurrentStreak(currentUser.getStreakCount() != null ? currentUser.getStreakCount() : 0);

        // 6. Huy hiệu đã đạt được
        dashboard.setBadges(this.convertToBadgeResponseDTO(currentUser.getBadge()));

        // 7. Trạng thái gói học phí
        Subscription currentSubscription = subscriptionRepository.findTopByUserOrderByEndDateDesc(currentUser)
                .orElse(null);
        if (currentSubscription != null && currentSubscription.getEndDate().isAfter(Instant.now())) {
            PlanResponseDTO planDTO = convertToPlanResponseDTO(currentSubscription.getPlan());
            dashboard.setCurrentPlan(planDTO);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dashboard.setSubscriptionExpiryDate(currentSubscription.getEndDate()
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(formatter));
        } else {
            dashboard.setCurrentPlan(null);
            dashboard.setSubscriptionExpiryDate(null);
        }

        return dashboard;
    }

    private User getCurrentAuthenticatedUser() {
        return userRepository.findByEmail(securityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))).orElse(null);
    }

    private BadgeResponseDTO convertToBadgeResponseDTO(Badge badge) {
        BadgeResponseDTO dto = new BadgeResponseDTO();
        dto.setId(badge.getId());
        dto.setName(badge.getName());
        dto.setDescription(badge.getDescription());
        dto.setImage(badge.getImage());
        return dto;
    }

    private PlanResponseDTO convertToPlanResponseDTO(Plan plan) {
        PlanResponseDTO dto = new PlanResponseDTO();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setPrice(plan.getPrice());
        dto.setDurationInDays(plan.getDurationInDays());
        return dto;
    }
}
