package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.SpendingType;
import org.example.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Integer transactionId;

    private String title;

    private String description;

    private BigDecimal amount;

    private String category;

    private LocalDate transactionDate;

    private TransactionType type;

    private SpendingType spendingType;
}
