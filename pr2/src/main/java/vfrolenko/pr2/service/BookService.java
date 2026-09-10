package vfrolenko.pr2.service;

import org.springframework.stereotype.Service;
import vfrolenko.pr2.dto.book.CreateBookRequest;
import vfrolenko.pr2.dto.book.UpdateBookRequest;
import vfrolenko.pr2.entity.Book;
import vfrolenko.pr2.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookService {

    private final Map<UUID, Book> storage = new ConcurrentHashMap<>();

    public List<Book> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Book> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Book create(CreateBookRequest request) {
        UUID id = UUID.randomUUID();
        Book book = new Book(
                id,
                request.title(),
                request.author(),
                request.genre(),
                request.publicationYear(),
                request.price(),
                request.pages()
        );
        storage.put(id, book);
        return book;
    }

    public Book update(UUID id, UpdateBookRequest request) {
        if (!storage.containsKey(id)) {
            throw new ResourceNotFoundException("Book with id '%s' not found".formatted(id));
        }
        Book updated = new Book(
                id,
                request.title(),
                request.author(),
                request.genre(),
                request.publicationYear(),
                request.price(),
                request.pages()
        );
        storage.put(id, updated);
        return updated;
    }

    public void delete(UUID id) {
        if (!storage.containsKey(id)) {
            throw new ResourceNotFoundException("Book with id '%s' not found".formatted(id));
        }
        storage.remove(id);
    }
}