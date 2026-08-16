package com.libmanagementsys.vestas_proj.service;

import com.libmanagementsys.vestas_proj.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.libmanagementsys.vestas_proj.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder pwEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder pwEncoder) {
        this.userRepo = userRepo;
        this.pwEncoder = pwEncoder;
    }

    public User register(User user) {
        user.setPassword(pwEncoder.encode(user.getPassword()));

        return userRepo.save(user);

    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(
                        () -> new RuntimeException("User not found"));
    }

}
