package com.fourstars.FourStars.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Plan;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.request.plan.PlanRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.plan.PlanResponseDTO;
import com.fourstars.FourStars.repository.PlanRepository;
import com.fourstars.FourStars.repository.SubscriptionRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceInUseException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

@Service
@CacheConfig(cacheNames = "plans")
public class PlanService {
    private static final Logger logger = LoggerFactory.getLogger(PlanService.class);

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
    @CacheEvict(allEntries = true)
    public PlanResponseDTO create(PlanRequestDTO planRequestDTO) throws DuplicateResourceException {
        logger.info("Request to create new plan with name: '{}'", planRequestDTO.getName());

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
        logger.info("Successfully created new plan with ID: {}", plan.getId());

        return this.convertToPlanResponseDTO(plan);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public PlanResponseDTO findById(long id) {
        logger.debug("Request to fetch plan by ID: {}", id);

        Plan plan = this.planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

        return this.convertToPlanResponseDTO(plan);
    }

    @Transactional(readOnly = true)
    public Plan getPlanEntityById(long id) throws ResourceNotFoundException {
        logger.debug("Request to fetch plan ENTITY by ID: {}", id);

        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
    }

    @Transactional
    @CacheEvict(key = "#id")
    public PlanResponseDTO update(long id, PlanRequestDTO planRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        logger.info("Request to update plan with ID: {}", id);

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
        logger.info("Successfully updated plan with ID: {}", updatedPlan.getId());

        return this.convertToPlanResponseDTO(updatedPlan);
    }

    @Transactional
    @CacheEvict(key = "#id")
    public void delete(long id) throws ResourceNotFoundException, ResourceInUseException {
        logger.info("Request to delete plan with ID: {}", id);

        Plan planToDelete = this.getPlanEntityById(id);

        if (this.subscriptionRepository.existsByPlanIdAndActiveTrue(id)) {
            throw new ResourceInUseException("Plan '" + planToDelete.getName()
                    + "' is currently assigned to subscriptions and cannot be deleted.");
        }

        this.planRepository.delete(planToDelete);
        logger.info("Successfully deleted plan with ID: {}", id);

    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<PlanResponseDTO> fetchAll(Pageable pageable, String name, BigDecimal minPrice,
            BigDecimal maxPrice, Integer minDurationInDays, Integer maxDurationInDays, Boolean active,
            LocalDate startCreatedAt, LocalDate endCreatedAt) {
        logger.debug("Request to fetch all plans, page: {}, size: {}", pageable.getPageNumber(),
                pageable.getPageSize());

        Specification<Plan> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + name.trim().toLowerCase() + "%"));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (minDurationInDays != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("durationInDays"), minDurationInDays));
            }

            if (maxDurationInDays != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("durationInDays"), maxDurationInDays));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            if (startCreatedAt != null) {
                Instant startInstant = startCreatedAt.atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startInstant));
            }

            if (endCreatedAt != null) {
                Instant endInstant = endCreatedAt.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endInstant));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Plan> pagePlan = this.planRepository.findAll(spec, pageable);

        List<PlanResponseDTO> planDTOs = pagePlan.getContent().stream()
                .map(this::convertToPlanResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pagePlan.getTotalPages(),
                pagePlan.getTotalElements());
        logger.debug("Found {} plans on page {}/{}", planDTOs.size(), meta.getPage(), meta.getPages());

        return new ResultPaginationDTO<>(meta, planDTOs);
    }
}
