package com.fourstars.FourStars.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.Role;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.request.quiz.QuestionChoiceDTO;
import com.fourstars.FourStars.domain.request.quiz.QuestionDTO;
import com.fourstars.FourStars.domain.request.quiz.QuizDTO;
import com.fourstars.FourStars.repository.*;
import com.fourstars.FourStars.util.constant.CategoryType;
import com.fourstars.FourStars.util.constant.QuestionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuizControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository
                .save(new Role(0, "ADMIN", "Admin role", true, null, null, null, null, null, null));
        adminUser = new User();
        adminUser.setEmail("quiz-admin@example.com");
        adminUser.setName("Quiz Admin");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRole(adminRole);
        userRepository.save(adminUser);

        testCategory = new Category();
        testCategory.setName("Test Category for Quiz");
        testCategory.setType(CategoryType.GRAMMAR);
        categoryRepository.save(testCategory);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void createQuiz_whenAdmin_shouldSucceed() throws Exception {
        QuestionChoiceDTO choice1 = new QuestionChoiceDTO(0, "is", null, true);
        QuestionChoiceDTO choice2 = new QuestionChoiceDTO(0, "are", null, false);

        QuestionDTO question1 = new QuestionDTO();
        question1.setQuestionType(QuestionType.MULTIPLE_CHOICE_TEXT);
        question1.setPrompt("She ___ a teacher.");
        question1.setChoices(Set.of(choice1, choice2));

        QuizDTO newQuizRequest = new QuizDTO();
        newQuizRequest.setTitle("New Grammar Quiz");
        newQuizRequest.setCategoryId(testCategory.getId());
        newQuizRequest.setQuestions(Set.of(question1));

        mockMvc.perform(post("/api/v1/admin/quizzes")
                .with(user(adminUser.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newQuizRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title", is("New Grammar Quiz")))
                .andExpect(jsonPath("$.data.questions", hasSize(1)))
                .andExpect(jsonPath("$.data.questions[0].prompt", is("She ___ a teacher.")));

        assertEquals(1, quizRepository.count());
        assertEquals(1, questionRepository.count());
    }
}