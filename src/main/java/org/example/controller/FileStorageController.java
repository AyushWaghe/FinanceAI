package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.APIResponse;
import org.example.dto.DocUploadResponse;
import org.example.service.DocumentRetrievalPipeline;
import org.example.service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/file")
public class FileStorageController {
    private final FileService fileService;
    private final DocumentRetrievalPipeline documentRetrievalPipeline;

    @PostMapping()
    public void upload(
            @RequestParam MultipartFile file) {
        fileService.upload(file);

    }

    @GetMapping()
    public InputStreamResource download(
            @RequestParam String objectKey) {

        InputStream inputStream = fileService.download(objectKey);

        return new InputStreamResource(inputStream);
    }

    @GetMapping("/getFiles")
    public List<DocUploadResponse> getUserFilesData() {
        return fileService.getUserFiles();
    }

    @DeleteMapping()
    public void deleteFile(
            @RequestParam("objectKey") String objectKey) {
        fileService.delete(objectKey);
    }
}
