package com.fourstars.FourStars.controller.client;

import com.fourstars.FourStars.domain.response.file.FileUploadResponseDTO;
import com.fourstars.FourStars.service.FileService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Utility - File Upload API", description = "APIs for uploading files")
public class FileUploadController {

    private final FileService fileService;

    public FileUploadController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "Upload a single file", description = "Uploads a file to the server. The request must be of type 'multipart/form-data'. Returns the URL and details of the saved file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "No file provided or the file is empty"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "500", description = "Could not save the file due to a server-side error")
    })
    @PostMapping("/upload")
    @ApiMessage("Upload a single file to server")
    @PreAuthorize("hasPermission(null, null)")
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
