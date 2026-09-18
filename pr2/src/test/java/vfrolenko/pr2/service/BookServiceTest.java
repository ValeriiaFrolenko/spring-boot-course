package vfrolenko.pr2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vfrolenko.pr2.dto.book.CreateBookRequest;
import vfrolenko.pr2.dto.book.UpdateBookRequest;
import vfrolenko.pr2.dto.book.UpdateBookStatusRequest;
import vfrolenko.pr2.entity.Book;
import vfrolenko.pr2.entity.BookStatus;
import vfrolenko.pr2.exception.DuplicateBookException;
import vfrolenko.pr2.exception.InvalidBookStatusTransitionException;
import vfrolenko.pr2.exception.ResourceNotFoundException;
import vfrolenko.pr2.repository.BookRepository;
import vfrolenko.pr2.service.strategy.BookPricingStrategy;
import vfrolenko.pr2.service.strategy.DiscountedPricingStrategy;
import vfrolenko.pr2.service.strategy.StandardPricingStrategy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    private static final UUID ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        List<BookPricingStrategy> strategies = List.of(
                new StandardPricingStrategy(),
                new DiscountedPricingStrategy()
        );
        bookService = new BookServiceImpl(bookRepository, strategies);
    }

    private Book draftBook() {
        return new Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.DRAFT);
    }

    private Book publishedBook() {
        return new Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.PUBLISHED);
    }

    private CreateBookRequest validCreateRequest() {
        return new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431);
    }

    private UpdateBookRequest validUpdateRequest() {
        return new UpdateBookRequest("Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431);
    }

    private UpdateBookStatusRequest statusRequest(BookStatus status) {
        return new UpdateBookStatusRequest(status);
    }

    // --- create ---

    @Test
    void create_validRequest_savesAndReturnsBook() {
        when(bookRepository.existsByTitleAndAuthor("Clean Code", "Robert Martin")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.create(validCreateRequest());

        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.status()).isEqualTo(BookStatus.DRAFT);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void create_duplicateTitleAndAuthor_throwsDuplicateBookException() {
        when(bookRepository.existsByTitleAndAuthor("Clean Code", "Robert Martin")).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(validCreateRequest()))
                .isInstanceOf(DuplicateBookException.class)
                .hasMessageContaining("Clean Code")
                .hasMessageContaining("Robert Martin");

        verify(bookRepository, never()).save(any());
    }

    // --- update ---

    @Test
    void update_validRequest_returnsUpdatedBook() {
        when(bookRepository.findById(ID)).thenReturn(Optional.of(draftBook()));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.update(ID, validUpdateRequest());

        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.status()).isEqualTo(BookStatus.DRAFT);
    }

    @Test
    void update_nonExistingId_throwsResourceNotFoundException() {
        when(bookRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update(ID, validUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ID.toString());
    }

    @Test
    void update_duplicateTitleAndAuthorOnChange_throwsDuplicateBookException() {
        Book existing = new Book(ID, "Old Title", "Old Author", "Programming", 2008, 29.99, 431, BookStatus.DRAFT);
        when(bookRepository.findById(ID)).thenReturn(Optional.of(existing));
        when(bookRepository.existsByTitleAndAuthor("Clean Code", "Robert Martin")).thenReturn(true);

        assertThatThrownBy(() -> bookService.update(ID, validUpdateRequest()))
                .isInstanceOf(DuplicateBookException.class);

        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateStatus_validTransition_returnsUpdatedBook() {
        when(bookRepository.findById(ID)).thenReturn(Optional.of(draftBook()));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.updateStatus(ID, statusRequest(BookStatus.PUBLISHED));

        assertThat(result.status()).isEqualTo(BookStatus.PUBLISHED);
    }

    @Test
    void updateStatus_invalidTransition_throwsInvalidBookStatusTransitionException() {
        when(bookRepository.findById(ID)).thenReturn(Optional.of(publishedBook()));

        assertThatThrownBy(() -> bookService.updateStatus(ID, statusRequest(BookStatus.DRAFT)))
                .isInstanceOf(InvalidBookStatusTransitionException.class)
                .hasMessageContaining("PUBLISHED")
                .hasMessageContaining("DRAFT");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateStatus_nonExistingId_throwsResourceNotFoundException() {
        when(bookRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateStatus(ID, statusRequest(BookStatus.PUBLISHED)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ID.toString());
    }

    // --- delete ---

    @Test
    void delete_existingId_deletesBook() {
        when(bookRepository.existsById(ID)).thenReturn(true);

        bookService.delete(ID);

        verify(bookRepository).deleteById(ID);
    }

    @Test
    void delete_nonExistingId_throwsResourceNotFoundException() {
        when(bookRepository.existsById(ID)).thenReturn(false);

        assertThatThrownBy(() -> bookService.delete(ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ID.toString());

        verify(bookRepository, never()).deleteById(any());
    }

    // --- calculatePrice ---

    @Test
    void calculatePrice_standardStrategy_returnsOriginalPrice() {
        when(bookRepository.findById(ID)).thenReturn(Optional.of(draftBook()));

        double price = bookService.calculatePrice(ID, "STANDARD");

        assertThat(price).isEqualTo(29.99);
    }

    @Test
    void calculatePrice_discountedStrategy_returnsReducedPrice() {
        when(bookRepository.findById(ID)).thenReturn(Optional.of(draftBook()));

        double price = bookService.calculatePrice(ID, "DISCOUNTED");

        assertThat(price).isEqualTo(29.99 * 0.80, within(0.001));
    }

    @Test
    void calculatePrice_unknownStrategy_throwsIllegalArgumentException() {
        when(bookRepository.findById(ID)).thenReturn(Optional.of(draftBook()));

        assertThatThrownBy(() -> bookService.calculatePrice(ID, "UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void calculatePrice_nonExistingBook_throwsResourceNotFoundException() {
        when(bookRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.calculatePrice(ID, "STANDARD"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- update boundary: same-state transitions ---


    @Test
    void updateStatus_sameStatusDraft_throwsInvalidBookStatusTransitionException() {
        when(bookRepository.findById(ID)).thenReturn(Optional.of(draftBook()));

        assertThatThrownBy(() -> bookService.updateStatus(ID, statusRequest(BookStatus.DRAFT)))
                .isInstanceOf(InvalidBookStatusTransitionException.class)
                .hasMessageContaining("DRAFT");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateStatus_sameStatusArchived_throwsInvalidBookStatusTransitionException() {
        Book archivedBook = new Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.ARCHIVED);
        when(bookRepository.findById(ID)).thenReturn(Optional.of(archivedBook));

        assertThatThrownBy(() -> bookService.updateStatus(ID, statusRequest(BookStatus.ARCHIVED)))
                .isInstanceOf(InvalidBookStatusTransitionException.class)
                .hasMessageContaining("ARCHIVED");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateStatus_draftToArchived_throwsInvalidBookStatusTransitionException() {
        when(bookRepository.findById(ID)).thenReturn(Optional.of(draftBook()));

        assertThatThrownBy(() -> bookService.updateStatus(ID, statusRequest(BookStatus.ARCHIVED)))
                .isInstanceOf(InvalidBookStatusTransitionException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("ARCHIVED");

        verify(bookRepository, never()).save(any());
    }

    // --- calculatePrice boundary ---

    @Test
    void calculatePrice_emptyStrategyName_throwsIllegalArgumentException() {
        when(bookRepository.findById(ID)).thenReturn(Optional.of(draftBook()));

        assertThatThrownBy(() -> bookService.calculatePrice(ID, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void calculatePrice_nullStrategyName_throwsNullPointerException() {
        when(bookRepository.findById(ID)).thenReturn(Optional.of(draftBook()));

        assertThatThrownBy(() -> bookService.calculatePrice(ID, null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- duplicate case-insensitive ---

    @Test
    void create_duplicateTitleDifferentCase_throwsDuplicateBookException() {
        when(bookRepository.existsByTitleAndAuthor("clean code", "robert martin")).thenReturn(true);

        CreateBookRequest request = new CreateBookRequest("clean code", "robert martin", "Programming", 2008, 29.99, 431);

        assertThatThrownBy(() -> bookService.create(request))
                .isInstanceOf(DuplicateBookException.class);

        verify(bookRepository, never()).save(any());
    }
}