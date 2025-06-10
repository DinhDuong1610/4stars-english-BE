package com.fourstars.FourStars.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fourstars.FourStars.util.constant.AttachmentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "comment_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Comment cannot be null for an attachment")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    @JsonIgnore
    private Comment comment;

    @NotBlank(message = "File URL cannot be blank")
    @Size(max = 2048, message = "File URL cannot exceed 2048 characters")
    @Column(name = "file_url", nullable = false, length = 2048)
    private String fileUrl;

    @NotNull(message = "File type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 50)
    private AttachmentType fileType;

    @Size(max = 255, message = "Original file name cannot exceed 255 characters")
    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "file_size") // Kích thước file tính bằng byte
    private long fileSize;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
    }
}
