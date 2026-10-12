package com.example.expensetracker.service;

import com.example.expensetracker.dto.*;
import com.example.expensetracker.entity.Category;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExpenseResponse create(String email, ExpenseRequest request) {
        Expense expense = new Expense();
        apply(expense, request);
        expense.setUser(getUser(email));
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> list(String email, Category category,
                                              LocalDate from, LocalDate to, Pageable pageable) {
        User user = getUser(email);
        return PageResponse.from(
                expenseRepository.search(user.getId(), category, from, to, pageable)
                        .map(ExpenseResponse::from));
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(String email, Long id) {
        return ExpenseResponse.from(findOwned(email, id));
    }

    @Transactional
    public ExpenseResponse update(String email, Long id, ExpenseRequest request) {
        Expense expense = findOwned(email, id);
        apply(expense, request); // managed entity: changes are flushed on commit
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public void delete(String email, Long id) {
        expenseRepository.delete(findOwned(email, id));
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary(String email, YearMonth month) {
        User user = getUser(email);
        YearMonth ym = month != null ? month : YearMonth.now();
        List<CategoryTotal> totals = expenseRepository.sumByCategory(user.getId(), ym.atDay(1), ym.atEndOfMonth());
        BigDecimal total = totals.stream().map(CategoryTotal::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SummaryResponse(ym.toString(), total, totals);
    }

    // ---- helpers ----

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /** Looks up by id AND owner, so other users' expenses look like they don't exist (404, not 403). */
    private Expense findOwned(String email, Long id) {
        User user = getUser(email);
        return expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense " + id + " not found"));
    }

    private void apply(Expense expense, ExpenseRequest request) {
        expense.setTitle(request.title().trim());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setExpenseDate(request.expenseDate());
        expense.setNote(request.note());
    }
}
