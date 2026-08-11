package com.libmanagementsys.vestas_proj.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.libmanagementsys.vestas_proj.model.Book;

public interface BookRepository extends JpaRepository<Book, String> {
    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);
}
