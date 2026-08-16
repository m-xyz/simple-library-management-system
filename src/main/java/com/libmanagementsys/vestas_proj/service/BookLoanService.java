package com.libmanagementsys.vestas_proj.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.libmanagementsys.vestas_proj.config.AppProperties;
import com.libmanagementsys.vestas_proj.dto.LoanHistoryEntryDto;
import com.libmanagementsys.vestas_proj.dto.ReturnBookResultDto;
import com.libmanagementsys.vestas_proj.event.BookReturnedEvent;
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
    private final BookService bookService;
    private final BookRepository bookRepo;
    private final TransactionRepository transactionRepo;
    private final BookLoanRepository bookLoanRepo;
    private final AppProperties appProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    public BookLoanService(
            BookRepository bookRepo,
            TransactionRepository transactionRepo,
            BookLoanRepository bookLoanRepo,
            AppProperties appProperties,
            ApplicationEventPublisher eventPublisher,
            BookService bookService,
            UserService userService) {
        this.bookRepo = bookRepo;
        this.transactionRepo = transactionRepo;
        this.bookLoanRepo = bookLoanRepo;
        this.appProperties = appProperties;
        this.eventPublisher = eventPublisher;
        this.bookService = bookService;
        this.userService = userService;
    }

    public Transaction createTransaction(User user) {
        return transactionRepo.save(new Transaction(
                user,
                LocalDate.now(),
                LocalDate.now().plusDays(appProperties.getBookBorrowDays())));
    }

    public void validateBookLoanRequest(Set<String> isbns) {
        if (isbns == null || isbns.isEmpty()) {
            throw new IllegalArgumentException("No books selected.");
        }
    }

    public void createBookLoan(Book book, Transaction transaction) {
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

    Book validateGetBook(String isbn) {
        // Sanity check for book's existance
        Book book = bookRepo.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Book with ISBN \'" + isbn + "\' not found."));

        if (book.isDecommissioned()) {
            throw new IllegalStateException("Book with ISBN \'" + isbn + "\' has been decommissioned.");
        }

        return book;

    }

    @Transactional // Rollback if at any point an exception is thrown
    public void loanBooks(User user, Set<String> isbns, boolean handOver) {

        validateBookLoanRequest(isbns);

        Transaction transaction = createTransaction(user);

        for (String isbn : isbns) {

            Book book = validateGetBook(isbn);

            // Update stock accordingly
            if (!handOver) {
                bookService.updateStock(book, -1);
                // Check if book is still available
                // if (book.getStock() <= 0) {
                // throw new IllegalStateException("Book with ISBN \'" + isbn + "\' is out of
                // stock.");
                // }
            }

            createBookLoan(book, transaction);

        }

    }

    @Transactional
    public ReturnBookResultDto returnBook(Long transactionId, String isbn) {
        Book book = bookRepo
                .findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("No book found with ISBN: \'" + isbn + "\'"));
        BookLoan bookLoan = bookLoanRepo
                .findActiveLoan(transactionId, isbn)
                .orElseThrow(() -> new RuntimeException(
                        "No active loan found for: \'" + book.getTitle() + "\' (" + book.getIsbn() + ")"));

        BigDecimal fine = bookLoan.calculateFine(appProperties.getLateReturnFee());

        // Write transaction to DB
        bookLoanRepo.returnBook(
                transactionId,
                isbn,
                LocalDate.now(),
                fine);

        // Anti-snatch
        /*
         * User A returns ISBN 123
         * |
         * ↓
         * stock becomes 1
         * |
         * | User B
         * | |
         * | ↓
         * | sees stock = 1
         * | |
         * | ↓
         * | BORROW
         * |
         * ↓
         * Waiting-list handler
         * |
         * ↓
         * User C gets it
         */
        eventPublisher.publishEvent(new BookReturnedEvent(book));

        return new ReturnBookResultDto(book.getTitle(), isbn, fine);

    }

    public List<BookLoan> getActiveLoans(Long userId) {
        return bookLoanRepo.findActiveLoansByUserId(userId);
    }

    public List<LoanHistoryEntryDto> getLoanHistory() {
        return bookLoanRepo.getLoanHistory().stream().map(
                loan -> new LoanHistoryEntryDto(
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

    public boolean checkIfUserIsBorrowingBook(Long userId, String isbn) {
        return bookLoanRepo.checkIfUserIsBorrowingBook(userId, isbn);
    }

    public List<String> checkForDupeLoans(User user, Set<String> isbns) {

        return isbns.stream()
                .filter(getActiveLoans(user.getId()).stream()
                        .map(loan -> loan.getBook().getIsbn())
                        .collect(Collectors.toSet())::contains)
                .toList();
    }
}
