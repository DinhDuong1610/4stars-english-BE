package com.fourstars.FourStars.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Plan;
import com.fourstars.FourStars.domain.request.plan.PlanRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.plan.PlanResponseDTO;
import com.fourstars.FourStars.repository.PlanRepository;
import com.fourstars.FourStars.repository.SubscriptionRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceInUseException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class PlanService {
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public PlanService(PlanRepository planRepository, SubscriptionRepository subscriptionRepository) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public PlanResponseDTO convertToPlanResponseDTO(Plan plan) {
        if (plan == null)
            return null;

        PlanResponseDTO dto = new PlanResponseDTO();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setPrice(plan.getPrice());
        dto.setDurationInDays(plan.getDurationInDays());
        dto.setActive(plan.isActive());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());
        dto.setCreatedBy(plan.getCreatedBy());
        dto.setUpdatedBy(plan.getUpdatedBy());

        return dto;
    }

    @Transactional
    public PlanResponseDTO create(PlanRequestDTO planRequestDTO) throws DuplicateResourceException {
        if (this.planRepository.existsByName(planRequestDTO.getName())) {
            throw new DuplicateResourceException("Plan name '" + planRequestDTO.getName() + "' already exists.");
        }

        Plan plan = new Plan();
        plan.setName(planRequestDTO.getName());
        plan.setDescription(planRequestDTO.getDescription());
        plan.setPrice(planRequestDTO.getPrice());
        plan.setDurationInDays(planRequestDTO.getDurationInDays());
        plan.setActive(planRequestDTO.isActive());

        plan = this.planRepository.save(plan);

        return this.convertToPlanResponseDTO(plan);
    }

    @Transactional(readOnly = true)
    public PlanResponseDTO findById(long id) {
        Plan plan = this.planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

        return this.convertToPlanResponseDTO(plan);
    }

    @Transactional(readOnly = true)
    public Plan getPlanEntityById(long id) throws ResourceNotFoundException {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
    }

    @Transactional
    public PlanResponseDTO update(long id, PlanRequestDTO planRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {

        Plan planDB = this.getPlanEntityById(id);

        if (!planDB.getName().equalsIgnoreCase(planRequestDTO.getName())) {
            if (this.planRepository.existsByNameAndIdNot(planRequestDTO.getName(), id)) {
                throw new DuplicateResourceException("Plan name '" + planRequestDTO.getName() + "' already exists.");
            }
        }

        planDB.setName(planRequestDTO.getName());
        planDB.setDescription(planRequestDTO.getDescription());
        planDB.setPrice(planRequestDTO.getPrice());
        planDB.setDurationInDays(planRequestDTO.getDurationInDays());
        planDB.setActive(planRequestDTO.isActive());

        Plan updatedPlan = this.planRepository.save(planDB);

        return this.convertToPlanResponseDTO(updatedPlan);
    }

    @Transactional
    public void delete(long id) throws ResourceNotFoundException, ResourceInUseException {
        Plan planToDelete = this.getPlanEntityById(id);

        if (this.subscriptionRepository.existsByPlanIdAndActiveTrue(id)) {
            throw new ResourceInUseException("Plan '" + planToDelete.getName()
                    + "' is currently assigned to subscriptions and cannot be deleted.");
        }

        this.planRepository.delete(planToDelete);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<PlanResponseDTO> fetchAll(Pageable pageable) {
        Page<Plan> pagePlan = this.planRepository.findAll(pageable);

        List<PlanResponseDTO> planDTOs = pagePlan.getContent().stream()
                .map(this::convertToPlanResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pagePlan.getTotalPages(),
                pagePlan.getTotalElements());

        return new ResultPaginationDTO<>(meta, planDTOs);
    }
}
