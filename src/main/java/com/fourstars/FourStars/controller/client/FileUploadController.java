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

        FileService.SavedFileInfo savedFileInfo = fileService.saveFile(file);

        String fileUrl = "/uploads/" + savedFileInfo.uniqueFilename();

        FileUploadResponseDTO response = new FileUploadResponseDTO(
                savedFileInfo.uniqueFilename(),
                fileUrl,
                savedFileInfo.originalFilename(),
                savedFileInfo.fileSize());
        return ResponseEntity.ok(response);
    }
}
