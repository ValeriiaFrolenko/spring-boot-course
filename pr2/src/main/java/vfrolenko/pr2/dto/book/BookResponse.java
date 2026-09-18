package vfrolenko.pr2.dto.book;

import vfrolenko.pr2.entity.Book;
import vfrolenko.pr2.entity.BookStatus;

import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String author,
        String genre,
        int publicationYear,
        double price,
        int pages,
        BookStatus status
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
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
}