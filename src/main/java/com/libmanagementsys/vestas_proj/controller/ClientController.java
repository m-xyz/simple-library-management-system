package com.libmanagementsys.vestas_proj.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.libmanagementsys.vestas_proj.config.AppProperties;
import com.libmanagementsys.vestas_proj.model.Book;
import com.libmanagementsys.vestas_proj.model.BookLoan;
import com.libmanagementsys.vestas_proj.model.User;
import com.libmanagementsys.vestas_proj.service.BookLoanService;
import com.libmanagementsys.vestas_proj.service.UserService;
import com.libmanagementsys.vestas_proj.service.WaitingListService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("client")
public class ClientController {
    private final UserService userService;
    private final BookLoanService bookLoanService;
    private final AppProperties appProperties;
    private final WaitingListService waitingListService;

    public ClientController(
            UserService userService,
            BookLoanService bookLoanService,
            AppProperties appProperties,
            WaitingListService waitingListService) {
        this.userService = userService;
        this.bookLoanService = bookLoanService;
        this.appProperties = appProperties;
        this.waitingListService = waitingListService;
    }

    @PostMapping("/borrow")
    public String borrowBooks(
            @RequestParam("isbns") Set<String> isbns,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(auth.getName());

        try {
            List<String> dupeLoans = bookLoanService.checkForDupeLoans(user, isbns);
            if (!dupeLoans.isEmpty()) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "You already have the following "
                                + (dupeLoans.size() > 1 ? "books" : "book") +
                                " on loan: " + String.join(", ", dupeLoans));
                // Pass dupes
                redirectAttributes.addFlashAttribute("duplicateIsbns", dupeLoans);
                // Pass user selected
                redirectAttributes.addFlashAttribute("selectedIsbns", isbns);

                return "redirect:/client";
            }

            bookLoanService.loanBooks(user, isbns, false);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    (isbns.size() > 1 ? "Books" : "Book")
                            + " successfully borrowed: " + String.join(", ", isbns));

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e);
        }

        return "redirect:/client";
    }

    @GetMapping("/return-book")
    public String returnBookPage(Model model) {
        User user = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        List<BookLoan> loans = bookLoanService.getActiveLoans(user.getId());
        model.addAttribute("bookLoans", loans);
        model.addAttribute("waitingListEntries", waitingListService.getEntriesForUser(user.getId()));
        model.addAttribute("appProperties", appProperties);

        return "return-book";
    }

    @PostMapping("/return-book")
    public String returnBookPost(
            @RequestParam(name = "loanIds", required = false) List<String> loanIds,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        if (loanIds == null || loanIds.isEmpty()) {
            return "redirect:/client/return-book";
        }

        redirectAttributes.addFlashAttribute(
                "successMessages",
                loanIds.stream()
                        .map(loanId -> loanId.split(":"))
                        .map(parts -> bookLoanService.returnBook(Long.valueOf(parts[0]), parts[1]))
                        .map(result -> String.format("Successfully returned '%s' (%s)%s",
                                result.title(),
                                result.isbn(),
                                result.fine().compareTo(BigDecimal.ZERO) > 0
                                        ? String.format(" FINE %.2f €", result.fine())
                                        : ""))
                        .toList());

        return "redirect:/client";
    }

    @PostMapping("/waitlist/{isbn}")
    @ResponseBody
    public ResponseEntity<String> joinWaitlist(
            @PathVariable String isbn,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        try {
            Book book = waitingListService.addToWaitList(userService.findByUsername(auth.getName()), isbn);
            return ResponseEntity.ok(
                    String.format(
                            "Successfully joined waiting list for '%s' (%s)",
                            book.getTitle(),
                            book.getIsbn()));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e);

            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

}
