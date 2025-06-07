package com.fourstars.FourStars.domain.request.grammar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GrammarRequestDTO {

    @NotBlank(message = "Grammar lesson name cannot be blank")
    @Size(max = 255, message = "Grammar lesson name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Grammar content cannot be blank")
    private String content;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;
}
