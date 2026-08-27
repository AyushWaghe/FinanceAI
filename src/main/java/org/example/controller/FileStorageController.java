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
    public ResponseEntity<APIResponse<Void>> upload(
            @RequestParam MultipartFile file) {

        fileService.upload(file);
        APIResponse apiResponse=new APIResponse<>();
        apiResponse.setMessage("File uploaded successfully");
        apiResponse.setSuccess(true);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<InputStreamResource> download(
            @RequestParam String objectKey) {

        InputStream inputStream= fileService.download(objectKey);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @GetMapping("/getFiles")
    public ResponseEntity<APIResponse<List<DocUploadResponse>>> getUserFilesData() {
        List<DocUploadResponse> docUploadResponses= fileService.getUserFiles();
        APIResponse<List<DocUploadResponse>> apiResponse=new APIResponse<>();
        apiResponse.setData(docUploadResponses);
        apiResponse.setSuccess(true);
        apiResponse.setMessage("User files fetched successfully");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity<APIResponse<Void>> deleteFile(
            @RequestParam("objectKey") String objectKey) {

        fileService.delete(objectKey);
        APIResponse<Void> apiResponse=new APIResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("User document deleted successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
}
