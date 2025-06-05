package com.fourstars.FourStars.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.Plan;
import com.fourstars.FourStars.domain.request.plan.PlanRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.plan.PlanResponseDTO;
import com.fourstars.FourStars.service.PlanService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceInUseException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

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

    @GetMapping("/{id}")
    @ApiMessage("Get plan by id")
    public ResponseEntity<PlanResponseDTO> findById(@PathVariable("id") long id) {
        PlanResponseDTO plan = this.planService.findById(id);

        return ResponseEntity.ok(plan);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update an existing plan")
    public ResponseEntity<PlanResponseDTO> update(@PathVariable("id") long id,
            @RequestBody PlanRequestDTO planRequestDTO) throws ResourceNotFoundException, DuplicateResourceException {
        PlanResponseDTO plan = this.planService.update(id, planRequestDTO);
        return ResponseEntity.ok(plan);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a plan")
    public ResponseEntity<Void> deletePlan(@PathVariable long id)
            throws ResourceNotFoundException, ResourceInUseException {
        planService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @ApiMessage("Fetch all plans with pagination")
    public ResponseEntity<ResultPaginationDTO<PlanResponseDTO>> getAllPlans(Pageable pageable) {
        ResultPaginationDTO<PlanResponseDTO> result = planService.fetchAll(pageable);
        return ResponseEntity.ok(result);
    }
}
