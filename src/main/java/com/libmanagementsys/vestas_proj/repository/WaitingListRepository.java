package com.libmanagementsys.vestas_proj.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.libmanagementsys.vestas_proj.model.WaitingListEntry;

public interface WaitingListRepository extends JpaRepository<WaitingListEntry, Long> {

    Optional<WaitingListEntry> findFirstByBookIsbnOrderByRequestDateAsc(String isbn);

    boolean existsByBookIsbnAndUserId(String isbn, Long userId);

    List<WaitingListEntry> findByUser_IdOrderByRequestDateAsc(Long userId);

    @Query("""
                SELECT COUNT(w)
                FROM WaitingListEntry w
                WHERE w.book.isbn = :isbn
                  AND w.requestDate < :requestDate
            """)
    long countPeopleAhead(
            @Param("isbn") String isbn,
            @Param("requestDate") LocalDateTime requestDate);

    long countByBookIsbn(String isbn);
}
