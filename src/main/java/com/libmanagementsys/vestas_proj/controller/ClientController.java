package com.libmanagementsys.vestas_proj.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.libmanagementsys.vestas_proj.model.BookLoan;
import com.libmanagementsys.vestas_proj.model.User;
import com.libmanagementsys.vestas_proj.service.BookLoanService;
import com.libmanagementsys.vestas_proj.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("client")
public class ClientController {
    private final UserService userService;
    private final BookLoanService bookLoanService;

    public ClientController(UserService userService, BookLoanService bookLoanService) {
        this.userService = userService;
        this.bookLoanService = bookLoanService;
    }

    @PostMapping("/borrow")
    public String borrowBooks(
            @RequestParam("isbns") Set<String> isbns,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(auth.getName());

        // Check if the user requesting the book already has a copy of it on loan
        // validDupes() ?
        Set<String> currentLoans = bookLoanService.getActiveLoans(user.getId()).stream()
                .map(loan -> loan.getBook().getIsbn())
                .collect(Collectors.toSet());

        List<String> dupeLoans = currentLoans.stream()
                .filter(isbns::contains)
                .toList();

        if (!dupeLoans.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "You already have the following "
                            + (dupeLoans.size() > 1 ? "books" : "book") + " on loan: " + String.join(", ", dupeLoans));
            // Pass dupes
            redirectAttributes.addFlashAttribute("duplicateIsbns", dupeLoans);
            // Pass user selected
            redirectAttributes.addFlashAttribute("selectedIsbns", isbns);

            return "redirect:/client";
        }

        bookLoanService.loanBooks(user, isbns);

        redirectAttributes.addFlashAttribute("successMessage",
                (isbns.size() > 1 ? "Books" : "Book") + " successfully borrowed: " + String.join(", ", isbns));

        return "redirect:/client";
    }

    @GetMapping("/return-book")
    public String returnBookPage(Model model) {
        User user = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        List<BookLoan> loans = bookLoanService.getActiveLoans(user.getId());
        model.addAttribute("bookLoans", loans);

        return "return-book";
    }

    @PostMapping("/return-book")
    public String postMethodName(
            @RequestParam(name = "loanIds", required = false) List<String> loanIds,
            Authentication auth) {

        if (loanIds == null || loanIds.isEmpty()) {
            return "redirect:/client/return-book";
        }

        for (String loanId : loanIds) {
            String[] loanIdList = loanId.split(":");

            Long transactionId = Long.valueOf(loanIdList[0]);
            String isbn = loanIdList[1];

            bookLoanService.returnBook(transactionId, isbn);
        }

        return "redirect:/client";
    }

}
