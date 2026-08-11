package com.libmanagementsys.vestas_proj.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.*;

@Entity
@Table(name = "book_loans")
public class BookLoan {

    @EmbeddedId
    private BookLoanId id; // Composite primary key

    @ManyToOne
    @MapsId("transactionId")
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @ManyToOne
    @MapsId("isbn")
    @JoinColumn(name = "isbn")
    private Book book;

    private LocalDate returnDate;

    @Column(precision = 10, scale = 2) // ???
    private BigDecimal fine = BigDecimal.ZERO;

    public BookLoanId getId() {
        return this.id;
    }

    public void setId(BookLoanId id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Book getBook() {
        return this.book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getReturnDate() {
        return this.returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    //public BigDecimal getFine() {
    //    return this.fine;
    //}

    public void setFine(BigDecimal fine) {
        this.fine = fine;
    }

    @Transient
    public BigDecimal getFine() {
        if(this.transaction == null || transaction.getDueDate() == null) {
            return BigDecimal.ZERO;
        }

        LocalDate today = LocalDate.now();
        if(!today.isAfter(this.transaction.getDueDate())) {
            return BigDecimal.ZERO;
        }

        long daysLate = ChronoUnit.DAYS.between(this.transaction.getDueDate(), today);

        // TODO: Figure out better way to store fine fee
        return new BigDecimal("0.50").multiply(BigDecimal.valueOf(daysLate));
    }

}
