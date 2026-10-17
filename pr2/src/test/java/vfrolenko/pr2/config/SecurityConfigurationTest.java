package vfrolenko.pr2.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void accessProtectedWebPage_withoutAuthentication_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    void loginWithDbAdmin_validCredentials_authenticates() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("password"))
                .andExpect(authenticated().withRoles("ADMIN", "USER"));
    }

    @Test
    void loginWithDbUser_validCredentials_authenticates() throws Exception {
        mockMvc.perform(formLogin().user("user").password("password"))
                .andExpect(authenticated().withRoles("USER"));
    }

    @Test
    void loginWithInvalidCredentials_isUnauthenticated() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("wrong"))
                .andExpect(unauthenticated());
    }

    @Test
    void accessAdminPageAsUser_isForbidden() throws Exception {
        mockMvc.perform(get("/books/new").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessAdminPageAsAdmin_isOk() throws Exception {
        mockMvc.perform(get("/books/new").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}