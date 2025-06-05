package com.fourstars.FourStars.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.plan.PlanRequestDTO;
import com.fourstars.FourStars.domain.response.plan.PlanResponseDTO;
import com.fourstars.FourStars.service.PlanService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.DuplicateResourceException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/plans")
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    @ApiMessage("Create a new plan (course package)")
    public ResponseEntity<PlanResponseDTO> create(@Valid @RequestBody PlanRequestDTO planRequestDTO)
            throws DuplicateResourceException {
        PlanResponseDTO plan = this.planService.create(planRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }
}
