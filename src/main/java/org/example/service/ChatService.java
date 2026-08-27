package org.example.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.agent.PlannerAgent;
import org.example.client.UserDetailClient;
import org.example.dao.ConversationRepository;
import org.example.dao.MessageRepository;
import org.example.dto.ChatResponses;
import org.example.dto.PlannerAgentResponse;
import org.example.enums.Role;
import org.example.model.Conversation;
import org.example.model.Message;
import org.example.util.AuthenticationUtil;
import org.example.util.TokenUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PlannerAgent plannerAgent;
    private Timer plannerAgentResponseTime;
    private final MeterRegistry meterRegistry;
    private final UserDetailClient userDetailClient;

    @PostConstruct()
    public void init(){
        plannerAgentResponseTime=meterRegistry.timer("planner.agent.response.time");
    }

    public List<ChatResponses> sendMessages(String query,boolean thinkAndAnswer) {
        Integer userId= AuthenticationUtil.getCurrentUserId();
        Conversation conversation = conversationRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    Conversation newConversation = new Conversation();
                    newConversation.setUserId(userId.longValue());
                    return conversationRepository.save(newConversation);
                });

        String summary = conversation.getLatestSummary();

        LocalDateTime summaryUpdatedAt = conversation.getSummaryUpdatedAt();

        List<Message> messages =
                messageRepository
                        .findByConversationConversationIdAndCreatedAtGreaterThanOrderByCreatedAtAsc(
                                conversation.getConversationId(),
                                summaryUpdatedAt
                        );

        String messagesText = messages.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        boolean generateSummary =
                TokenUtil.getTokenCount(summary + messagesText + query) > 200;

        String userPrompt = """
            Below are the previous messages till now. DON'T MAKE THEM AS SOURCE OF TRUTH THESE ARE JUST FOR REFERENCE. ALWAYS FETCH THE LATEST DATA FROM THE TOOLS FIRST. EVEN IF THE DATA ABOUT USER QUERY IS PRESENT IN THE OLDER CHATS DON'T RELY ON THEM IF YOU SEE THAT THE DATA CAN BE FETCHED FROM TOOLS THEN ALWAYS CALL THEM AND IF NOT THEN ONLY RELY ON THE OLDER CHAT INFORMATION.
            ALWAYS TELL USER IF IN CASE YOU ARE REFERRING TO THE OLDER CHATS FOR ANSWERING USER QUERY IN SUCH CASES:
            %s

            Below is the summary of the chat till now.DON'T MAKE THIS SUMMARY AS SOURCE OF TRUTH THIS IS JUST FOR REFERENCE.ALWAYS FETCH THE LATEST DATA FROM THE TOOLS FIRST.EVEN IF THE DATA ABOUT USER QUERY IS PRESENT IN THE SUMMARY DON'T RELY ON IT IF YOU SEE THAT THE DATA CAN BE FETCHED FROM TOOLS THEN ALWAYS CALL TOOLS AND IF NOT THEN ONLY RELY ON THE SUMMARY TO ANSWER THE QUERY.
            %s

            Below is the user query:
            %s

            Generate summary?
            %s
            """.formatted(
                messagesText,
                summary,
                query,
                generateSummary
        );
        Timer.Sample plannerAgentStopwatch=Timer.start(meterRegistry);
        PlannerAgentResponse plannerAgentResponse =
                plannerAgent.answerUserQuery(userPrompt,thinkAndAnswer);
        plannerAgentStopwatch.stop(plannerAgentResponseTime);

        if (plannerAgentResponse.isSummaryGenerated()) {
            conversation.setLatestSummary(
                    plannerAgentResponse.getSummary()
            );
            conversation.setSummaryUpdatedAt(
                    LocalDateTime.now()
            );
        }

        Message userMessage = new Message();
        userMessage.setRole(Role.USER);
        userMessage.setContent(query);
        userMessage.setConversation(conversation);

        Message assistantMessage = new Message();
        assistantMessage.setRole(Role.ASSISTANT);
        assistantMessage.setContent(
                plannerAgentResponse.getLlmResponse()
        );
        assistantMessage.setConversation(conversation);

        conversation.getMessages().add(userMessage);
        conversation.getMessages().add(assistantMessage);

        conversationRepository.save(conversation);

        List<Message> messageListAll =
                messageRepository
                        .findByConversationConversationIdOrderByCreatedAtAsc(
                                conversation.getConversationId()
                        );

  return messageListAll.stream()
                .map(message -> new ChatResponses(
                        message.getMessageId(),
                        message.getRole(),
                        message.getContent()
                ))
                .toList();

    }

    public List<ChatResponses> getMessages() {

        Integer userId=AuthenticationUtil.getCurrentUserId();
        Optional<Conversation> conversation = conversationRepository
                .findByUserId(userId);

        if(!conversation.isEmpty()){
            List<Message> messageListAll =
                    messageRepository
                            .findByConversationConversationIdOrderByCreatedAtAsc(
                                    conversation.get().getConversationId()
                            );

            return messageListAll.stream()
                    .map(message -> new ChatResponses(
                            message.getMessageId(),
                            message.getRole(),
                            message.getContent()
                    ))
                    .toList();
        }

        return new ArrayList<>();
    }

    @Transactional
    public void deleteConversation() {

        conversationRepository
                .findByUserId(AuthenticationUtil.getCurrentUserId())
                .ifPresent(conversationRepository::delete);
    }
}
