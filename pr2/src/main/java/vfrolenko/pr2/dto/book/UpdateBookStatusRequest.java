package vfrolenko.pr2.dto.book;

import jakarta.validation.constraints.NotNull;
import vfrolenko.pr2.entity.BookStatus;

public record UpdateBookStatusRequest(
        @NotNull(message = "Status must not be null")
        BookStatus status
) {}