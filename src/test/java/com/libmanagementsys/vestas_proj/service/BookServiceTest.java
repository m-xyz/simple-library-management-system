package com.libmanagementsys.vestas_proj.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.libmanagementsys.vestas_proj.dto.AddBookRequestDto;
import com.libmanagementsys.vestas_proj.model.Author;
import com.libmanagementsys.vestas_proj.model.Book;
import com.libmanagementsys.vestas_proj.repository.AuthorRepository;
import com.libmanagementsys.vestas_proj.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository bookRepo;

    @Mock
    AuthorRepository authorRepo;

    @InjectMocks
    BookService bookService;

    @Test
    void fetchAuthor_whenExists_returnsAuthor() {
        Author a = new Author();
        a.setName("Jane Doe");

        when(authorRepo.findByNameIgnoreCase("Jane Doe")).thenReturn(Optional.of(a));

        Author result = bookService.fetchAuthor("Jane Doe");

        assertSame(a, result);
        verify(authorRepo, times(1)).findByNameIgnoreCase("Jane Doe");
        verify(authorRepo, never()).save(any());
    }

    @Test
    void fetchAuthor_whenMissing_createsAndSaves() {
        when(authorRepo.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(authorRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Author result = bookService.fetchAuthor("  New Author  ");

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepo).save(captor.capture());

        assertEquals("New Author", captor.getValue().getName());
        assertEquals("New Author", result.getName());
    }

    @Test
    void addBook_savesBookWithAuthorAndStock() {
        Author a = new Author();
        a.setName("Author X");

        when(authorRepo.findByNameIgnoreCase("Author X")).thenReturn(Optional.of(a));
        when(bookRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AddBookRequestDto dto = new AddBookRequestDto("ISBN123", "Title", "Author X", 3);

        Book saved = bookService.addBook(dto);

        assertEquals("ISBN123", saved.getIsbn());
        assertEquals("Title", saved.getTitle());
        assertEquals(3, saved.getStock());
        assertSame(a, saved.getAuthor());
        verify(bookRepo).save(saved);
    }
}
