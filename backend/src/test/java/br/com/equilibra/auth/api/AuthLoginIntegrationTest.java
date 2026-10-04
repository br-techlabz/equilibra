package br.com.equilibra.auth.api;

import br.com.equilibra.auth.application.PasswordService;
import br.com.equilibra.shared.web.filter.RequestIdFilter;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AuthLoginIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters(new RequestIdFilter(), springSecurityFilterChain)
            .build();
    }

    @Test
    void shouldAuthenticateWithValidCredentials() throws Exception {
        userRepository.saveAndFlush(new User("login.valid@example.com", passwordService.encode("senhaValida123")));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login.valid@example.com",
                      "password": "senhaValida123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.email").value("login.valid@example.com"))
            .andExpect(content().string(not(containsString("password"))))
            .andExpect(content().string(not(containsString("passwordHash"))))
            .andExpect(content().string(not(containsString("token"))))
            .andExpect(content().string(not(containsString("senhaValida123"))));
    }

    @Test
    void shouldReturnUnauthorizedForIncorrectPassword() throws Exception {
        userRepository.saveAndFlush(new User("login.wrong-password@example.com", passwordService.encode("senhaValida123")));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login.wrong-password@example.com",
                      "password": "senhaErrada123"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Authentication failed"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value("Invalid email or password."))
            .andExpect(jsonPath("$.requestId").exists())
            .andExpect(content().string(not(containsString("login.wrong-password@example.com"))))
            .andExpect(content().string(not(containsString("senhaErrada123"))))
            .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    void shouldReturnUnauthorizedForMissingEmail() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login.missing@example.com",
                      "password": "senhaValida123"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Authentication failed"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value("Invalid email or password."))
            .andExpect(jsonPath("$.requestId").exists())
            .andExpect(content().string(not(containsString("login.missing@example.com"))))
            .andExpect(content().string(not(containsString("senhaValida123"))))
            .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    void shouldReturnUnauthorizedForInactiveUser() throws Exception {
        User inactiveUser = new User("login.inactive@example.com", passwordService.encode("senhaValida123"));
        inactiveUser.setActive(false);
        userRepository.saveAndFlush(inactiveUser);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login.inactive@example.com",
                      "password": "senhaValida123"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Authentication failed"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value("Invalid email or password."))
            .andExpect(content().string(not(containsString("inactive"))))
            .andExpect(content().string(not(containsString("senhaValida123"))))
            .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    void shouldAuthenticateWithEmailCasingAndSpacesNormalized() throws Exception {
        userRepository.saveAndFlush(new User("login.normalized@example.com", passwordService.encode("senhaValida123")));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": " LOGIN.NORMALIZED@EXAMPLE.COM ",
                      "password": "senhaValida123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("login.normalized@example.com"));
    }

    @Test
    void shouldRespectExactPasswordWithoutTrim() throws Exception {
        String rawPassword = " senha com espacos ";
        userRepository.saveAndFlush(new User("login.exact-password@example.com", passwordService.encode(rawPassword)));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login.exact-password@example.com",
                      "password": " senha com espacos "
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login.exact-password@example.com",
                      "password": "senha com espacos"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Invalid email or password."));
    }

    @Test
    void shouldReturnEquivalentExternalResponseForMissingEmailAndWrongPassword() throws Exception {
        userRepository.saveAndFlush(new User("login.enumeration@example.com", passwordService.encode("senhaValida123")));

        MvcResult wrongPassword = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login.enumeration@example.com",
                      "password": "senhaErrada123"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andReturn();

        MvcResult missingEmail = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login.enumeration-missing@example.com",
                      "password": "senhaErrada123"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andReturn();

        assertThat(wrongPassword.getResponse().getContentType()).isEqualTo(missingEmail.getResponse().getContentType());
        assertThat(wrongPassword.getResponse().getStatus()).isEqualTo(missingEmail.getResponse().getStatus());
        assertThat(wrongPassword.getResponse().getContentAsString()).contains("Authentication failed", "Invalid email or password.");
        assertThat(missingEmail.getResponse().getContentAsString()).contains("Authentication failed", "Invalid email or password.");
        assertThat(wrongPassword.getResponse().getContentAsString()).doesNotContain("login.enumeration@example.com");
        assertThat(missingEmail.getResponse().getContentAsString()).doesNotContain("login.enumeration-missing@example.com");
    }
}
