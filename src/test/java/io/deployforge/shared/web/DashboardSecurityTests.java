package io.deployforge.shared.web;

import io.deployforge.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DashboardController.class, properties = {
        "deployforge.bootstrap.username=test-admin",
        "deployforge.bootstrap.password=test-password-for-local-tests"
})
@Import(SecurityConfiguration.class)
class DashboardSecurityTests {

    @Autowired
    MockMvc mvc;

    @Test
    void anonymousDashboardRequestRedirectsToLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginFormContainsCsrfToken() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("_csrf")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboardShowsUnimplementedFeaturesHonestly() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Henüz GitHub bağlantısı kurulmadı")));
    }

    @Test
    @WithMockUser
    void logoutWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/logout")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void logoutWithCsrfSucceeds() throws Exception {
        mvc.perform(post("/logout").with(csrf())).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void configuredCredentialsAllowLogin() throws Exception {
        mvc.perform(post("/login").with(csrf())
                        .param("username", "test-admin")
                        .param("password", "test-password-for-local-tests"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }

    @Test
    void invalidCredentialsAreRejected() throws Exception {
        mvc.perform(post("/login").with(csrf())
                        .param("username", "test-admin").param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?error"));
    }
}
