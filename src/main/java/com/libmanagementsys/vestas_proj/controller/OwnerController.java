package com.libmanagementsys.vestas_proj.controller;

import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.libmanagementsys.vestas_proj.model.AddBookRequest;
import com.libmanagementsys.vestas_proj.service.BookLoanService;
import com.libmanagementsys.vestas_proj.service.BookService;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    private final BookService bookService;
    private final BookLoanService bookLoanService;

    public OwnerController(BookService bookService, BookLoanService bookLoanService) {
        this.bookService = bookService;
        this.bookLoanService = bookLoanService;
    }

    @GetMapping("/add-book")
    public String addBookPage(Model model) {
        model.addAttribute("bookRequest", new AddBookRequest());

        return "add-book";
    }

    @PostMapping("/add-book")
    public String addBook(@ModelAttribute("bookRequest") AddBookRequest bookRequest,
            RedirectAttributes redirectAttributes,
            BindingResult result) {

        if (bookService.existsByIsbn(bookRequest.getIsbn())) {
            result.rejectValue("isbn",
                    "duplicate_isbn",
                    "A book with this ISBN already exists.");
        }

        if (result.hasErrors()) {
            return "add-book";
        }

        bookService.addBook(bookRequest);
        redirectAttributes.addFlashAttribute("addBookSuccessMessage",
                "Successfully added " + bookRequest.getStock() + " copies of \'" + bookRequest.getTitle() + "\' ("
                        + bookRequest.getIsbn() + ")");

        return "redirect:/owner";
    }

    @GetMapping("/loan-history")
    public String loanHistory(Model model) {

        model.addAttribute("loanHistory", bookLoanService.getLoanHistory());

        return "loan-history";
    }

    @GetMapping("/loans/{isbn}")
    @ResponseBody
    public HashMap<String, HashMap<String, Object>> getLoans(@PathVariable String isbn) {
        return bookLoanService.getOnLoanByIsbn(isbn);
    }

}
