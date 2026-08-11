package com.libmanagementsys.vestas_proj.controller;

import com.libmanagementsys.vestas_proj.repository.UserRepository;
import com.libmanagementsys.vestas_proj.service.BookService;
import com.libmanagementsys.vestas_proj.service.UserService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.core.Authentication;

import com.libmanagementsys.vestas_proj.model.User;

@Controller
public class AppController {

    private final BookService bookService;
    private final UserRepository userRepository;
    private final UserService userService;

    AppController(UserService userService, UserRepository userRepository, BookService bookService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/forbidden")
    public String forbidden(Model model, Authentication auth) {
        boolean adm = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        if (adm) {
            model.addAttribute("returnUrl", "/owner");
        } else {
            model.addAttribute("returnUrl", "/client");
        }
        return "forbidden";
    }

    @GetMapping("/owner")
    public String ownerHome(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("books", bookService.getAllBooks());
        return "owner";
    }

    @GetMapping("/client")
    public String clientHome(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("books", bookService.getAllBooks());
        return "client";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@Valid @ModelAttribute("user") User user,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        // Check username and email dupes
        if (userRepository.existsByUsername(user.getUsername())) {
            result.rejectValue(
                    "username",
                    "duplicate_username",
                    "Username already exists.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue(
                    "email",
                    "duplicate_email",
                    "Email already exists.");
        }

        // Protect against HTML tampering of role
        if (user.getRole() == null || (!user.getRole().equals("CLIENT") &&
                !user.getRole().equals("OWNER"))) {

            result.rejectValue(
                    "role",
                    "invalid_role",
                    "Invalid account type.");
        }

        if (result.hasErrors()) {
            System.out.println(result);
            return "signup";
        }

        userService.register(user);

        redirectAttributes.addFlashAttribute("successMessage", "Account created successfully!");

        return "redirect:/login";
    }

}