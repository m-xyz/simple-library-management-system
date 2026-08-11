package com.libmanagementsys.vestas_proj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.libmanagementsys.vestas_proj.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  User findByEmail(String email);

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}