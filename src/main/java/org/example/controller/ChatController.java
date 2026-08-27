package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.agent.PlannerAgent;
import org.example.client.UserDetailClient;
import org.example.dto.APIResponse;
import org.example.dto.ChatResponses;
import org.example.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final PlannerAgent plannerAgent;
    private final ChatService chatService;
    private final UserDetailClient userDetailClient;

    @GetMapping()
    public List<ChatResponses> askPlannerAgent(@RequestParam("query") String query,@RequestParam("thinkAndAnswer") boolean thinkAndAnswer){
        return chatService.sendMessages(query,thinkAndAnswer);
    }


    @GetMapping("/getMessages")
    public List<ChatResponses> getMessages(){
        return chatService.getMessages();
    }


    @DeleteMapping()
    public void deleteMessages(){
        chatService.deleteConversation();
    }
}
