package org.example.client;

import lombok.RequiredArgsConstructor;
import org.example.dto.APIResponse;
import org.example.dto.TransactionResponse;
import org.example.exceptions.ToolClientException;
import org.example.util.AuthenticationUtil;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Component
public class UserDetailClient {
    private final RestClient restClient;

    public APIResponse<Integer> getCredits() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user-profile/getCredits")
                            .queryParam("userId", AuthenticationUtil.getCurrentUserId())
                            .build())
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    APIResponse<Integer>
                                    >() {}
                    );
        }catch (Exception e){
            throw new ToolClientException("Error while calling getCredits request due to"+e);
        }
    }

    public APIResponse<Integer> decrementCredits() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user-profile/decrementCredit")
                            .queryParam("userId", AuthenticationUtil.getCurrentUserId())
                            .build())
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    APIResponse<Integer>
                                    >() {}
                    );
        }catch (Exception e){
            throw new ToolClientException("Error while calling decrementCredits request due to"+e);
        }
    }
}
