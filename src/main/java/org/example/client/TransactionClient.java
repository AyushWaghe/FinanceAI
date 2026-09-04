package org.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.dto.APIResponse;
import org.example.dto.TransactionRequest;
import org.example.dto.TransactionResponse;
import org.example.dto.UserCategoriesResponse;
import org.example.exceptions.ToolClientException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Component
public class TransactionClient {
    private final RestClient restClient;
    private final int page=0;
    private final int pageSize=100000;
    private final ObjectMapper objectMapper;

    public APIResponse<List<TransactionResponse>> getTransactions(
            LocalDate startDate,
            LocalDate endDate) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/transactions")
                            .queryParamIfPresent(
                                    "startDate",
                                    java.util.Optional.ofNullable(startDate)
                            )
                            .queryParamIfPresent(
                                    "endDate",
                                    java.util.Optional.ofNullable(endDate)
                            )
                            .build())
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    APIResponse<List<TransactionResponse>>
                                    >() {}
                    );
        }catch (Exception e){
            throw new ToolClientException("Error while calling getTransactions request due to"+e);
        }
    }

    public APIResponse<List<UserCategoriesResponse>> getUserCategories() {
       return restClient.get().uri(uriBuilder -> uriBuilder.path("/transactions/categories").build())
               .retrieve()
               .body(new ParameterizedTypeReference<APIResponse<List<UserCategoriesResponse>>>() {
               });
    }


    public List<TransactionResponse> getMonthlyTransactions(
            Integer month,
            Integer year) {
        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/transactions/monthly")
                            .queryParam("month", month)
                            .queryParam("year", year)
                            .queryParam("page", page)
                            .queryParam("pageSize", pageSize)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            System.out.println("Response is"+response);



            return objectMapper.convertValue(
                    response.get("data").get("content"),
                    new TypeReference<List<TransactionResponse>>() {}
            );
        } catch (Exception e) {
            System.out.println(e);
            throw new ToolClientException("Error while calling get monthly transactions request due to"+e);
        }
    }

}
