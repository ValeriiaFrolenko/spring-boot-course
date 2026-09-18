package vfrolenko.pr2.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vfrolenko.pr2.dto.book.BookResponse;
import vfrolenko.pr2.dto.book.CreateBookRequest;
import vfrolenko.pr2.dto.book.UpdateBookRequest;
import vfrolenko.pr2.exception.ResourceNotFoundException;
import vfrolenko.pr2.service.BookService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAll() {
        List<BookResponse> books = bookService.findAll().stream()
                .map(BookResponse::from)
                .toList();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable UUID id) {
        return bookService.findById(id)
                .map(BookResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book with id '%s' not found".formatted(id)));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateBookRequest request) {
        UUID createdId = bookService.create(request).id();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateBookRequest request) {
        BookResponse updated = BookResponse.from(bookService.update(id, request));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/price")
    public ResponseEntity<Double> calculatePrice(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "STANDARD") String strategy) {
        return ResponseEntity.ok(bookService.calculatePrice(id, strategy));
    }
}