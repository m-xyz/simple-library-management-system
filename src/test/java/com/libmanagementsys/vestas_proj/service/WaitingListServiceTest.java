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

import com.libmanagementsys.vestas_proj.model.Book;
import com.libmanagementsys.vestas_proj.model.User;
import com.libmanagementsys.vestas_proj.model.WaitingListEntry;
import com.libmanagementsys.vestas_proj.repository.WaitingListRepository;

@ExtendWith(MockitoExtension.class)
class WaitingListServiceTest {

    @Mock
    WaitingListRepository waitingListRepo;

    @Mock
    BookService bookService;

    @Mock
    BookLoanService bookLoanService;

    @InjectMocks
    WaitingListService waitingListService;

    @Test
    void addToWaitList_bookNotFound_throws() {
        when(bookService.findByIsbn("X")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> waitingListService.addToWaitList(new User(), "X"));
    }

    @Test
    void addToWaitList_alreadyOnWaitlist_throws() {
        Book b = new Book("1", "T", null, 1);
        User u = new User();
        u.setId(42L);

        when(bookService.findByIsbn("1")).thenReturn(Optional.of(b));
        when(waitingListRepo.existsByBookIsbnAndUserId("1", 42L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> waitingListService.addToWaitList(u, "1"));
    }

    @Test
    void addToWaitList_userBorrowing_throws() {
        Book b = new Book("2", "T2", null, 1);
        User u = new User();
        u.setId(77L);

        when(bookService.findByIsbn("2")).thenReturn(Optional.of(b));
        when(waitingListRepo.existsByBookIsbnAndUserId("2", 77L)).thenReturn(false);
        when(bookLoanService.checkIfUserIsBorrowingBook(77L, "2")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> waitingListService.addToWaitList(u, "2"));
    }

    @Test
    void addToWaitList_success_savesEntryAndReturnsBook() {
        Book b = new Book("3", "T3", null, 1);
        User u = new User();
        u.setId(99L);

        when(bookService.findByIsbn("3")).thenReturn(Optional.of(b));
        when(waitingListRepo.existsByBookIsbnAndUserId("3", 99L)).thenReturn(false);
        when(bookLoanService.checkIfUserIsBorrowingBook(99L, "3")).thenReturn(false);
        when(waitingListRepo.save(any(WaitingListEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = waitingListService.addToWaitList(u, "3");

        assertSame(b, result);
        verify(waitingListRepo).save(any(WaitingListEntry.class));
    }
}
