package com.fourstars.FourStars.controller.client;

import com.fourstars.FourStars.domain.response.file.FileUploadResponseDTO;
import com.fourstars.FourStars.service.FileService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
public class FileUploadController {

    private final FileService fileService;

    public FileUploadController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    @ApiMessage("Upload a single file to server")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file)
            throws IOException {

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty.");
        }

        String savedFilename = fileService.saveFile(file);

        // Tạo URL để client có thể truy cập file
        // Ví dụ: http://localhost:8080/uploads/ten-file-da-luu.jpg
        String fileUrl = "/uploads/" + savedFilename;

        FileUploadResponseDTO response = new FileUploadResponseDTO(savedFilename, fileUrl);
        return ResponseEntity.ok(response);
    }
}
