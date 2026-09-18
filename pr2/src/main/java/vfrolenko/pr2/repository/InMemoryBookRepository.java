package vfrolenko.pr2.repository;

import org.springframework.stereotype.Repository;
import vfrolenko.pr2.entity.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBookRepository implements BookRepository {

    private final Map<UUID, Book> storage = new ConcurrentHashMap<>();

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return storage.containsKey(id);
    }

    @Override
    public Book save(Book book) {
        storage.put(book.id(), book);
        return book;
    }

    @Override
    public void deleteById(UUID id) {
        storage.remove(id);
    }

    @Override
    public boolean existsByTitleAndAuthor(String title, String author) {
        return storage.values().stream()
                .anyMatch(book -> book.title().equalsIgnoreCase(title)
                        && book.author().equalsIgnoreCase(author));
    }
}