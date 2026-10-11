package vfrolenko.pr2.config;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc runs against a mocked servlet container, so HTTP response headers never contain
 * a real "Set-Cookie" for the session (that is produced by the actual container, e.g. Tomcat,
 * not by DispatcherServlet). The session-cookie requirement from the assignment is therefore
 * verified at the equivalent MockMvc level: the {@link MockHttpSession} created for the
 * authenticated request, identified by {@code server.servlet.session.cookie.name}'s underlying
 * session id, and reused across requests exactly as a browser would reuse the cookie.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTest {

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void accessProtectedEndpoint_withoutAuthentication_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    void login_withValidCredentials_authenticatesAndCreatesSession() throws Exception {
        MvcResult result = mockMvc.perform(formLogin().user(USERNAME).password(PASSWORD))
                .andExpect(authenticated())
                .andReturn();

        HttpSession session = result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotBlank();
    }

    @Test
    void login_withInvalidCredentials_isUnauthenticated() throws Exception {
        mockMvc.perform(formLogin().user(USERNAME).password("wrong-password"))
                .andExpect(unauthenticated());
    }

    @Test
    void accessProtectedEndpoint_withSessionFromLogin_doesNotRequireReLogin() throws Exception {
        MvcResult loginResult = mockMvc.perform(formLogin().user(USERNAME).password(PASSWORD))
                .andExpect(authenticated())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/ping").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void logout_invalidatesSessionAndRevokesAccess() throws Exception {
        MvcResult loginResult = mockMvc.perform(formLogin().user(USERNAME).password(PASSWORD))
                .andExpect(authenticated())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(post("/logout").session(session))
                .andExpect(unauthenticated());

        assertThat(session.isInvalid()).isTrue();

        mockMvc.perform(get("/api/ping").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
    }
}