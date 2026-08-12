package com.libmanagementsys.vestas_proj.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.libmanagementsys.vestas_proj.model.AddBookRequest;
import com.libmanagementsys.vestas_proj.model.Author;
import com.libmanagementsys.vestas_proj.model.Book;
import com.libmanagementsys.vestas_proj.repository.AuthorRepository;
import com.libmanagementsys.vestas_proj.repository.BookRepository;

@Service
public class BookService {
    private final BookRepository bookRepo;
    private final AuthorRepository authorRepo;

    public BookService(BookRepository bookRepo, AuthorRepository authorRepo) {
        this.bookRepo = bookRepo;
        this.authorRepo = authorRepo;
    }

    // public Book addBook(Book book) {
    public Book addBook(AddBookRequest bookRequest) {

        // Check if the author of book being added already exists
        Author author = authorRepo
                .findByNameIgnoreCase(bookRequest.getAuthorName().trim())
                .orElseGet(() -> {

                    // Save new author
                    Author newAuthor = new Author();
                    newAuthor.setName(bookRequest.getAuthorName().trim());

                    return authorRepo.save(newAuthor);
                });

        Book book = new Book();

        book.setIsbn(bookRequest.getIsbn());
        book.setTitle(bookRequest.getTitle());
        book.setStock(bookRequest.getStock());
        book.setAuthor(author);

        return bookRepo.save(book);
    }

    public Book updateBook(Book book) {
        return bookRepo.save(book);
    }

    public void deleteBook(String isbn) {
        bookRepo.deleteById(isbn);
    }

    public List<Book> getAllBooks() {
        return bookRepo.findAll();
    }

    public Optional<Book> findByIsbn(String isbn) {
        return bookRepo.findById(isbn);
    }

    public boolean existsByIsbn(String isbn) {
        return bookRepo.existsByIsbn(isbn);
    }
}
