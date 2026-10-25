package vfrolenko.pr2.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import vfrolenko.pr2.TestcontainersConfiguration;
import vfrolenko.pr2.entity.Book;
import vfrolenko.pr2.entity.BookStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaBookRepository.class})
class JpaBookRepositoryTest {

    @Autowired
    private JpaBookRepository repository;

    private static final UUID ID = UUID.randomUUID();

    private Book baseBook() {
        return new Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.DRAFT);
    }

    @Test
    void save_newBook_canBeFoundById() {
        repository.save(baseBook());

        var result = repository.findById(ID);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Clean Code");
        assertThat(result.get().status()).isEqualTo(BookStatus.DRAFT);
    }

    @Test
    void existsByTitleAndAuthor_caseInsensitive_returnsTrue() {
        repository.save(baseBook());

        boolean exists = repository.existsByTitleAndAuthor("clean code", "robert martin");

        assertThat(exists).isTrue();
    }

    @Test
    void findByTitleContaining_partialMatch_returnsBook() {
        repository.save(baseBook());

        List<Book> result = repository.findByTitleContaining("Clean");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(ID);
    }
}