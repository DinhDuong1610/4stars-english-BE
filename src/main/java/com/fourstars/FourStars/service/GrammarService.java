package com.fourstars.FourStars.service;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.Grammar;
import com.fourstars.FourStars.domain.request.grammar.GrammarRequestDTO;
import com.fourstars.FourStars.domain.response.grammar.GrammarResponseDTO;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.GrammarRepository;
import com.fourstars.FourStars.util.constant.CategoryType;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class GrammarService {
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

        // Làm sạch HTML trước khi lưu
        String unsafeContent = requestDTO.getContent();
        String safeContent = Jsoup.clean(unsafeContent, Safelist.basicWithImages());

        Grammar grammar = new Grammar();
        grammar.setName(requestDTO.getName());
        grammar.setContent(safeContent);
        grammar.setCategory(category);

        Grammar savedGrammar = grammarRepository.save(grammar);
        return convertToGrammarResponseDTO(savedGrammar);
    }
}
