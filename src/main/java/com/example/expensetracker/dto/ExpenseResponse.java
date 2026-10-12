package com.example.expensetracker.dto;

import com.example.expensetracker.entity.Category;
import com.example.expensetracker.entity.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id, String title, BigDecimal amount,
        Category category, LocalDate expenseDate, String note) {

    public static ExpenseResponse from(Expense e) {
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(),
                e.getCategory(), e.getExpenseDate(), e.getNote());
    }
}
