package org.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthStatsResponse {

    @NotNull
    BigDecimal totalIncome;
    @NotNull
    BigDecimal totalExpense;
    @NotNull
    BigDecimal totalNeedsExpense;
    @NotNull
    BigDecimal totalWantsExpense;
    @NotNull
    BigDecimal totalSavings;
}
