package org.example.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.enums.SpendingType;
import org.example.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "User id cannot be null")
    private Integer userId;

    private String title;

    private String description;

    @NotNull(message = "Transaction amount cannot be null")
    @DecimalMin(value = "0.0",inclusive = false,message = "Transaction amount must be greater than 0")
    @DecimalMax(value = "10000000.0",inclusive = false,message = "Transaction amount must be less that 1cr")
    private BigDecimal amount;

//    @NotNull(message = "Transaction category cannot be blank")
    private String category;

    @NotNull(message = "Transaction date cannot be blank")
    @PastOrPresent(message = "Transaction date cannot be a future date")
    private LocalDate transactionDate;

    @NotNull(message = "Transaction type cannot be null")
    private TransactionType type; //income,expense

//    @NotNull(message = "Transaction spending type cannot be null")
    private SpendingType spendingType; //wants,needs,savings

}
