package com.libmanagementsys.vestas_proj.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.libmanagementsys.vestas_proj.model.AppProperties;
import com.libmanagementsys.vestas_proj.model.Book;
import com.libmanagementsys.vestas_proj.model.BookLoan;
import com.libmanagementsys.vestas_proj.model.BookLoanId;
import com.libmanagementsys.vestas_proj.model.LoanHistoryEntry;
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
    private final AppProperties appProperties;

    public BookLoanService(
            BookRepository bookRepo,
            TransactionRepository transactionRepo,
            BookLoanRepository bookLoanRepo,
            AppProperties appProperties) {
        this.bookRepo = bookRepo;
        this.transactionRepo = transactionRepo;
        this.bookLoanRepo = bookLoanRepo;
        this.appProperties = appProperties;
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
        transaction.setDueDate(LocalDate.now().plusDays(appProperties.getBookBorrowDays()));
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
            bookLoan.setReturnDate(null); // NULL returnDate means book is still on loan
            bookLoan.setFine(BigDecimal.ZERO);
            bookLoanRepo.save(bookLoan);

        }

    }

    @Transactional
    public void returnBook(Long transactionId, String isbn) {
        Book book = bookRepo
                .findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        BookLoan bookLoan = bookLoanRepo
                .findActiveLoan(transactionId, isbn)
                .orElseThrow(() -> new RuntimeException("Active loan not found"));

        book.addStock(1);

        // Write to DB
        bookLoanRepo.returnBook(transactionId, isbn, LocalDate.now(),
                bookLoan.calculateFine(appProperties.getLateReturnFee()));
        bookRepo.save(book);

    }

    public List<BookLoan> getActiveLoans(Long userId) {
        return bookLoanRepo.findActiveLoansByUserId(userId);
    }

    public List<LoanHistoryEntry> getLoanHistory() {
        return bookLoanRepo.getLoanHistory().stream().map(
                loan -> new LoanHistoryEntry(
                        loan.getReturnDate() == null ? "ACTIVE" : "COMPLETED",
                        loan.getTransaction().getUser().getUsername(),
                        loan.getBook().getTitle(),
                        loan.getBook().getIsbn(),
                        loan.getTransaction().getRequestDate(),
                        loan.getTransaction().getDueDate(),
                        loan.getReturnDate(),
                        loan.getFine()))
                .toList();
    }

    public HashMap<String, HashMap<String, Object>> getOnLoanByIsbn(String isbn) {
        List<BookLoan> b = bookLoanRepo.getOnLoanByIsbn(isbn);
        HashMap<String, HashMap<String, Object>> entry = new HashMap<>();

        if (!b.isEmpty()) {

            HashMap<String, Object> book = new HashMap<>();

            // TITLE
            Book bb = bookRepo.findByIsbn(isbn).orElseThrow();
            book.put("title", bb.getTitle());

            // USERS
            List<HashMap<String, String>> users = new ArrayList<>();

            for (BookLoan bl : b.stream().toList()) {
                HashMap<String, String> userEntry = new HashMap<>();
                userEntry.put("username", bl.getTransaction().getUser().getUsername());
                userEntry.put("requestDate",
                        bl.getTransaction().getRequestDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                userEntry.put("dueDate",
                        bl.getTransaction().getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                userEntry.put("fine", bl.calculateFine(appProperties.getLateReturnFee()).toString());
                users.add(userEntry);
            }

            book.put("users", users);
            entry.put(isbn, book);

        }

        return entry;

    }

    public int getOnLoanCountByIsbn(String isbn) {
        return bookLoanRepo.getOnLoanByIsbn(isbn).size();
    }
}
