package com.fourstars.FourStars.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourstars.FourStars.domain.Role;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.request.auth.RegisterRequestDTO;
import com.fourstars.FourStars.domain.request.auth.ReqLoginDTO;
import com.fourstars.FourStars.repository.RoleRepository;
import com.fourstars.FourStars.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setActive(true);
        roleRepository.save(userRole);
    }

    @Test
    void register_whenValidUserDetails_shouldReturn201CreatedAndSaveUser() throws Exception {
        Role userRole = roleRepository.findByName("USER").get();

        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setName("Dinh Duong");
        registerRequest.setEmail("dinhd.23it@vku.udn.vn");
        registerRequest.setPassword("123456789");
        registerRequest.setRoleId(userRole.getId());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email", is("dinhd.23it@vku.udn.vn")))
                .andExpect(jsonPath("$.data.name", is("Dinh Duong")))
                .andExpect(jsonPath("$.data.role.name", is("USER")));

        assertTrue(userRepository.existsByEmail("dinhd.23it@vku.udn.vn"));
    }

    @Test
    void login_whenValidCredentials_shouldReturn200AndToken() throws Exception {
        Role userRole = roleRepository.findByName("USER").get();
        User existingUser = new User();
        existingUser.setName("Dinh Duong");
        existingUser.setEmail("dinhd.23it@vku.udn.vn");
        existingUser.setPassword(passwordEncoder.encode("123456789"));
        existingUser.setRole(userRole);
        existingUser.setActive(true);
        userRepository.save(existingUser);

        ReqLoginDTO loginRequest = new ReqLoginDTO();
        loginRequest.setUsername("dinhd.23it@vku.udn.vn");
        loginRequest.setPassword("123456789");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.data.user.email", is("dinhd.23it@vku.udn.vn")));
    }
}