package com.libmanagementsys.vestas_proj.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.*;

@Embeddable
public class BookLoanId implements Serializable {

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "isbn")
    private String isbn;

    public BookLoanId() {
    }

    public BookLoanId(Long transactionId, String isbn) {
        this.transactionId = transactionId;
        this.isbn = isbn;
    }

    public Long getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BookLoanId))
            return false;

        BookLoanId other = (BookLoanId) o;

        return Objects.equals(this.transactionId, other.transactionId) && Objects.equals(this.isbn, other.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.transactionId, this.isbn);
    }
}
