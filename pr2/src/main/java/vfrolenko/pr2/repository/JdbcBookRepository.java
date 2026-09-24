package vfrolenko.pr2.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vfrolenko.pr2.entity.Book;
import vfrolenko.pr2.entity.BookStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class JdbcBookRepository implements BookRepository {

    private final JdbcClient jdbcClient;

    public JdbcBookRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private static Book mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Book(
                UUID.fromString(rs.getString("id")),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("genre"),
                rs.getInt("publication_year"),
                rs.getDouble("price"),
                rs.getInt("pages"),
                BookStatus.valueOf(rs.getString("status"))
        );
    }

    @Override
    public List<Book> findAll() {
        return jdbcClient
                .sql("SELECT id, title, author, genre, publication_year, price, pages, status FROM books")
                .query(JdbcBookRepository::mapRow)
                .list();
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return jdbcClient
                .sql("SELECT id, title, author, genre, publication_year, price, pages, status FROM books WHERE id = :id")
                .param("id", id.toString())
                .query(JdbcBookRepository::mapRow)
                .optional();
    }

    @Override
    public boolean existsById(UUID id) {
        Integer count = jdbcClient
                .sql("SELECT COUNT(*) FROM books WHERE id = :id")
                .param("id", id.toString())
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    @Override
    public Book save(Book book) {
        if (existsById(book.id())) {
            jdbcClient
                    .sql("""
                            UPDATE books
                            SET title = :title,
                                author = :author,
                                genre = :genre,
                                publication_year = :publicationYear,
                                price = :price,
                                pages = :pages,
                                status = :status
                            WHERE id = :id
                            """)
                    .param("id", book.id().toString())
                    .param("title", book.title())
                    .param("author", book.author())
                    .param("genre", book.genre())
                    .param("publicationYear", book.publicationYear())
                    .param("price", book.price())
                    .param("pages", book.pages())
                    .param("status", book.status().name())
                    .update();
        } else {
            jdbcClient
                    .sql("""
                            INSERT INTO books (id, title, author, genre, publication_year, price, pages, status)
                            VALUES (:id, :title, :author, :genre, :publicationYear, :price, :pages, :status)
                            """)
                    .param("id", book.id().toString())
                    .param("title", book.title())
                    .param("author", book.author())
                    .param("genre", book.genre())
                    .param("publicationYear", book.publicationYear())
                    .param("price", book.price())
                    .param("pages", book.pages())
                    .param("status", book.status().name())
                    .update();
        }
        return book;
    }

    @Override
    public void deleteById(UUID id) {
        jdbcClient
                .sql("DELETE FROM books WHERE id = :id")
                .param("id", id.toString())
                .update();
    }

    @Override
    public boolean existsByTitleAndAuthor(String title, String author) {
        Integer count = jdbcClient
                .sql("SELECT COUNT(*) FROM books WHERE LOWER(title) = LOWER(:title) AND LOWER(author) = LOWER(:author)")
                .param("title", title)
                .param("author", author)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    @Override
    public List<Book> findByTitleContaining(String name) {
        return jdbcClient
                .sql("SELECT id, title, author, genre, publication_year, price, pages, status FROM books WHERE LOWER(title) LIKE LOWER(:name)")
                .param("name", "%" + name + "%")
                .query(JdbcBookRepository::mapRow)
                .list();
    }
}
