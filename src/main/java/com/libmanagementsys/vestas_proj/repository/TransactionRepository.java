package com.libmanagementsys.vestas_proj.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.libmanagementsys.vestas_proj.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
