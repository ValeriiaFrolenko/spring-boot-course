package vfrolenko.pr2.exception;

import vfrolenko.pr2.entity.BookStatus;

public class InvalidBookStatusTransitionException extends DomainException {
    public InvalidBookStatusTransitionException(BookStatus from, BookStatus to) {
        super("Cannot transition book status from %s to %s".formatted(from, to));
    }
}