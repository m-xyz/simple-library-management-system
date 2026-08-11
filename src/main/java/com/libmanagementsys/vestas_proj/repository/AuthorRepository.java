package com.libmanagementsys.vestas_proj.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.libmanagementsys.vestas_proj.model.Author;

public interface AuthorRepository extends JpaRepository<Author, Integer> {
    Optional<Author> findByNameIgnoreCase(String name); // Case insensitive
}
