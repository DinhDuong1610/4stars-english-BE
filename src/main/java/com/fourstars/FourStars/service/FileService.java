package com.fourstars.FourStars.service;

import com.fourstars.FourStars.util.error.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    public record SavedFileInfo(String uniqueFilename, String originalFilename, long fileSize) {
    }

    @Value("${fourstars.upload-dir}")
    private String uploadDir;

    public SavedFileInfo saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty. Please select a file to upload.");
        }

        logger.info("Attempting to save file '{}' with size {} bytes.", file.getOriginalFilename(), file.getSize());

        Path uploadPath = Paths.get(this.uploadDir);
        if (!Files.exists(uploadPath)) {
            logger.debug("Upload directory does not exist. Creating directory: {}", uploadPath);

            Files.createDirectories(uploadPath);
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        long fileSize = file.getSize();

        String fileExtension = "";
        try {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } catch (Exception e) {
            logger.warn("Uploaded file '{}' has no extension.", originalFilename);

        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        logger.debug("Generated unique filename: {}", uniqueFilename);

        Path destinationPath = uploadPath.resolve(uniqueFilename);
        logger.debug("Resolved final destination path: {}", destinationPath);

        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Successfully saved file '{}' as '{}'.", originalFilename, uniqueFilename);

        return new SavedFileInfo(uniqueFilename, originalFilename, fileSize);
    }
}
