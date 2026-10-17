package vfrolenko.pr2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vfrolenko.pr2.config.SecurityConfiguration;
import vfrolenko.pr2.entity.Book;
import vfrolenko.pr2.entity.BookStatus;
import vfrolenko.pr2.service.BookService;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookWebController.class)
@Import(SecurityConfiguration.class)
class BookWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private DataSource dataSource;

    @Test
    void listBooks_authenticatedUser_returnsListView() throws Exception {
        when(bookService.findAll()).thenReturn(List.of(
                new Book(UUID.randomUUID(), "Title", "Author", "Genre", 2020, 10.0, 100, BookStatus.DRAFT)
        ));

        mockMvc.perform(get("/books").with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books"));
    }

    @Test
    void showCreateForm_admin_returnsFormView() throws Exception {
        mockMvc.perform(get("/books/new").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("books/create"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    void showCreateForm_user_returnsForbidden() throws Exception {
        mockMvc.perform(get("/books/new").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBook_validData_redirectsToBooks() throws Exception {
        mockMvc.perform(post("/books")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("title", "New Book")
                        .param("author", "Author")
                        .param("genre", "Genre")
                        .param("publicationYear", "2020")
                        .param("price", "10.0")
                        .param("pages", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void createBook_invalidData_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/books")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("title", "")
                        .param("author", "Author")
                        .param("genre", "Genre")
                        .param("publicationYear", "2020")
                        .param("price", "10.0")
                        .param("pages", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/create"))
                .andExpect(model().attributeHasFieldErrors("book", "title"));
    }

    @Test
    void createBook_withoutCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/books")
                        .with(user("admin").roles("ADMIN"))
                        .param("title", "New Book")
                        .param("author", "Author")
                        .param("genre", "Genre")
                        .param("publicationYear", "2020")
                        .param("price", "10.0")
                        .param("pages", "100"))
                .andExpect(status().isForbidden());
    }
}