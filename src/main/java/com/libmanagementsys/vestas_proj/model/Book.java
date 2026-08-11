package com.libmanagementsys.vestas_proj.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id
    private String isbn;

    private String title;

    private int stock;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    public String getId() {
        return this.isbn;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Author getAuthor() {
        return this.author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public int getStock() {
        return this.stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void addStock(int quantity) {
        this.stock += quantity;
    }

}
