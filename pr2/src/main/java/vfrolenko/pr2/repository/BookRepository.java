package vfrolenko.pr2.repository;

import vfrolenko.pr2.entity.Book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository {
    List<Book> findAll();
    Optional<Book> findById(UUID id);
    boolean existsById(UUID id);
    Book save(Book book);
    void deleteById(UUID id);
    boolean existsByTitleAndAuthor(String title, String author);
}