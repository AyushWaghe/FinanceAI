package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LuceneDocumentData {
    String chunkText;
    String userId;
    String chunkId;
    String docId;
}
