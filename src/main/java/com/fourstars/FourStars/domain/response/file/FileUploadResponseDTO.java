package com.fourstars.FourStars.domain.response.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileUploadResponseDTO {
    private String fileName;
    private String fileUrl;
    private String originalFileName;
    private long fileSize;
}
