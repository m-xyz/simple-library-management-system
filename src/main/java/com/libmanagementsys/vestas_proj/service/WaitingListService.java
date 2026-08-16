package com.libmanagementsys.vestas_proj.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.libmanagementsys.vestas_proj.dto.WaitingListEntryDto;
import com.libmanagementsys.vestas_proj.model.Book;
import com.libmanagementsys.vestas_proj.model.User;
import com.libmanagementsys.vestas_proj.model.WaitingListEntry;
import com.libmanagementsys.vestas_proj.repository.WaitingListRepository;

@Service
public class WaitingListService {
    private final WaitingListRepository waitingListRepo;
    private final BookService bookService;
    private final BookLoanService bookLoanService;

    public WaitingListService(
            WaitingListRepository waitingListRepo,
            BookService bookService,
            BookLoanService bookLoanService) {
        this.waitingListRepo = waitingListRepo;
        this.bookService = bookService;
        this.bookLoanService = bookLoanService;
    }

    public Book addToWaitList(User user, String isbn) {
        Book book = bookService.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book with ISBN \'" + isbn + "\' not found."));

        // Check if user is already on waitlist for this book
        if (waitingListRepo.existsByBookIsbnAndUserId(book.getIsbn(), user.getId())) {
            throw new IllegalArgumentException("You are already on the waiting list for \'" + book.getTitle() + "\'");
        }
        // Check if user already has a copy of this book on loan
        if (bookLoanService.checkIfUserIsBorrowingBook(user.getId(), isbn)) {
            throw new IllegalArgumentException(
                    "You already have \'" + book.getTitle() + "\' (" + book.getIsbn() + ") on loan.");
        }

        waitingListRepo.save(
                new WaitingListEntry(
                        book,
                        user,
                        LocalDateTime.now()));

        return book;
    }

    public void removeFromWaitList(WaitingListEntry waitingListEntry) {
        waitingListRepo.delete(waitingListEntry);
    }

    public Optional<WaitingListEntry> getNextInLine(Book book) {
        return waitingListRepo.findFirstByBookIsbnOrderByRequestDateAsc(book.getIsbn());
    }

    public long getCountByBook(String isbn) {
        return waitingListRepo.countByBookIsbn(isbn);
    }

    public List<WaitingListEntryDto> getEntriesForUser(Long userId) {
        List<WaitingListEntry> entries = waitingListRepo.findByUser_IdOrderByRequestDateAsc(userId);

        return entries.stream()
                .map(entry -> {

                    long peopleAhead = waitingListRepo.countPeopleAhead(
                            entry.getBook().getIsbn(),
                            entry.getRequestDate());

                    return new WaitingListEntryDto(
                            entry.getBook(),
                            (int) peopleAhead + 1);
                })
                .toList();

    }

}
