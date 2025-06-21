package com.fourstars.FourStars.domain;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fourstars.FourStars.util.SecurityUtil;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vocabularies", indexes = {
        @Index(name = "idx_vocabulary_categoryid", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Word cannot be blank")
    @Size(min = 1, max = 150, message = "Word must be between 1 and 150 characters")
    @Column(nullable = false, length = 150)
    private String word;

    @Size(max = 10000, message = "English definition is too long")
    @Column(name = "definition_en", columnDefinition = "TEXT")
    private String definitionEn;

    @Size(max = 10000, message = "Vietnamese meaning is too long")
    @Column(name = "meaning_vi", columnDefinition = "TEXT")
    private String meaningVi;

    @Size(max = 10000, message = "English example is too long")
    @Column(name = "example_en", columnDefinition = "TEXT")
    private String exampleEn;

    @Size(max = 10000, message = "Vietnamese example is too long")
    @Column(name = "example_vi", columnDefinition = "TEXT")
    private String exampleVi;

    @Size(max = 50, message = "Part of speech is too long")
    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @Size(max = 100, message = "Pronunciation is too long")
    @Column(length = 100)
    private String pronunciation;

    @Size(max = 2048, message = "Image URL is too long")
    @Column(length = 2048)
    private String image;

    @Size(max = 2048, message = "Audio URL is too long")
    @Column(length = 2048)
    private String audio;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @NotNull(message = "Category cannot be null for a vocabulary")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @OneToMany(mappedBy = "vocabulary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserVocabulary> userLearningProgress;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse(null);
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse(null);
        this.updatedAt = Instant.now();
    }
}
