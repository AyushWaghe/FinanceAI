package org.example.agent;

import lombok.RequiredArgsConstructor;
import org.example.service.DocumentIngestionService;
import org.example.service.DocumentRetrievalService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class KnowledgeAgent {

    private final DocumentIngestionService documentIngestionService;
    private final DocumentRetrievalService documentRetrievalService;

    public void ingestDoc(MultipartFile multipartFile,String docDesc){
        documentIngestionService.ingestDocument(multipartFile,docDesc);
    }

    public void retrieveDoc(String query){
        documentRetrievalService.retrieveDocuments(query);
    }
}
