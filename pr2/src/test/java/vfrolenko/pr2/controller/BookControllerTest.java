package vfrolenko.pr2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import vfrolenko.pr2.dto.book.BookResponse;
import vfrolenko.pr2.dto.book.CreateBookRequest;
import vfrolenko.pr2.dto.book.UpdateBookRequest;
import vfrolenko.pr2.entity.BookStatus;
import vfrolenko.pr2.exception.ResourceNotFoundException;
import vfrolenko.pr2.service.BookService;
import vfrolenko.pr2.dto.book.UpdateBookStatusRequest;
import vfrolenko.pr2.exception.InvalidBookStatusTransitionException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private static final String BASE_URL = "/api/v1/books";
    private static final UUID ID = UUID.randomUUID();

    private BookResponse bookResponse() {
        return new BookResponse(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.DRAFT);
    }

    private CreateBookRequest validCreateRequest() {
        return new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431);
    }

    private UpdateBookRequest validUpdateRequest() {
        return new UpdateBookRequest("Updated Title", "New Author", "Fiction", 2020, 19.99, 300);
    }

    @Test
    void getAll_returnsOk() throws Exception {
        when(bookService.findAll()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    void create_validRequest_returns201WithLocation() throws Exception {
        when(bookService.create(any())).thenReturn(
                new vfrolenko.pr2.entity.Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.DRAFT));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void create_blankTitle_returns400WithErrors() throws Exception {
        CreateBookRequest request = new CreateBookRequest("", "Robert Martin", "Programming", 2008, 29.99, 431);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void create_negativePrice_returns400WithErrors() throws Exception {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 2008, -1.0, 431);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void create_unknownField_returns400() throws Exception {
        String json = """
                {
                    "title": "Clean Code",
                    "author": "Robert Martin",
                    "genre": "Programming",
                    "publicationYear": 2008,
                    "price": 29.99,
                    "pages": 431,
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_existingId_returnsBook() throws Exception {
        when(bookService.findById(ID)).thenReturn(Optional.of(
                new vfrolenko.pr2.entity.Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.DRAFT)));

        mockMvc.perform(get(BASE_URL + "/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void getById_nonExistingId_returns404() throws Exception {
        when(bookService.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_existingId_returnsUpdatedBook() throws Exception {
        when(bookService.update(eq(ID), any())).thenReturn(
                new vfrolenko.pr2.entity.Book(ID, "Updated Title", "New Author", "Fiction", 2020, 19.99, 300, BookStatus.DRAFT));

        UpdateBookRequest updateRequest = new UpdateBookRequest("Updated Title", "New Author", "Fiction", 2020, 19.99, 300);

        mockMvc.perform(put(BASE_URL + "/" + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void update_nonExistingId_returns404() throws Exception {
        when(bookService.update(any(), any())).thenThrow(new ResourceNotFoundException("not found"));

        UpdateBookRequest updateRequest = new UpdateBookRequest("Updated Title", "New Author", "Fiction", 2020, 19.99, 300);

        mockMvc.perform(put(BASE_URL + "/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_validTransition_returnsUpdatedBook() throws Exception {
        when(bookService.updateStatus(eq(ID), any())).thenReturn(
                new vfrolenko.pr2.entity.Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.PUBLISHED));

        UpdateBookStatusRequest request = new UpdateBookStatusRequest(BookStatus.PUBLISHED);

        mockMvc.perform(patch(BASE_URL + "/" + ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void updateStatus_invalidTransition_returns422() throws Exception {
        when(bookService.updateStatus(any(), any())).thenThrow(
                new InvalidBookStatusTransitionException(BookStatus.PUBLISHED, BookStatus.DRAFT));

        UpdateBookStatusRequest request = new UpdateBookStatusRequest(BookStatus.DRAFT);

        mockMvc.perform(patch(BASE_URL + "/" + ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void updateStatus_nonExistingId_returns404() throws Exception {
        when(bookService.updateStatus(any(), any())).thenThrow(new ResourceNotFoundException("not found"));

        UpdateBookStatusRequest request = new UpdateBookStatusRequest(BookStatus.PUBLISHED);

        mockMvc.perform(patch(BASE_URL + "/" + UUID.randomUUID() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonExistingId_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(bookService).delete(any());

        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
    // --- publicationYear boundary ---

    @Test
    void create_publicationYearTooEarly_returns400() throws Exception {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 1449, 29.99, 431);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.publicationYear").exists());
    }

    @Test
    void create_publicationYearTooLate_returns400() throws Exception {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 2101, 29.99, 431);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.publicationYear").exists());
    }

    @Test
    void create_publicationYearMinBoundary_returns201() throws Exception {
        when(bookService.create(any())).thenReturn(
                new vfrolenko.pr2.entity.Book(ID, "Clean Code", "Robert Martin", "Programming", 1450, 29.99, 431, BookStatus.DRAFT));

        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 1450, 29.99, 431);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_publicationYearMaxBoundary_returns201() throws Exception {
        when(bookService.create(any())).thenReturn(
                new vfrolenko.pr2.entity.Book(ID, "Clean Code", "Robert Martin", "Programming", 2100, 29.99, 431, BookStatus.DRAFT));

        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 2100, 29.99, 431);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // --- pages boundary ---

    @Test
    void create_pagesTooFew_returns400() throws Exception {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 2008, 29.99, 0);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.pages").exists());
    }

    @Test
    void create_pagesTooMany_returns400() throws Exception {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 2008, 29.99, 10001);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.pages").exists());
    }

    @Test
    void create_pagesMinBoundary_returns201() throws Exception {
        when(bookService.create(any())).thenReturn(
                new vfrolenko.pr2.entity.Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 1, BookStatus.DRAFT));

        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 2008, 29.99, 1);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_pagesMaxBoundary_returns201() throws Exception {
        when(bookService.create(any())).thenReturn(
                new vfrolenko.pr2.entity.Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 10000, BookStatus.DRAFT));

        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 2008, 29.99, 10000);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // --- searchByName ---

    @Test
    void searchByName_matchFound_returnsOkWithList() throws Exception {
        when(bookService.searchByName("Clean")).thenReturn(List.of(
                new vfrolenko.pr2.entity.Book(ID, "Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431, BookStatus.DRAFT)));

        mockMvc.perform(get(BASE_URL + "/search").param("name", "Clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void searchByName_noMatch_returnsOkWithEmptyList() throws Exception {
        when(bookService.searchByName("XYZ")).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/search").param("name", "XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}