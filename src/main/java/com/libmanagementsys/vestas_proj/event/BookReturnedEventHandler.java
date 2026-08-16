package com.libmanagementsys.vestas_proj.event;

import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.libmanagementsys.vestas_proj.model.Book;
import com.libmanagementsys.vestas_proj.service.BookLoanService;
import com.libmanagementsys.vestas_proj.service.BookService;
import com.libmanagementsys.vestas_proj.service.WaitingListService;

@Component
public class BookReturnedEventHandler {
    private final BookService bookService;
    private final WaitingListService waitingListService;
    private final BookLoanService bookLoanService;

    public BookReturnedEventHandler(BookService bookService,
            WaitingListService waitingListService,
            BookLoanService bookLoanService) {
        this.bookService = bookService;
        this.waitingListService = waitingListService;
        this.bookLoanService = bookLoanService;
    }

    // This only happens after returnBook() complets the db commit successfully
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBookReturned(BookReturnedEvent event) {
        Book b = event.book();

        waitingListService.getNextInLine(b)
                .ifPresentOrElse(entry -> {
                    // If there are clients waiting for this book get next in line
                    bookLoanService.loanBooks(
                            entry.getUser(),
                            Set.of(entry.getBook().getIsbn()),
                            true // This is a handover loan, stock should not be updated
                        );
                    waitingListService.removeFromWaitList(entry);
                },
                        // If there aren't any clients waiting for this book return it back to stock
                        () -> bookService.updateStock(b, 1));

    }
}
