package org.example.dto;

import lombok.Data;
import org.example.enums.DocumentState;

@Data
public class DocUploadResponse {
    private String objectKey;
    private String fileName;
    private DocumentState documentState;
    private String docType;
    private String docSummary;
    private String rejectedReason;
}
