package com.fourstars.FourStars.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.Grammar;
import com.fourstars.FourStars.domain.request.grammar.GrammarRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.grammar.GrammarResponseDTO;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.GrammarRepository;
import com.fourstars.FourStars.util.constant.CategoryType;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

@Service
public class GrammarService {
    private static final Logger logger = LoggerFactory.getLogger(GrammarService.class);

    private final GrammarRepository grammarRepository;
    private final CategoryRepository categoryRepository;

    public GrammarService(GrammarRepository grammarRepository, CategoryRepository categoryRepository) {
        this.grammarRepository = grammarRepository;
        this.categoryRepository = categoryRepository;
    }

    private GrammarResponseDTO convertToGrammarResponseDTO(Grammar grammar) {
        if (grammar == null)
            return null;
        GrammarResponseDTO dto = new GrammarResponseDTO();
        dto.setId(grammar.getId());
        dto.setName(grammar.getName());
        dto.setContent(grammar.getContent());

        if (grammar.getCategory() != null) {
            GrammarResponseDTO.CategoryInfoDTO catInfo = new GrammarResponseDTO.CategoryInfoDTO();
            catInfo.setId(grammar.getCategory().getId());
            catInfo.setName(grammar.getCategory().getName());
            dto.setCategory(catInfo);
        }

        dto.setCreatedAt(grammar.getCreatedAt());
        dto.setUpdatedAt(grammar.getUpdatedAt());
        dto.setCreatedBy(grammar.getCreatedBy());
        dto.setUpdatedBy(grammar.getUpdatedBy());
        return dto;
    }

    @Transactional
    public GrammarResponseDTO createGrammar(GrammarRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        logger.info("Request to create new grammar lesson with name: '{}' in category ID: {}", requestDTO.getName(),
                requestDTO.getCategoryId());

        if (grammarRepository.existsByNameAndCategoryId(requestDTO.getName(), requestDTO.getCategoryId())) {
            throw new DuplicateResourceException(
                    "A grammar lesson with the same name already exists in this category.");
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + requestDTO.getCategoryId()));

        if (category.getType() != CategoryType.GRAMMAR) {
            throw new BadRequestException("The selected category is not of type 'GRAMMAR'.");
        }

        String unsafeContent = requestDTO.getContent();
        String safeContent = Jsoup.clean(unsafeContent, Safelist.basicWithImages());

        Grammar grammar = new Grammar();
        grammar.setName(requestDTO.getName());
        grammar.setContent(safeContent);
        grammar.setCategory(category);

        Grammar savedGrammar = grammarRepository.save(grammar);

        logger.info("Successfully created new grammar lesson with ID: {}", savedGrammar.getId());

        return convertToGrammarResponseDTO(savedGrammar);
    }

    @Transactional
    public GrammarResponseDTO updateGrammar(long id, GrammarRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        logger.info("Request to update grammar lesson with ID: {}", id);

        Grammar grammarDB = grammarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grammar lesson not found with id: " + id));

        if (grammarRepository.existsByNameAndCategoryIdAndIdNot(requestDTO.getName(), requestDTO.getCategoryId(), id)) {
            throw new DuplicateResourceException(
                    "A grammar lesson with the same name already exists in this category.");
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + requestDTO.getCategoryId()));

        if (category.getType() != CategoryType.GRAMMAR) {
            throw new BadRequestException("The selected category is not of type 'GRAMMAR'.");
        }

        grammarDB.setName(requestDTO.getName());
        grammarDB.setContent(requestDTO.getContent());
        grammarDB.setCategory(category);

        Grammar updatedGrammar = grammarRepository.save(grammarDB);

        logger.info("Successfully updated grammar lesson with ID: {}", updatedGrammar.getId());

        return convertToGrammarResponseDTO(updatedGrammar);
    }

    @Transactional
    public void deleteGrammar(long id) throws ResourceNotFoundException {
        logger.info("Request to delete grammar lesson with ID: {}", id);

        Grammar grammarToDelete = grammarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grammar lesson not found with id: " + id));

        grammarRepository.delete(grammarToDelete);
        logger.info("Successfully deleted grammar lesson with ID: {}", id);

    }

    @Transactional(readOnly = true)
    public GrammarResponseDTO fetchGrammarById(long id) throws ResourceNotFoundException {
        logger.debug("Request to fetch grammar lesson by ID: {}", id);

        Grammar grammar = grammarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grammar lesson not found with id: " + id));
        return convertToGrammarResponseDTO(grammar);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<GrammarResponseDTO> fetchAllGrammars(Pageable pageable, Long categoryId, String name) {
        logger.debug("Request to fetch all grammar lessons with categoryId: {} and name: {}", categoryId, name);

        Specification<Grammar> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name.trim() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Grammar> pageGrammar = grammarRepository.findAll(spec, pageable);
        List<GrammarResponseDTO> grammarDTOs = pageGrammar.getContent().stream()
                .map(this::convertToGrammarResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pageGrammar.getTotalPages(),
                pageGrammar.getTotalElements());

        logger.debug("Found {} grammar lessons on page {}/{}", grammarDTOs.size(), meta.getPage(), meta.getPages());

        return new ResultPaginationDTO<>(meta, grammarDTOs);
    }
}
