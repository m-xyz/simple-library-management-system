package com.libmanagementsys.vestas_proj.model;

public class AddBookRequest {
    private String isbn;
    private String title;
    private String authorName;
    private int stock;

    public AddBookRequest(String isbn, String title, String authorName, int stock) {
        this.isbn = isbn;
        this.title = title;
        this.authorName = authorName;
        this.stock = stock;
    }

    public AddBookRequest() {
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
