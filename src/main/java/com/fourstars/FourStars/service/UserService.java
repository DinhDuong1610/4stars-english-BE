package com.fourstars.FourStars.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
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
import com.fourstars.FourStars.domain.request.auth.ForgotPasswordRequestDTO;
import com.fourstars.FourStars.domain.request.auth.RegisterRequestDTO;
import com.fourstars.FourStars.domain.request.auth.ResetPasswordRequestDTO;
import com.fourstars.FourStars.domain.request.user.ChangePasswordRequestDTO;
import com.fourstars.FourStars.domain.request.user.CreateUserRequestDTO;
import com.fourstars.FourStars.domain.request.user.UpdateUserRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.auth.ResCreateUserDTO;
import com.fourstars.FourStars.domain.response.auth.ResLoginDTO;
import com.fourstars.FourStars.domain.response.badge.BadgeResponseDTO;
import com.fourstars.FourStars.domain.response.dashboard.DashboardResponseDTO;
import com.fourstars.FourStars.domain.response.permission.UserPermissionDTO;
import com.fourstars.FourStars.domain.response.plan.PlanResponseDTO;
import com.fourstars.FourStars.domain.response.user.UserResponseDTO;
import com.fourstars.FourStars.repository.BadgeRepository;
import com.fourstars.FourStars.repository.PlanRepository;
import com.fourstars.FourStars.repository.RoleRepository;
import com.fourstars.FourStars.repository.SubscriptionRepository;
import com.fourstars.FourStars.repository.UserQuizAttemptRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.repository.UserVocabularyRepository;
import com.fourstars.FourStars.repository.projection.LeaderboardProjection;
import com.fourstars.FourStars.util.SecurityUtil;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BadgeRepository badgeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserVocabularyRepository userVocabularyRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SecurityUtil securityUtil;
    private final CacheManager cacheManager;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${fourstars.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public UserService(UserRepository userRepository,
            RoleRepository roleRepository,
            BadgeRepository badgeRepository,
            PasswordEncoder passwordEncoder,
            UserVocabularyRepository userVocabularyRepository,
            UserQuizAttemptRepository userQuizAttemptRepository,
            PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            SecurityUtil securityUtil, CacheManager cacheManager,
            EmailService emailService, RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.badgeRepository = badgeRepository;
        this.passwordEncoder = passwordEncoder;
        this.userVocabularyRepository = userVocabularyRepository;
        this.userQuizAttemptRepository = userQuizAttemptRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.securityUtil = securityUtil;
        this.cacheManager = cacheManager;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
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
    public ResLoginDTO loginWithGoogle(String code) {
        logger.info("Attempting to log in user with Google Authorization Code.");
        if (code == null || code.isBlank()) {
            logger.error("Google Authorization Code is null or empty.");
            throw new BadRequestException("Google Authorization Code cannot be empty.");
        }

        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    googleClientId,
                    googleClientSecret,
                    code,
                    "postmessage").execute();

            String idTokenString = tokenResponse.getIdToken();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new BadRequestException("Invalid Google ID Token obtained from code exchange.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            logger.info("Google Token exchange successful for email: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        logger.info("User with email {} not found. Creating a new account from Google login.", email);
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                        newUser.setActive(true);
                        newUser.setPoint(0);
                        Badge badge = badgeRepository.findById(1L)
                                .orElseThrow(
                                        () -> new ResourceNotFoundException("Badge not found with id: " + 1L));
                        newUser.setBadge(badge);

                        Role role = roleRepository.findById(2L)
                                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + 2L));
                        newUser.setRole(role);

                        Role userRole = roleRepository.findByName("USER")
                                .orElseThrow(() -> new RuntimeException("Default 'USER' role not found."));
                        newUser.setRole(userRole);
                        return userRepository.save(newUser);
                    });

            ResLoginDTO res = new ResLoginDTO();
            ResLoginDTO.UserLogin userLoginDTO = buildUserLoginDTO(user);
            res.setUser(userLoginDTO);

            String accessToken = this.securityUtil.createAccessToken(user.getEmail(), res);
            res.setAccessToken(accessToken);

            return res;

        } catch (Exception e) {
            logger.error("!!! Google ID Token verification failed !!!", e);
            throw new BadRequestException("Failed to verify Google Token: " + e.getMessage());
        }
    }

    private ResLoginDTO.UserLogin buildUserLoginDTO(User user) {
        ResLoginDTO.UserLogin dto = new ResLoginDTO.UserLogin(user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getStreakCount(),
                user.getPoint(),
                user.getBadge());
        return dto;
    }

    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO requestDTO)
            throws DuplicateResourceException, ResourceNotFoundException {
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Email '" + requestDTO.getEmail() + "' already exists.");
        }
        logger.info("Admin attempting to create a new user with email: {}", requestDTO.getEmail());

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
        logger.info("Admin successfully created new user with ID: {}", savedUser.getId());

        return convertToUserResponseDTO(savedUser);
    }

    @Transactional
    public List<UserResponseDTO> createBulkUsers(List<CreateUserRequestDTO> requestDTOs)
            throws DuplicateResourceException {
        logger.info("Admin request to create {} users in bulk", requestDTOs.size());

        if (requestDTOs == null || requestDTOs.isEmpty()) {
            throw new BadRequestException("User list cannot be empty.");
        }

        Set<String> emailsInRequest = new HashSet<>();
        for (CreateUserRequestDTO dto : requestDTOs) {
            if (!emailsInRequest.add(dto.getEmail().toLowerCase())) {
                throw new DuplicateResourceException("Duplicate email found in the request list: " + dto.getEmail());
            }
        }

        List<User> existingUsers = userRepository.findByEmailIn(emailsInRequest);
        if (!existingUsers.isEmpty()) {
            String existingEmails = existingUsers.stream()
                    .map(User::getEmail)
                    .collect(Collectors.joining(", "));
            throw new DuplicateResourceException("The following emails already exist: " + existingEmails);
        }

        List<User> usersToSave = new ArrayList<>();
        for (CreateUserRequestDTO dto : requestDTOs) {
            User user = new User();
            user.setName(dto.getName());
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setActive(dto.isActive());
            user.setPoint(0);

            Badge badge = badgeRepository.findById(dto.getBadgeId() != null ? dto.getBadgeId() : 1L)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Badge not found with id: " + dto.getBadgeId()));
            user.setBadge(badge);

            Role role = roleRepository.findById(dto.getRoleId() != null ? dto.getRoleId() : 2L)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + dto.getRoleId()));
            user.setRole(role);

            usersToSave.add(user);
        }

        List<User> savedUsers = userRepository.saveAll(usersToSave);
        logger.info("Successfully created {} new users in bulk.", savedUsers.size());

        return savedUsers.stream()
                .map(this::convertToUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO fetchUserById(long id) throws ResourceNotFoundException {
        logger.debug("Fetching user by ID: {}", id);

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
        logger.info("Admin attempting to update user with ID: {}", id);

        String oldEmail = userDB.getEmail();

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

        Cache cache = cacheManager.getCache("user_permissions");
        if (cache != null) {
            cache.evict(oldEmail);
            if (requestDTO.getEmail() != null && !oldEmail.equals(requestDTO.getEmail())) {
                cache.evict(requestDTO.getEmail());
            }
        }
        logger.info("Successfully updated user with ID: {}. Evicting cache for email: {}", updatedUser.getId(),
                updatedUser.getEmail());

        return convertToUserResponseDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(long id) throws ResourceNotFoundException {
        logger.info("Admin attempting to delete user with ID: {}", id);

        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(userToDelete);
        logger.info("Successfully deleted user with ID: {}", id);

    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<UserResponseDTO> fetchAllUsers(Pageable pageable, String name, String email,
            Boolean active, String role, LocalDate startCreatedAt, LocalDate endCreatedAt) {
        logger.debug("Fetching all users, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + name.trim().toLowerCase() + "%"));
            }
            if (email != null && !email.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                        "%" + email.trim().toLowerCase() + "%"));
            }
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }
            if (role != null && !role.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("role").get("name"), role));
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

        Page<User> pageUser = userRepository.findAll(spec, pageable);
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
    @Cacheable(value = "user_permissions", key = "#email")
    public UserPermissionDTO getPermissionsByEmail(String email) {
        logger.debug("Fetching permissions for user: '{}'. Checking cache 'user_permissions' first.", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (user.getRole() == null) {
            return new UserPermissionDTO("USER", Set.of());
        }

        Set<String> userPermissions = user.getRole().getPermissions().stream()
                .map(p -> p.getMethod() + ":" + p.getApiPath())
                .collect(Collectors.toSet());

        return new UserPermissionDTO(user.getRole().getName(), userPermissions);
    }

    public User getUserEntityByEmail(String email) throws ResourceNotFoundException {

        Cache cache = cacheManager.getCache("user_permissions");
        if (cache != null) {
            User cachedUser = cache.get(email, User.class);
            if (cachedUser != null) {
                return cachedUser;
            }
        }

        User userFromDb = this.findUserByEmailInDatabase(email);

        if (cache != null) {
            cache.put(email, userFromDb);
        }
        return userFromDb;
    }

    @Transactional(readOnly = true)
    protected User findUserByEmailInDatabase(String email) {
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
        logger.debug("Loading user by username for Spring Security context: {}", email);

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
        logger.info("Updating refresh token for user: {}", email);

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
        logger.info("New user registration attempt for email: {}", registerDTO.getEmail());

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new DuplicateResourceException("Email '" + registerDTO.getEmail() + "' already exists.");
        }

        User newUser = new User();
        newUser.setName(registerDTO.getName());
        newUser.setEmail(registerDTO.getEmail());

        newUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        newUser.setActive(true);
        newUser.setPoint(0);
        Badge badge = badgeRepository.findById(1L)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Badge not found with id: " + 1L));
        newUser.setBadge(badge);

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default 'USER' role not found."));
        newUser.setRole(userRole);

        User savedUser = userRepository.save(newUser);
        logger.info("Successfully registered new user with ID: {}", savedUser.getId());

        return convertToUserResponseDTO(savedUser);
    }

    @Transactional
    public void changeCurrentUserPassword(ChangePasswordRequestDTO requestDTO) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User '{}' requesting to change password.", currentUser.getEmail());

        if (!passwordEncoder.matches(requestDTO.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Incorrect current password.");
        }

        String newHashedPassword = passwordEncoder.encode(requestDTO.getNewPassword());

        currentUser.setPassword(newHashedPassword);
        userRepository.save(currentUser);

        logger.info("User '{}' successfully changed password.", currentUser.getEmail());
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<UserResponseDTO> getLeaderboard(Pageable pageable, Long badgeId) {
        logger.debug("Fetching leaderboard data, page: {}, badgeId: {}", pageable.getPageNumber(), badgeId);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        Instant startOfWeek = monday.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfWeek = sunday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        Page<LeaderboardProjection> leaderboardPage = userQuizAttemptRepository.findWeeklyLeaderboard(
                startOfWeek, endOfWeek, badgeId, pageable);

        List<UserResponseDTO> userDTOs = leaderboardPage.getContent().stream()
                .filter(p -> p.getUserId() != null)
                .map(projection -> {
                    UserResponseDTO dto = new UserResponseDTO();
                    dto.setId(projection.getUserId());
                    dto.setName(projection.getName());
                    dto.setEmail(projection.getEmail());
                    dto.setPoint(projection.getWeeklyPoints());
                    User user = userRepository.findById(projection.getUserId()).orElse(null);
                    if (user != null) {
                        dto.setStreakCount(user.getStreakCount());
                        dto.setBadge(new UserResponseDTO.BadgeInfoDTO(user.getBadge().getId(),
                                user.getBadge().getName(), user.getBadge().getImage()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                leaderboardPage.getNumber() + 1,
                leaderboardPage.getSize(),
                leaderboardPage.getTotalPages(),
                leaderboardPage.getTotalElements());

        return new ResultPaginationDTO<>(meta, userDTOs);
    }

    @Transactional(readOnly = true)
    public DashboardResponseDTO getUserDashboard() {
        User currentUser = getCurrentAuthenticatedUser();
        logger.debug("Fetching dashboard data for user: {}", currentUser.getEmail());

        DashboardResponseDTO dashboard = new DashboardResponseDTO();

        dashboard.setTotalVocabulary(userVocabularyRepository.countByUser(currentUser));

        Map<Integer, Integer> levelCounts = new HashMap<>();
        List<UserVocabulary> userVocabularies = userVocabularyRepository.findByUser(currentUser);
        for (UserVocabulary uv : userVocabularies) {
            levelCounts.put(uv.getLevel(), levelCounts.getOrDefault(uv.getLevel(), 0) + 1);
        }
        dashboard.setVocabularyLevelCounts(levelCounts);

        dashboard.setTotalQuizzesCompleted(userQuizAttemptRepository.countByUser(currentUser));

        Double averageScore = userQuizAttemptRepository.calculateAverageScoreByUser(currentUser);
        dashboard.setAverageQuizScore(averageScore != null ? averageScore : 0.0);

        dashboard.setCurrentStreak(currentUser.getStreakCount() != null ? currentUser.getStreakCount() : 0);

        if (currentUser.getBadge() != null) {
            dashboard.setBadges(this.convertToBadgeResponseDTO(currentUser.getBadge()));
        }

        dashboard.setUserPoints(currentUser.getPoint() != 0 ? currentUser.getPoint() : 0);

        long reviewCount = userVocabularyRepository.countByUserAndNextReviewAtBefore(currentUser, Instant.now());
        dashboard.setWordsToReviewCount(reviewCount);

        Subscription currentSubscription = subscriptionRepository.findTopByUserOrderByEndDateDesc(currentUser)
                .orElse(null);
        if (currentSubscription != null && currentSubscription.getEndDate().isAfter(Instant.now())
                && currentSubscription.isActive()) {
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

        logger.debug("Dashboard data composed for user: {}", currentUser.getEmail());
        return dashboard;
    }

    @Transactional
    public void checkAndAwardBadge(User user) {
        Badge bestBadge = badgeRepository.findTopByPointLessThanEqualOrderByPointDesc(user.getPoint()).orElse(null);

        if (bestBadge != null && !bestBadge.equals(user.getBadge())) {
            logger.info("Awarding new badge '{}' to user '{}'. Old badge was: '{}'",
                    bestBadge.getName(),
                    user.getEmail(),
                    user.getBadge() != null ? user.getBadge().getName() : "None");

            user.setBadge(bestBadge);
        }
    }

    @Transactional(readOnly = true)
    public void handleForgotPassword(ForgotPasswordRequestDTO requestDTO) {
        String email = requestDTO.getEmail();
        logger.info("Forgot password request received for email: {}", email);

        userRepository.findByEmail(email).ifPresent(user -> {
            String otp = String.format("%06d", new Random().nextInt(999999));
            String redisKey = "otp:" + email;

            redisTemplate.opsForValue().set(redisKey, otp, 5, TimeUnit.MINUTES);
            logger.debug("Saved OTP for email {} to Redis. Key: {}", email, redisKey);

            emailService.sendOtpEmail(email, otp);
        });
    }

    @Transactional
    public void handleResetPassword(ResetPasswordRequestDTO requestDTO) {
        String email = requestDTO.getEmail();
        String otp = requestDTO.getOtp();
        String redisKey = "otp:" + email;

        logger.info("Reset password attempt for email: {}", email);

        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new BadRequestException("Invalid or expired OTP.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));
        userRepository.save(user);
        logger.info("Successfully reset password for user: {}", email);

        redisTemplate.delete(redisKey);
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
