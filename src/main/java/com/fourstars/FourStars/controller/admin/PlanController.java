package com.fourstars.FourStars.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequestMapping("/api/v1/admin/plans")
@Tag(name = "Admin - Plan Management API", description = "APIs for managing subscription plans")
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @Operation(summary = "Create a new subscription plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plan created successfully"),
            @ApiResponse(responseCode = "409", description = "Plan name already exists")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Create a new plan (course package)")
    public ResponseEntity<PlanResponseDTO> create(@Valid @RequestBody PlanRequestDTO planRequestDTO)
            throws DuplicateResourceException {
        PlanResponseDTO plan = this.planService.create(planRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    @Operation(summary = "Get a plan by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved plan"),
            @ApiResponse(responseCode = "404", description = "Plan not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get plan by id")
    public ResponseEntity<PlanResponseDTO> findById(@PathVariable("id") long id) {
        PlanResponseDTO plan = this.planService.findById(id);

        return ResponseEntity.ok(plan);
    }

    @Operation(summary = "Update an existing plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan updated successfully"),
            @ApiResponse(responseCode = "404", description = "Plan not found"),
            @ApiResponse(responseCode = "409", description = "Plan name already exists for another plan")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Update an existing plan")
    public ResponseEntity<PlanResponseDTO> update(@PathVariable("id") long id,
            @RequestBody PlanRequestDTO planRequestDTO) throws ResourceNotFoundException, DuplicateResourceException {
        PlanResponseDTO plan = this.planService.update(id, planRequestDTO);
        return ResponseEntity.ok(plan);
    }

    @Operation(summary = "Delete a plan", description = "Deletes a plan. Fails if the plan has active subscriptions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plan deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Plan not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete plan, it is in use by subscriptions")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Delete a plan")
    public ResponseEntity<Void> deletePlan(@PathVariable long id)
            throws ResourceNotFoundException, ResourceInUseException {
        planService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all plans with pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch all plans with pagination")
    public ResponseEntity<ResultPaginationDTO<PlanResponseDTO>> getAllPlans(Pageable pageable,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "minDuration", required = false) Integer minDuration,
            @RequestParam(name = "maxDuration", required = false) Integer maxDuration,
            @RequestParam(name = "startCreatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startCreatedAt,
            @RequestParam(name = "endCreatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endCreatedAt) {
        ResultPaginationDTO<PlanResponseDTO> result = planService.fetchAll(pageable, name, minPrice, maxPrice,
                minDuration, maxDuration, active, startCreatedAt, endCreatedAt);
        return ResponseEntity.ok(result);
    }
}
