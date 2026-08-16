package com.libmanagementsys.vestas_proj.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanHistoryEntryDto(
                String loanStatus,
                String username,
                String bookTitle,
                String isbn,
                LocalDate requestDate,
                LocalDate dueDate,
                LocalDate returnDate,
                BigDecimal fine) {
}