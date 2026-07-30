package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.ChatService;
import org.example.service.DocParserService;
import org.example.service.DocumentIngestionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
//    private final DocParserService docParserService;
    private final DocumentIngestionService documentIngestionService;
    @PostMapping
    public String chat(@RequestParam("message") String messsage){
        return chatService.chat(messsage);
    }

    @PostMapping("/pdf")
    public String pdf(@RequestParam("file") MultipartFile file,@RequestParam("fileDescription") String fileDescription){
        return documentIngestionService.ingestDocument(file,fileDescription);
    }
}
