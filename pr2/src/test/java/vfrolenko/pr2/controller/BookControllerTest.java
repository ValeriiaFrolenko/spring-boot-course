package vfrolenko.pr2.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;
import vfrolenko.pr2.dto.book.CreateBookRequest;
import vfrolenko.pr2.dto.book.UpdateBookRequest;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/books";

    private CreateBookRequest validRequest() {
        return new CreateBookRequest("Clean Code", "Robert Martin", "Programming", 2008, 29.99, 431);
    }

    private String createBookAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        return location.substring(location.lastIndexOf("/") + 1);
    }

    @Test
    void getAll_returnsOk() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    void create_validRequest_returns201WithLocation() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated());
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
        String id = createBookAndGetId();

        mockMvc.perform(get(BASE_URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void getById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_existingId_returnsUpdatedBook() throws Exception {
        String id = createBookAndGetId();

        UpdateBookRequest updateRequest = new UpdateBookRequest("Updated Title", "New Author", "Fiction", 2020, 19.99, 300);

        mockMvc.perform(put(BASE_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void update_nonExistingId_returns404() throws Exception {
        UpdateBookRequest updateRequest = new UpdateBookRequest("Updated Title", "New Author", "Fiction", 2020, 19.99, 300);

        mockMvc.perform(put(BASE_URL + "/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        String id = createBookAndGetId();

        mockMvc.perform(delete(BASE_URL + "/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonExistingId_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}