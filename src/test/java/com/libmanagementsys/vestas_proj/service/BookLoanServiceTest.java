package com.libmanagementsys.vestas_proj.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.libmanagementsys.vestas_proj.config.AppProperties;
import com.libmanagementsys.vestas_proj.model.Book;
import com.libmanagementsys.vestas_proj.model.Transaction;
import com.libmanagementsys.vestas_proj.model.User;
import com.libmanagementsys.vestas_proj.repository.BookLoanRepository;
import com.libmanagementsys.vestas_proj.repository.BookRepository;
import com.libmanagementsys.vestas_proj.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class BookLoanServiceTest {

    @Mock
    BookRepository bookRepo;

    @Mock
    TransactionRepository transactionRepo;

    @Mock
    BookLoanRepository bookLoanRepo;

    @Mock
    AppProperties appProperties;

    @Mock
    org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Mock
    BookService bookService;

    @Mock
    UserService userService;

    @InjectMocks
    BookLoanService bookLoanService;

    @Test
    void validateBookLoanRequest_nullOrEmpty_throws() {
        assertThrows(IllegalArgumentException.class, () -> bookLoanService.validateBookLoanRequest(null));
        assertThrows(IllegalArgumentException.class, () -> bookLoanService.validateBookLoanRequest(java.util.Set.of()));
    }

    @Test
    void validateGetBook_notFound_throws() {
        when(bookRepo.findById("NOPE")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bookLoanService.validateGetBook("NOPE"));
    }

    @Test
    void validateGetBook_decommissioned_throws() {
        Book b = new Book("123", "T", null, 1);
        b.setDecommissioned(true);

        when(bookRepo.findById("123")).thenReturn(Optional.of(b));

        assertThrows(IllegalStateException.class, () -> bookLoanService.validateGetBook("123"));
    }

    @Test
    void createTransaction_setsDueDateBasedOnProperties() {
        User user = new User();
        when(appProperties.getBookBorrowDays()).thenReturn(appProperties.getBookBorrowDays());

        when(transactionRepo.save(any())).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            return t;
        });

        Transaction tx = bookLoanService.createTransaction(user);

        assertNotNull(tx);
        assertEquals(tx.getRequestDate().plusDays(appProperties.getBookBorrowDays()), tx.getDueDate());
    }
}
