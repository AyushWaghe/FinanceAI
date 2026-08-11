package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.DocumentRetrievalService;
import org.example.service.FileStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RequiredArgsConstructor
@RestController
@RequestMapping("/file")
public class FileStorageController {
    private final FileStorageService fileStorageService;
    private final DocumentRetrievalService documentRetrievalService;

    @PostMapping("{userId}")
    public ResponseEntity<String> upload(
            @RequestParam MultipartFile file,
            @PathVariable("userId") Integer userId) {

        String key = fileStorageService.upload(file, userId);
        return ResponseEntity.ok(key);
    }

    @GetMapping
    public ResponseEntity<InputStreamResource> download(
            @RequestParam String objectKey) {

        InputStream inputStream=fileStorageService.download(objectKey);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(
            @RequestParam String objectKey) {

        fileStorageService.delete(objectKey);
        return ResponseEntity.ok(objectKey);

    }

    @GetMapping("/lucene")
    public String getLuceneTest(@RequestParam("userQuery") String userQuery,@RequestParam("userId") Integer userId) {
        documentRetrievalService.retrieveDocuments(userQuery,userId);
        return "Done";
    }
}
