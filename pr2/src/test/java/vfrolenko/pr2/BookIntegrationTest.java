package vfrolenko.pr2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;
import vfrolenko.pr2.dto.book.CreateBookRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@WithMockUser(username = "testuser")
class BookIntegrationTest {

    private static final String BASE_URL = "/api/v1/books";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void create_validRequest_persistsRowInDatabase() throws Exception {
        CreateBookRequest request = new CreateBookRequest(
                "Effective Java", "Joshua Bloch", "Programming", 2018, 44.99, 412);

        MvcResult result = mockMvc.perform(post(BASE_URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        String createdId = location.substring(location.lastIndexOf('/') + 1);

        var row = jdbcClient
                .sql("SELECT title, author, genre, publication_year, price, pages, status FROM books WHERE id = ?")
                .param(java.util.UUID.fromString(createdId))
                .query((rs, _) -> new Object[]{
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getInt("publication_year"),
                        rs.getDouble("price"),
                        rs.getInt("pages"),
                        rs.getString("status")
                })
                .single();

        assertThat(row[0]).isEqualTo("Effective Java");
        assertThat(row[1]).isEqualTo("Joshua Bloch");
        assertThat(row[2]).isEqualTo("Programming");
        assertThat(row[3]).isEqualTo(2018);
        assertThat(row[4]).isEqualTo(44.99);
        assertThat(row[5]).isEqualTo(412);
        assertThat(row[6]).isEqualTo("DRAFT");
    }

    @Test
    void create_invalidRequest_returns400AndDoesNotPersist() throws Exception {
        long countBefore = jdbcClient.sql("SELECT COUNT(*) FROM books").query(Long.class).single();

        CreateBookRequest invalidRequest = new CreateBookRequest(
                "", "Joshua Bloch", "Programming", 2018, 44.99, 412);

        mockMvc.perform(post(BASE_URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        long countAfter = jdbcClient.sql("SELECT COUNT(*) FROM books").query(Long.class).single();
        assertThat(countAfter).isEqualTo(countBefore);
    }
}