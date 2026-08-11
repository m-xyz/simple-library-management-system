package com.libmanagementsys.vestas_proj.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.libmanagementsys.vestas_proj.model.Book;
import com.libmanagementsys.vestas_proj.model.BookLoan;
import com.libmanagementsys.vestas_proj.model.BookLoanId;
import com.libmanagementsys.vestas_proj.model.Transaction;
import com.libmanagementsys.vestas_proj.model.User;
import com.libmanagementsys.vestas_proj.repository.BookLoanRepository;
import com.libmanagementsys.vestas_proj.repository.BookRepository;
import com.libmanagementsys.vestas_proj.repository.TransactionRepository;

import jakarta.transaction.Transactional;

@Service
public class BookLoanService {
    private final BookRepository bookRepo;
    private final TransactionRepository transactionRepo;
    private final BookLoanRepository bookLoanRepo;

    public BookLoanService(
            BookRepository bookRepo,
            TransactionRepository transactionRepo,
            BookLoanRepository bookLoanRepo) {
        this.bookRepo = bookRepo;
        this.transactionRepo = transactionRepo;
        this.bookLoanRepo = bookLoanRepo;
    }

    // TODO: Create function for each step
    @Transactional // Rollback if at any point an exception is thrown
    public void loanBooks(User user, Set<String> isbns) {

        // validateRequest()
        if (isbns == null || isbns.isEmpty()) {
            throw new IllegalArgumentException("No books selected.");
        }

        // createTransaction()
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setRequestDate(LocalDate.now());
        transaction.setDueDate(LocalDate.now().plusDays(14));
        transaction = transactionRepo.save(transaction);

        // createBookLoans()
        for (String isbn : isbns) {
            // Sanity check for book's existance
            Book book = bookRepo.findById(isbn)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Book with ISBN \'" + isbn + "\' not found."));

            // Check if book is still available
            if (book.getStock() <= 0) {
                throw new IllegalStateException("Book with ISBN \'" + isbn + "\' is out of stock.");
            }

            // Decrease book stock
            book.addStock(-1);
            bookRepo.save(book);

            // createBookLoan()
            BookLoan bookLoan = new BookLoan();
            BookLoanId bookLoanId = new BookLoanId();
            bookLoanId.setTransactionId(transaction.getTransactionId()); // (1) Composite PK
            bookLoanId.setIsbn(book.getIsbn()); // (2) Composite PK
            bookLoan.setId(bookLoanId); // Set composite PK
            bookLoan.setTransaction(transaction);
            bookLoan.setBook(book);
            // Book has yet to be returned (TODO: Improve this maybe)
            bookLoan.setReturnDate(null);
            // No fine
            bookLoan.setFine(BigDecimal.ZERO);
            bookLoanRepo.save(bookLoan);

        }

    }

    @Transactional
    public void returnBook(Long transactionId, String isbn) {
        Transaction transaction = transactionRepo
                .findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found."));
        Book book = bookRepo
                .findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        BookLoan bookLoan = bookLoanRepo
                .findActiveLoan(transactionId, isbn)
                .orElseThrow(() -> new RuntimeException("Active loan not found"));

        book.addStock(1);

        // Write to DB
        bookLoanRepo.returnBook(transactionId, isbn, LocalDate.now(), bookLoan.getFine());
        bookRepo.save(book);

    }

    public List<BookLoan> getActiveLoans(Long userId) {
        return bookLoanRepo.findActiveLoansByUserId(userId);
    }
}
