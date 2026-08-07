//package org.example.controller;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import lombok.RequiredArgsConstructor;
//import org.example.dto.LLMDocIngestionResponse;
//import org.example.service.ChatService;
//import org.example.service.DocParserService;
//import org.example.service.DocumentIngestionService;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/chat")
//@RequiredArgsConstructor
//public class ChatController {
//
//    private final ChatService chatService;
////    private final DocParserService docParserService;
//    private final DocumentIngestionService documentIngestionService;
//    @PostMapping
//    public String chat(@RequestParam("message") String messsage){
//        return chatService.chat(messsage);
//    }
//
//    @PostMapping("/pdf")
//    public String pdf(@RequestParam("file") MultipartFile file, @RequestParam("fileDescription") String fileDescription) throws JsonProcessingException {
//        return documentIngestionService.ingestDocument(file,fileDescription);
//    }
//}
