package com.libmanagementsys.vestas_proj.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanHistoryEntry(
                String loanStatus,
                String username,
                String bookTitle,
                String isbn,
                LocalDate requestDate,
                LocalDate dueDate,
                LocalDate returnDate,
                BigDecimal fine) {
}