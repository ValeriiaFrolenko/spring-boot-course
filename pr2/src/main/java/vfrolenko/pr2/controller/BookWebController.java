package vfrolenko.pr2.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vfrolenko.pr2.dto.book.CreateBookRequest;
import vfrolenko.pr2.service.BookService;

@Controller
@RequestMapping("/books")
public class BookWebController {

    private final BookService bookService;

    public BookWebController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new CreateBookRequest("", "", "", 2024, 0.0, 1));
        return "books/create";
    }

    @PostMapping
    public String createBook(
            @Valid @ModelAttribute("book") CreateBookRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "books/create";
        }

        bookService.create(request);
        redirectAttributes.addFlashAttribute("successMessage", "Книгу успішно додано!");
        return "redirect:/books";
    }
}