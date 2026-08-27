package org.example.client;

import lombok.RequiredArgsConstructor;
import org.example.dto.*;
import org.example.exceptions.ToolClientException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalyticsClient {
    private final RestClient restClient;

    public APIResponse<List<MonthlySpendingResponse>> getMonthlySavings(
            Integer userId,
            Integer year) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/analytics/monthly-savings/user/{userId}")
                            .queryParam("year", year)
                            .build(userId))
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    APIResponse<List<MonthlySpendingResponse>>
                                    >() {}
                    );
        }catch (Exception e){
            throw new ToolClientException("Error while calling getMonthlySavings request due to"+e);
        }
    }

    public APIResponse<MonthStatsResponse> getMonthStats(
            Integer userId,
            Integer month,
            Integer year) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/analytics/month-stats/user/{userId}")
                            .queryParam("month", month)
                            .queryParam("year", year)
                            .build(userId))
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    APIResponse<MonthStatsResponse>
                                    >() {}
                    );
        }catch (Exception e){
            throw new ToolClientException("Error while calling getMonthStats request due to"+e);
        }
    }

    public APIResponse<List<MonthlyNeedsWantsResponse>> getMonthlyNeedsWants(
            Integer userId,
            Integer year) {

       try {
           return restClient.get()
                   .uri(uriBuilder -> uriBuilder
                           .path("/analytics/monthly-needs-wants/user/{userId}")
                           .queryParam("year", year)
                           .build(userId))
                   .retrieve()
                   .body(
                           new ParameterizedTypeReference<
                                   APIResponse<List<MonthlyNeedsWantsResponse>>
                                   >() {}
                   );
       }catch (Exception e){
           throw new ToolClientException("Error while calling getMonthlyNeedsWants request due to"+e);
       }
    }

    public APIResponse<List<MonthlyCategoryResponse>> getCategoryWise(
            Integer userId,
            Integer year,
            Integer month) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/analytics/monthly-category/user/{userId}")
                            .queryParam("year", year)
                            .queryParam("month", month)
                            .build(userId))
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    APIResponse<List<MonthlyCategoryResponse>>
                                    >() {}
                    );
        }catch (Exception e){
            throw new ToolClientException("Error while calling getCategoryWise request due to"+e);
        }
    }

    public APIResponse<List<MonthlyIncomeExpenseReponse>> getMonthlyIncomeVsExpense(
            Integer userId,
            Integer year) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/analytics/income-vs-expense/user/{userId}")
                            .queryParam("year", year)
                            .build(userId))
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    APIResponse<List<MonthlyIncomeExpenseReponse>>
                                    >() {}
                    );
        }catch (Exception e){
            throw new ToolClientException("Error while calling getMonthlyIncomeVsExpense request due to"+e);
        }
    }
}
