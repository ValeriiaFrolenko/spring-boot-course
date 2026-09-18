package vfrolenko.pr2.service;

import org.springframework.stereotype.Service;
import vfrolenko.pr2.dto.book.CreateBookRequest;
import vfrolenko.pr2.dto.book.UpdateBookRequest;
import vfrolenko.pr2.entity.Book;
import vfrolenko.pr2.entity.BookStatus;
import vfrolenko.pr2.exception.DuplicateBookException;
import vfrolenko.pr2.exception.InvalidBookStatusTransitionException;
import vfrolenko.pr2.exception.ResourceNotFoundException;
import vfrolenko.pr2.repository.BookRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(UUID id) {
        return bookRepository.findById(id);
    }

    public Book create(CreateBookRequest request) {
        if (bookRepository.existsByTitleAndAuthor(request.title(), request.author())) {
            throw new DuplicateBookException(request.title(), request.author());
        }
        Book book = new Book(
                UUID.randomUUID(),
                request.title(),
                request.author(),
                request.genre(),
                request.publicationYear(),
                request.price(),
                request.pages(),
                BookStatus.DRAFT
        );
        return bookRepository.save(book);
    }

    public Book update(UUID id, UpdateBookRequest request) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book with id '%s' not found".formatted(id)));

        boolean titleOrAuthorChanged = !existing.title().equalsIgnoreCase(request.title())
                || !existing.author().equalsIgnoreCase(request.author());

        if (titleOrAuthorChanged && bookRepository.existsByTitleAndAuthor(request.title(), request.author())) {
            throw new DuplicateBookException(request.title(), request.author());
        }

        if (!existing.status().canTransitionTo(request.status())) {
            throw new InvalidBookStatusTransitionException(existing.status(), request.status());
        }

        Book updated = new Book(
                id,
                request.title(),
                request.author(),
                request.genre(),
                request.publicationYear(),
                request.price(),
                request.pages(),
                request.status()
        );
        return bookRepository.save(updated);
    }

    public void delete(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book with id '%s' not found".formatted(id));
        }
        bookRepository.deleteById(id);
    }
}