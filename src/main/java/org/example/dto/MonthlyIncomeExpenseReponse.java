package org.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyIncomeExpenseReponse {
    @NotNull
    Integer month;

    @NotNull
    BigDecimal income=BigDecimal.ZERO;

    @NotNull
    BigDecimal expense=BigDecimal.ZERO;
}
