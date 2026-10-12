package com.example.expensetracker.repository;

import com.example.expensetracker.dto.CategoryTotal;
import com.example.expensetracker.entity.Category;
import com.example.expensetracker.entity.Expense;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Always scope by user id so one user can never read another user's data.
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT e FROM Expense e
            WHERE e.user.id = :userId
              AND (:category IS NULL OR e.category = :category)
              AND (:from IS NULL OR e.expenseDate >= :from)
              AND (:to IS NULL OR e.expenseDate <= :to)
            """)
    Page<Expense> search(@Param("userId") Long userId,
                         @Param("category") Category category,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to,
                         Pageable pageable);

    @Query("""
            SELECT new com.example.expensetracker.dto.CategoryTotal(e.category, SUM(e.amount))
            FROM Expense e
            WHERE e.user.id = :userId AND e.expenseDate BETWEEN :start AND :end
            GROUP BY e.category
            ORDER BY SUM(e.amount) DESC
            """)
    List<CategoryTotal> sumByCategory(@Param("userId") Long userId,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);
}
