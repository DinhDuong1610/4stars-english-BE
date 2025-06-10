package com.fourstars.FourStars.service;

import com.fourstars.FourStars.util.error.BadRequestException;
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

    public record SavedFileInfo(String uniqueFilename, String originalFilename, long fileSize) {
    }

    @Value("${fourstars.upload-dir}")
    private String uploadDir;

    public SavedFileInfo saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty. Please select a file to upload.");
        }

        // Tạo thư mục upload nếu nó chưa tồn tại
        Path uploadPath = Paths.get(this.uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Lấy thông tin từ file gốc
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        long fileSize = file.getSize();

        // Tạo tên file duy nhất để tránh trùng lặp
        String fileExtension = "";
        try {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } catch (Exception e) {
            // Không có phần mở rộng file
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Tạo đường dẫn đầy đủ đến file
        Path destinationPath = uploadPath.resolve(uniqueFilename);

        // Copy file vào thư mục đích
        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về một record chứa tất cả thông tin cần thiết
        return new SavedFileInfo(uniqueFilename, originalFilename, fileSize);
    }
}
