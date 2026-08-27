package org.example.tools;

import lombok.RequiredArgsConstructor;
import org.example.client.TransactionClient;
import org.example.dto.APIResponse;
import org.example.dto.TransactionResponse;
import org.example.dto.UserCategoriesResponse;
import org.example.util.AuthenticationUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionsTools {
    private final TransactionClient transactionClient;


    @Tool(
            name = "get_transactions",
            description = """
            Fetch the user's LATEST/LIVE financial transactions.

            Use this tool when the user asks about their
            transactions, expenses, income, or spending
            for a specific date range or ONLY for particular year.

            startDate and endDate are optional.
            If the user does not specify a date range,
            leave them empty.
            """
    )
    public List<TransactionResponse> getTransactions(
            @ToolParam(
                    description =  """
                Start date of the transaction date range.
                Inclusive.
                MUST be in yyyy-MM-dd format.
                Example: 2026-08-01.
                """
            )
                    LocalDate startDate,

            @ToolParam(
                    description =  """
                End date of the transaction date range.
                Inclusive.
                MUST be in yyyy-MM-dd format.
                Example: 2026-08-18.
                """
            )
                    LocalDate endDate) {

        System.out.println("Calling getTransactions Tool");

        Integer userId = getAuthenticatedUserId();

        APIResponse<List<TransactionResponse>> response =
                transactionClient.getTransactions(
                        userId,
                        startDate,
                        endDate
                );
        return response.getData();
    }

    @Tool(
            name = "get_user_categories",
            description = """
            Fetch the user's LATEST/LIVE created transaction categories.
            
            Use this tool to know what all transactions CATEGORIES have already been created by the user. For example categories like 
            food,travel,leisure etc.
            """
    )
    public List<UserCategoriesResponse> getUserCategories() {

        System.out.println("Calling get user categories tool");

        Integer userId = getAuthenticatedUserId();

        APIResponse<List<UserCategoriesResponse>> response =
                transactionClient.getUserCategories(
                        userId);
        return response.getData();
    }

    @Tool(
            name = "get_monthly_transactions",
            description = """
        Fetch the user's LATEST/LIVE transactions for a specific month and year. This tool will give all the transactions of the user for specified month and year
        Each transaction will contain information (Title,description,amount,category,date,INCOME/EXPENSE,spending type)

        month must be between 1 and 12.
        year must be a valid year.
        """
    )
    public List<TransactionResponse> getMonthlyTransactions(
            @ToolParam(
                    description = "Month number from 1 to 12."
            )
                    Integer month,

            @ToolParam(
                    description = "Four-digit calendar year, such as 2026."
            )
                    Integer year) {

        System.out.println("Calling get monthly transaction tool");

        Integer userId = getAuthenticatedUserId();

               List<TransactionResponse> response=transactionClient.getMonthlyTransactions(
                        userId,
                        month,
                        year
                );

               return response;
    }

    private Integer getAuthenticatedUserId() {
        return AuthenticationUtil.getCurrentUserId();
    }



}
