package com.example.expensetracker.controller;

import com.example.expensetracker.dto.*;
import com.example.expensetracker.entity.Category;
import com.example.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@AuthenticationPrincipal UserDetails user,
                                  @Valid @RequestBody ExpenseRequest request) {
        return expenseService.create(user.getUsername(), request);
    }

    @GetMapping
    public PageResponse<ExpenseResponse> list(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "expenseDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return expenseService.list(user.getUsername(), category, from, to, pageable);
    }

    /** Totals per category for a month, e.g. /api/expenses/summary?month=2026-10 (defaults to current month). */
    @GetMapping("/summary")
    public SummaryResponse summary(@AuthenticationPrincipal UserDetails user,
                                   @RequestParam(required = false)
                                   @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return expenseService.summary(user.getUsername(), month);
    }

    @GetMapping("/{id}")
    public ExpenseResponse get(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        return expenseService.get(user.getUsername(), id);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@AuthenticationPrincipal UserDetails user, @PathVariable Long id,
                                  @Valid @RequestBody ExpenseRequest request) {
        return expenseService.update(user.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        expenseService.delete(user.getUsername(), id);
    }
}
