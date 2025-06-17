package com.fourstars.FourStars.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.quiz.QuizDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.service.QuizService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @ApiMessage("Create a new quiz")
    public ResponseEntity<QuizDTO> createQuiz(@Valid @RequestBody QuizDTO quizDTO) {
        QuizDTO createdQuiz = quizService.createQuiz(quizDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update an existing quiz")
    public ResponseEntity<QuizDTO> updateQuiz(@PathVariable("id") long id, @Valid @RequestBody QuizDTO quizDTO) {
        QuizDTO updatedQuiz = quizService.updateQuiz(id, quizDTO);
        return ResponseEntity.ok(updatedQuiz);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a quiz")
    public ResponseEntity<Void> deleteQuiz(@PathVariable("id") long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @ApiMessage("Get a quiz by id for admin")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable("id") long id) {
        return ResponseEntity.ok(quizService.getQuizForAdmin(id));
    }

    @GetMapping
    @ApiMessage("Get all quizzes for admin with filtering")
    public ResponseEntity<ResultPaginationDTO<QuizDTO>> getAllQuizzes(
            Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId) {

        return ResponseEntity.ok(quizService.getAllQuizzesForAdmin(pageable, categoryId));
    }
}
