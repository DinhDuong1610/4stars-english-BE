package com.fourstars.FourStars.controller.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.plan.PlanResponseDTO;
import com.fourstars.FourStars.service.PlanService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("ClientPlanController")
@RequestMapping("/api/v1/plans")
@Tag(name = "Client - Plan Management API", description = "APIs for managing subscription plans")
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @Operation(summary = "Get a plan by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved plan"),
            @ApiResponse(responseCode = "404", description = "Plan not found")
    })
    @GetMapping("/{id}")
    @ApiMessage("Get plan by id")
    public ResponseEntity<PlanResponseDTO> findById(@PathVariable("id") long id) {
        PlanResponseDTO plan = this.planService.findById(id);

        return ResponseEntity.ok(plan);
    }

    @Operation(summary = "Get all plans with pagination")
    @GetMapping
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
