package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public String chat(@RequestParam("message") String messsage){
        return chatService.chat(messsage);
    }
}
