package com.fourstars.FourStars.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourstars.FourStars.domain.Role;
import com.fourstars.FourStars.domain.request.plan.PlanRequestDTO;
import com.fourstars.FourStars.repository.PlanRepository;
import com.fourstars.FourStars.repository.RoleRepository;
import com.fourstars.FourStars.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        planRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void createPlan_whenAuthenticatedAsAdmin_shouldSucceed() throws Exception {
        roleRepository.save(new Role(0, "ADMIN", "Admin role", true, null, null, null, null, null, null));

        PlanRequestDTO newPlanRequest = new PlanRequestDTO();
        newPlanRequest.setName("Gói Premium 1 năm");
        newPlanRequest.setDescription("Truy cập toàn bộ nội dung trong 1 năm.");
        newPlanRequest.setPrice(new BigDecimal("999000"));
        newPlanRequest.setDurationInDays(365);
        newPlanRequest.setActive(true);

        mockMvc.perform(post("/api/v1/admin/plans")
                .with(user("admin@test.com").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPlanRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Gói Premium 1 năm")))
                .andExpect(jsonPath("$.data.price", is(999000)));

        assertEquals(1, planRepository.count());
    }

    @Test
    void createPlan_whenAuthenticatedAsUser_shouldReturn403Forbidden() throws Exception {
        PlanRequestDTO newPlanRequest = new PlanRequestDTO();
        newPlanRequest.setName("Gói Premium 1 năm");

        mockMvc.perform(post("/api/v1/admin/plans")
                .with(user("user@test.com").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPlanRequest)))
                .andExpect(status().isForbidden());
    }
}
