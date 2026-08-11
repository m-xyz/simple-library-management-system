package com.libmanagementsys.vestas_proj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.libmanagementsys.vestas_proj.model.BookLoan;
import com.libmanagementsys.vestas_proj.model.BookLoanId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookLoanRepository extends JpaRepository<BookLoan, BookLoanId> {
    @Query("""
            SELECT bl
            FROM BookLoan bl
            JOIN bl.transaction t
            WHERE t.user.id = :userId
            AND bl.returnDate IS NULL
                """)
    List<BookLoan> findActiveLoansByUserId(Long userId);

    @Modifying
    @Query("""
            UPDATE BookLoan bl
            SET bl.returnDate = :returnDate,
                bl.fine = :fine
            WHERE bl.id.transactionId = :transactionId
            AND bl.id.isbn = :isbn
            AND bl.returnDate IS NULL
                """)
    int returnBook(
            @Param("transactionId") Long transactionId,
            @Param("isbn") String isbn,
            @Param("returnDate") LocalDate returnDate,
            @Param("fine") BigDecimal fine);

    @Query("""
            SELECT bl
            FROM BookLoan bl
            WHERE bl.id.transactionId = :transactionId
            AND bl.id.isbn = :isbn
            AND bl.returnDate IS NULL
                """)
    Optional<BookLoan> findActiveLoan(
            @Param("transactionId") Long transactionId,
            @Param("isbn") String isbn);

    @Query("""
            SELECT bl
            FROM BookLoan bl
            JOIN bl.transaction t
            JOIN bl.book b
            ORDER BY t.requestDate DESC
                """)
    List<BookLoan> getLoanHistory();
}
