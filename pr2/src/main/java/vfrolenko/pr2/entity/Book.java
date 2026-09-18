package vfrolenko.pr2.entity;

import java.util.UUID;

public record Book(
        UUID id,
        String title,
        String author,
        String genre,
        int publicationYear,
        double price,
        int pages,
        BookStatus status
) {}