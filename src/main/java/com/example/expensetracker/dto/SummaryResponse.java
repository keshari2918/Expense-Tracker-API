package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record SummaryResponse(String month, BigDecimal total, List<CategoryTotal> byCategory) {
}
