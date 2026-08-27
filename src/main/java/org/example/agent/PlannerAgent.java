package org.example.agent;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.client.UserDetailClient;
import org.example.dto.PlannerAgentResponse;
import org.example.prompts.PromptLoaderImpl;
import org.example.tools.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PlannerAgent {
    private final ChatClient chatClient;

    private final TransactionsTools transactionsTools;
    private final BillTools billTools;
    private final AnalyticsTools analyticsTools;
    private final RAGAgentTool ragAgentTool;
    private final HelperTools helperTools;
    private final PromptLoaderImpl promptLoader;
    private final MeterRegistry meterRegistry;
    private final UserDetailClient userDetailClient;


    public PlannerAgentResponse answerUserQuery(String query,boolean thinkAndAnswer) {
        PlannerAgentResponse plannerAgentResponse;
        if (thinkAndAnswer){
            plannerAgentResponse= chatClient.prompt()
                    .user(query)
                    .options(OpenAiChatOptions.builder().model("gpt-5-mini")
                            .temperature(1.0).parallelToolCalls(true).build())
                    .system(promptLoader.load("PlannerAgentPrompt"))
                    .tools(
                            transactionsTools,
                            billTools,
                            analyticsTools,
                            ragAgentTool,
                            helperTools
                    )
                    .call()
                    .entity(PlannerAgentResponse.class);
        }else{
            plannerAgentResponse= chatClient.prompt()
                    .user(query)
                    .options(OpenAiChatOptions.builder().model("gpt-4.1-mini").temperature(0.2)
                            .parallelToolCalls(true).build())
                    .system(promptLoader.load("PlannerAgentPrompt"))
                    .tools(
                            transactionsTools,
                            billTools,
                            analyticsTools,
                            ragAgentTool,
                            helperTools
                    )
                    .call()
                    .entity(PlannerAgentResponse.class);
        }


        return plannerAgentResponse;

    }

//    public PlannerAgentResponse answerUserQuery(String query, Integer userId) {
//
//        var response = chatClient.prompt()
//                .user(query)
//                .system(promptLoader.load("PlannerAgentPrompt"))
//                .tools(
//                        transactionsTools,
//                        billTools,
//                        analyticsTools,
//                        ragAgentTool,
//                        helperTools
//                )
//                .call()
//                .responseEntity(PlannerAgentResponse.class);
//
//        System.out.println("MODEL USED: " +
//                response.response().getMetadata().getUsage().getNativeUsage());
//
//        PlannerAgentResponse plannerAgentResponse =
//                response.entity();
//
//        return plannerAgentResponse;
//    }

}
