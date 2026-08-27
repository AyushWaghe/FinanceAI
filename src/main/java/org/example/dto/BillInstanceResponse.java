package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.BillStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillInstanceResponse {

    @NotBlank
    private Integer id;

    @NotBlank
    private String title;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    private BillStatus billStatus;
}
