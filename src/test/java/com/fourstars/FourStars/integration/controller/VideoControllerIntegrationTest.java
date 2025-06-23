package com.fourstars.FourStars.integration.controller;

import com.fourstars.FourStars.domain.*;
import com.fourstars.FourStars.repository.*;
import com.fourstars.FourStars.util.constant.CategoryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VideoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User regularUser;
    private User premiumUser;
    private Video testVideo;

    @BeforeEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void setUp() {
        videoRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        Role userRole = new Role();
        userRole.setName("USER");
        roleRepository.save(userRole);

        Role premiumRole = new Role();
        premiumRole.setName("PREMIUM");
        roleRepository.save(premiumRole);

        Permission viewVideoPermission = new Permission();
        viewVideoPermission.setName("View Video Detail");
        viewVideoPermission.setApiPath("/api/v1/videos/{id}");
        viewVideoPermission.setMethod("GET");
        viewVideoPermission.setModule("Video");
        permissionRepository.save(viewVideoPermission);

        premiumRole.setPermissions(List.of(viewVideoPermission));
        roleRepository.save(premiumRole);

        regularUser = new User();
        regularUser.setEmail("user@example.com");
        regularUser.setName("Regular User");
        regularUser.setPassword(passwordEncoder.encode("password"));
        regularUser.setRole(userRole);
        userRepository.save(regularUser);

        premiumUser = new User();
        premiumUser.setEmail("premium@example.com");
        premiumUser.setName("Premium User");
        premiumUser.setPassword(passwordEncoder.encode("password"));
        premiumUser.setRole(premiumRole);
        userRepository.save(premiumUser);

        Category testCategory = new Category();
        testCategory.setName("Test Videos");
        testCategory.setType(CategoryType.VIDEO);
        categoryRepository.save(testCategory);

        testVideo = new Video();
        testVideo.setTitle("Test Video Title");
        testVideo.setUrl("http://example.com/video");
        testVideo.setCategory(testCategory);
        videoRepository.save(testVideo);
    }

    @Test
    void getVideoDetail_whenUserIsPremium_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/videos/{id}", testVideo.getId())
                .with(user(premiumUser.getEmail()).roles("PREMIUM")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title", is("Test Video Title")));
    }

}