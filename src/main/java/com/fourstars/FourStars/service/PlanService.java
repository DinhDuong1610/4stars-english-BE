package com.fourstars.FourStars.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Plan;
import com.fourstars.FourStars.domain.request.plan.PlanRequestDTO;
import com.fourstars.FourStars.domain.response.plan.PlanResponseDTO;
import com.fourstars.FourStars.repository.PlanRepository;
import com.fourstars.FourStars.repository.SubscriptionRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
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
}
