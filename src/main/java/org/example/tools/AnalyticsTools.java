package org.example.tools;

import lombok.RequiredArgsConstructor;
import org.example.client.AnalyticsClient;
import org.example.dto.*;
import org.example.util.AuthenticationUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalyticsTools {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsTools.class);

    private final AnalyticsClient analyticsClient;

    @Tool(
            name = "get_monthly_savings",
            description = """
        Get the user's monthly savings for a specific year. This tool will give you total savings of the user month wise. 
        

        Required:
        - year: the year for which monthly savings are required.
        """
    )
    public List<MonthlySpendingResponse> getMonthlySavings( @ToolParam(
            description = "Four-digit calendar year, such as 2026."
    ) Integer year) {

        log.info("Calling get_monthly_savings");

        Integer userId = getAuthenticatedUserId();

        APIResponse<List<MonthlySpendingResponse>> response =
                analyticsClient.getMonthlySavings(userId, year);

        return response.getData();
    }

    @Tool(
            name = "get_month_stats",
            description = """
        Get users LATEST/LIVE financial statistics for a specific month and year. This functions gives 
        totalIncome-: Total income of the user for particular month 
        totalExpense-: Total expenditure of the user for particular month
        totalNeedsExpense-:Total expenditure of the user on NEEDS for particular month
        totalWantsExpense-:Total expenditure of the user on WANTS for particular month
        totalSavings-:Total savings of the user for particular month
        
        Required:
        - month: month number from 1 to 12
        - year: year for which the statistics are required
        """
    )
    public MonthStatsResponse getMonthStats(
            @ToolParam(
                    description = "Month number from 1 to 12."
            )
                    Integer month,
            @ToolParam(
                    description = "Four-digit calendar year, such as 2026."
            )
                    Integer year) {


        log.info("Calling get_month_stats");
        Integer userId = getAuthenticatedUserId();

        APIResponse<MonthStatsResponse> response =
                analyticsClient.getMonthStats(userId, month, year);

        return response.getData();
    }

    @Tool(
            name = "get_monthly_needs_wants",
            description = """
        Get the user's LATEST/LIVE monthly spending breakdown between
        needs and wants for a specific year. Hence month wise you will get Needs and Wants expense of the user.

        Required:
        - year: year for which the data is required.
        """
    )
    public List<MonthlyNeedsWantsResponse> getMonthlyNeedsWants( @ToolParam(
            description = "Four-digit calendar year, such as 2026."
    ) Integer year) {

        log.info("Calling getMonthlyNeedsWants");

        Integer userId = getAuthenticatedUserId();

        APIResponse<List<MonthlyNeedsWantsResponse>> response =
                analyticsClient.getMonthlyNeedsWants(userId, year);

        return response.getData();
    }

    @Tool(
            name = "get_monthly_category_wise",
            description = """
        Get the user's LATEST/LIVE category-wise spending for a specific month and year.

        Required:
        - year: year for which the data is required
        - month: month number from 1 to 12
        """
    )
    public List<MonthlyCategoryResponse> getCategoryWise(
            @ToolParam(
                    description = "Four-digit calendar year, such as 2026."
            )
                    Integer year,
            @ToolParam(
                    description = "Month number from 1 to 12."
            )
                    Integer month) {

        log.info("Calling get_monthly_category_wise");

        Integer userId = getAuthenticatedUserId();

        APIResponse<List<MonthlyCategoryResponse>> response =
                analyticsClient.getCategoryWise(userId, year, month);

        return response.getData();
    }

    @Tool(
            name = "get_monthly_income_vs_expense",
            description = """
        Get the user's LATEST/LIVE monthly income versus expense for a specific year.

        Required:
        - year: year for which the data is required.
        """
    )
    public List<MonthlyIncomeExpenseReponse> getMonthlyIncomeVsExpense(
            @ToolParam(
                    description = "Four-digit calendar year, such as 2026."
            )
                    Integer year) {

        Integer userId = getAuthenticatedUserId();

        log.info("Calling get_monthly_income_vs_expense");

        APIResponse<List<MonthlyIncomeExpenseReponse>> response =
                analyticsClient.getMonthlyIncomeVsExpense(userId, year);

        return response.getData();
    }

    private Integer getAuthenticatedUserId() {
        return AuthenticationUtil.getCurrentUserId();
    }
}