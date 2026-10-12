package com.example.expensetracker.dto;

import com.example.expensetracker.entity.Category;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotBlank @Size(max = 100) String title,
        @NotNull @DecimalMin(value = "0.01", message = "must be at least 0.01")
        @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull Category category,
        @NotNull @PastOrPresent LocalDate expenseDate,
        @Size(max = 255) String note) {
}
