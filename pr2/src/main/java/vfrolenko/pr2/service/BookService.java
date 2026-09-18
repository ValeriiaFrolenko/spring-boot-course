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
import vfrolenko.pr2.service.strategy.BookPricingStrategy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final Map<String, BookPricingStrategy> pricingStrategies;

    public BookService(BookRepository bookRepository, List<BookPricingStrategy> pricingStrategies) {
        this.bookRepository = bookRepository;
        this.pricingStrategies = pricingStrategies.stream()
                .collect(Collectors.toMap(BookPricingStrategy::name, Function.identity()));
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(UUID id) {
        return bookRepository.findById(id);
    }

    public double calculatePrice(UUID id, String strategyName) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book with id '%s' not found".formatted(id)));

        BookPricingStrategy strategy = Optional.ofNullable(pricingStrategies.get(strategyName.toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown pricing strategy: '%s'".formatted(strategyName)));

        return strategy.calculatePrice(book);
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