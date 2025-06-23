package com.fourstars.FourStars.integration.controller;

import com.fourstars.FourStars.domain.Role;
import com.fourstars.FourStars.repository.RoleRepository;
import com.fourstars.FourStars.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void getAllRoles_whenAuthenticatedAsAdmin_shouldReturnRoleList() throws Exception {
        roleRepository.save(new Role(0, "ADMIN", "Admin role", true, null, null, null, null, null, null));
        roleRepository.save(new Role(0, "USER", "User role", true, null, null, null, null, null, null));

        mockMvc.perform(get("/api/v1/admin/roles")
                .with(user("admin-test@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result", hasSize(2)));
    }

    @Test
    void getAllRoles_whenAuthenticatedAsUser_shouldReturn403Forbidden() throws Exception {

        mockMvc.perform(get("/api/v1/admin/roles")
                .with(user("regular-user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }
}