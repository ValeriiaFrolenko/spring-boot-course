package vfrolenko.pr2.exception;

public class DuplicateBookException extends RuntimeException {
    public DuplicateBookException(String title, String author) {
        super("Book with title '%s' and author '%s' already exists".formatted(title, author));
    }
}