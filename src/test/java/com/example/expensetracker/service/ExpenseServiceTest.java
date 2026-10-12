package com.example.expensetracker.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock ExpenseRepository expenseRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ExpenseService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("a@b.com");
    }

    @Test
    void create_savesExpenseForCurrentUser() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseRequest request = new ExpenseRequest("Lunch", new BigDecimal("12.50"),
                Category.FOOD, LocalDate.now(), null);
        ExpenseResponse response = service.create("a@b.com", request);

        assertEquals("Lunch", response.title());
        assertEquals(Category.FOOD, response.category());

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        assertSame(user, captor.getValue().getUser());
    }

    @Test
    void get_expenseOfAnotherUser_throwsNotFound() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.get("a@b.com", 99L));
    }

    @Test
    void summary_addsUpCategoryTotals() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(expenseRepository.sumByCategory(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        new CategoryTotal(Category.FOOD, new BigDecimal("100.00")),
                        new CategoryTotal(Category.RENT, new BigDecimal("50.00"))));

        SummaryResponse summary = service.summary("a@b.com", YearMonth.of(2026, 10));

        assertEquals("2026-10", summary.month());
        assertEquals(0, new BigDecimal("150.00").compareTo(summary.total()));
        assertEquals(2, summary.byCategory().size());
    }
}
