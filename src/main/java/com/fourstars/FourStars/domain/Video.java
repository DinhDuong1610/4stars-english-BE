package com.fourstars.FourStars.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fourstars.FourStars.util.SecurityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "videos", indexes = {
        @Index(name = "idx_video_categoryid", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Video title cannot be blank")
    @Size(max = 255, message = "Video title cannot exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Video URL cannot be blank")
    @Size(max = 2048, message = "Video URL cannot exceed 2048 characters")
    @Column(nullable = false, length = 2048)
    private String url;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 20, message = "Duration format is too long")
    @Column(length = 20)
    private String duration;

    @Column(columnDefinition = "TEXT") // Có thể là URL tới file .srt hoặc nội dung phụ đề
    private String subtitle;

    @NotNull(message = "Category cannot be null for a video")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;

    @Size(max = 100, message = "Created by identifier is too long")
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @Size(max = 100, message = "Updated by identifier is too long")
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

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
