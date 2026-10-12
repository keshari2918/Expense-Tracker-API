package com.example.expensetracker.dto;

import com.example.expensetracker.entity.Category;
import java.math.BigDecimal;

public record CategoryTotal(Category category, BigDecimal total) {
}
