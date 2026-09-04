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
            Integer year) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/analytics/monthly-savings/user")
                            .queryParam("year", year)
                            .build())
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
            Integer month,
            Integer year) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/analytics/month-stats/user")
                            .queryParam("month", month)
                            .queryParam("year", year)
                            .build())
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
            Integer year) {

       try {
           return restClient.get()
                   .uri(uriBuilder -> uriBuilder
                           .path("/analytics/monthly-needs-wants/user")
                           .queryParam("year", year)
                           .build())
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
            Integer year,
            Integer month) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/analytics/monthly-category/user")
                            .queryParam("year", year)
                            .queryParam("month", month)
                            .build())
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
            Integer year) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/analytics/income-vs-expense/user")
                            .queryParam("year", year)
                            .build())
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
