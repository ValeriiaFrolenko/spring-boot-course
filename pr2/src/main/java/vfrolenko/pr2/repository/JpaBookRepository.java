package vfrolenko.pr2.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import vfrolenko.pr2.entity.Book;
import vfrolenko.pr2.entity.BookJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class JpaBookRepository implements BookRepository {

    private final BookJpaRepository jpaRepository;

    public JpaBookRepository(BookJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Book> findAll() {
        return jpaRepository.findAll().stream()
                .map(JpaBookRepository::toDomain)
                .toList();
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(JpaBookRepository::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Book save(Book book) {
        BookJpaEntity saved = jpaRepository.save(toEntity(book));
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByTitleAndAuthor(String title, String author) {
        return jpaRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(title, author);
    }

    @Override
    public List<Book> findByTitleContaining(String name) {
        return jpaRepository.findByTitleContainingIgnoreCase(name).stream()
                .map(JpaBookRepository::toDomain)
                .toList();
    }

    private static BookJpaEntity toEntity(Book book) {
        return new BookJpaEntity(
                book.id(),
                book.title(),
                book.author(),
                book.genre(),
                book.publicationYear(),
                book.price(),
                book.pages(),
                book.status()
        );
    }

    private static Book toDomain(BookJpaEntity entity) {
        return new Book(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getGenre(),
                entity.getPublicationYear(),
                entity.getPrice(),
                entity.getPages(),
                entity.getStatus()
        );
    }
}