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

    /*
     * Fetch an author by name, if author isn't present in the db
     * a new entry for it will be created
     */
    public Author fetchAuthor(String authorName) {
        Author author = authorRepo
                .findByNameIgnoreCase(authorName.trim())
                .orElseGet(() -> {

                    Author newAuthor = new Author();
                    newAuthor.setName(authorName.trim());

                    return authorRepo.save(newAuthor);
                });
        return author;
    }

    // Add/Update book
    public Book addBook(AddBookRequest bookRequest) {
        return bookRepo.save(new Book(
                bookRequest.getIsbn(),
                bookRequest.getTitle(),
                fetchAuthor(bookRequest.getAuthorName()),
                bookRequest.getStock()));
    }

    public void decommBook(String isbn) {
        Book book = bookRepo
                .findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        book.setDecommissioned(!book.isDecommissioned());
        bookRepo.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepo.findAll();
    }

    public List<Book> getAllDecommissionedBooks() {
        return bookRepo.findByDecommissionedTrue();
    }

    public List<Book> getAllNonDecommissionedBooks() {
        return bookRepo.findByDecommissionedFalse();
    }

    public Optional<Book> findByIsbn(String isbn) {
        return bookRepo.findById(isbn);
    }

    public boolean existsByIsbn(String isbn) {
        return bookRepo.existsByIsbn(isbn);
    }
}
