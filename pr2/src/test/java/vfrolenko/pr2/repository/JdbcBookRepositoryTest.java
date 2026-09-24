package vfrolenko.pr2.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import vfrolenko.pr2.entity.Book;
import vfrolenko.pr2.entity.BookStatus;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcBookRepository.class)
class JdbcBookRepositoryTest {

    @Autowired
    private JdbcBookRepository repository;

    private static final UUID ID = UUID.randomUUID();

    private Book baseBook() {
        return new Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.DRAFT);
    }

    @BeforeEach
    void cleanUp() {
        repository.deleteById(ID);
    }

    @Test
    void save_newBook_canBeFoundById() {
        repository.save(baseBook());

        Optional<Book> result = repository.findById(ID);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Clean Code");
        assertThat(result.get().author()).isEqualTo("Robert Martin");
        assertThat(result.get().genre()).isEqualTo("Programming");
        assertThat(result.get().publicationYear()).isEqualTo(2008);
        assertThat(result.get().price()).isEqualTo(29.99);
        assertThat(result.get().pages()).isEqualTo(431);
        assertThat(result.get().status()).isEqualTo(BookStatus.DRAFT);
    }

    @Test
    void save_existingId_updatesRecord() {
        repository.save(baseBook());

        Book updated = new Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.PUBLISHED);
        repository.save(updated);

        Optional<Book> result = repository.findById(ID);
        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(BookStatus.PUBLISHED);
    }

    @Test
    void findById_nonExistingId_returnsEmpty() {
        Optional<Book> result = repository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_afterSave_containsBook() {
        repository.save(baseBook());

        List<Book> all = repository.findAll();

        assertThat(all).anyMatch(b -> b.id().equals(ID));
    }

    @Test
    void findAll_emptyTable_returnsEmptyList() {
        List<Book> all = repository.findAll();

        assertThat(all).noneMatch(b -> b.id().equals(ID));
    }

    @Test
    void existsById_savedBook_returnsTrue() {
        repository.save(baseBook());

        assertThat(repository.existsById(ID)).isTrue();
    }

    @Test
    void existsById_nonExistingId_returnsFalse() {
        assertThat(repository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    void deleteById_savedBook_canNoLongerBeFound() {
        repository.save(baseBook());

        repository.deleteById(ID);

        assertThat(repository.findById(ID)).isEmpty();
        assertThat(repository.existsById(ID)).isFalse();
    }

    @Test
    void existsByTitleAndAuthor_exactMatch_returnsTrue() {
        repository.save(baseBook());

        assertThat(repository.existsByTitleAndAuthor("Clean Code", "Robert Martin")).isTrue();
    }

    @Test
    void existsByTitleAndAuthor_caseInsensitive_returnsTrue() {
        repository.save(baseBook());

        assertThat(repository.existsByTitleAndAuthor("clean code", "robert martin")).isTrue();
    }

    @Test
    void existsByTitleAndAuthor_wrongAuthor_returnsFalse() {
        repository.save(baseBook());

        assertThat(repository.existsByTitleAndAuthor("Clean Code", "Someone Else")).isFalse();
    }

    @Test
    void existsByTitleAndAuthor_noBooks_returnsFalse() {
        assertThat(repository.existsByTitleAndAuthor("Clean Code", "Robert Martin")).isFalse();
    }

    @Test
    void findByTitleContaining_partialMatch_returnsBook() {
        repository.save(baseBook());

        List<Book> result = repository.findByTitleContaining("Clean");

        assertThat(result).anyMatch(b -> b.id().equals(ID));
    }

    @Test
    void findByTitleContaining_caseInsensitive_returnsBook() {
        repository.save(baseBook());

        List<Book> result = repository.findByTitleContaining("clean");

        assertThat(result).anyMatch(b -> b.id().equals(ID));
    }

    @Test
    void findByTitleContaining_noMatch_returnsEmptyList() {
        repository.save(baseBook());

        List<Book> result = repository.findByTitleContaining("XYZ_NO_MATCH");

        assertThat(result).noneMatch(b -> b.id().equals(ID));
    }

    @Test
    void findByTitleContaining_doesNotReturnUnrelatedBook() {
        repository.save(baseBook());

        UUID otherId = UUID.randomUUID();
        Book other = new Book(otherId, "Refactoring", "Martin Fowler", "Programming", 1999, 49.99, 448, BookStatus.DRAFT);
        repository.save(other);

        List<Book> result = repository.findByTitleContaining("Refactoring");

        assertThat(result).anyMatch(b -> b.id().equals(otherId));
        assertThat(result).noneMatch(b -> b.id().equals(ID));

        repository.deleteById(otherId);
    }
}