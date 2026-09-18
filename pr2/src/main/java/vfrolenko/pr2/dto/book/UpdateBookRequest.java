package vfrolenko.pr2.dto.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateBookRequest(

        @NotBlank(message = "Title must not be blank")
        @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
        String title,

        @NotBlank(message = "Author must not be blank")
        @Size(min = 2, max = 100, message = "Author must be between 2 and 100 characters")
        String author,

        @NotBlank(message = "Genre must not be blank")
        @Size(min = 2, max = 50, message = "Genre must be between 2 and 50 characters")
        String genre,

        @Min(value = 1450, message = "Publication year must be 1450 or later")
        @Max(value = 2100, message = "Publication year must be 2100 or earlier")
        int publicationYear,

        @Positive(message = "Price must be positive")
        double price,

        @Min(value = 1, message = "Pages must be at least 1")
        @Max(value = 10000, message = "Pages must be at most 10000")
        int pages
) {}