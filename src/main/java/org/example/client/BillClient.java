package org.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.dto.APIResponse;
import org.example.dto.BillInstanceResponse;
import org.example.dto.BillResponse;
import org.example.dto.TransactionResponse;
import org.example.enums.BillStatus;
import org.example.exceptions.ToolClientException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BillClient {
    private final RestClient restClient;
    private final int page=0;
    private final int pageSize=100000;
    private final ObjectMapper objectMapper;

    public List<BillResponse> getBills() {

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bill/user")
                            .queryParam("page", page)
                            .queryParam("pageSize", pageSize)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            return objectMapper.convertValue(
                    response.get("data").get("content"),
                    new TypeReference<List<BillResponse>>() {}
            );
        }catch (Exception e){
            throw new ToolClientException("Error while calling getBills request due to"+e);
        }
    }

    public List<BillInstanceResponse> getUpcomingBills(
            ) {

       try {
           JsonNode response =  restClient.get()
                   .uri(uriBuilder -> uriBuilder
                           .path("/bill-instance/upcoming")
                           .queryParam("page", page)
                           .queryParam("pageSize", pageSize)
                           .build())
                   .retrieve()
                   .body(JsonNode.class);

           return objectMapper.convertValue(
                   response.get("data").get("content"),
                   new TypeReference<List<BillInstanceResponse>>() {}
           );
       }catch (Exception e){
           throw new ToolClientException("Error while calling getUpcomingBills request due to"+e);
       }
    }

    public List<BillInstanceResponse> getBillsByStatus(
            BillStatus status) {

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bill-instance/status/user")
                            .queryParam("status", status)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            return objectMapper.convertValue(
                    response.get("data").get("content"),
                    new TypeReference<List<BillInstanceResponse>>() {}
            );
        }catch (Exception e){
            throw new ToolClientException("Error while calling getBillByStatus request due to"+e);
        }
    }

    public List<BillInstanceResponse> getOverdueBills(
            ) {

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bill-instance/overdue")
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            return objectMapper.convertValue(
                    response.get("data").get("content"),
                    new TypeReference<List<BillInstanceResponse>>() {}
            );
        }catch (Exception e){
            throw new ToolClientException("Error while calling getOverdueBills request due to"+e);
        }
    }
}
