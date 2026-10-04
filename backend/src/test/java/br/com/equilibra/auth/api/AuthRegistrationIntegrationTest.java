package br.com.equilibra.auth.api;

import br.com.equilibra.shared.web.filter.RequestIdFilter;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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
class AuthRegistrationIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters(new RequestIdFilter(), springSecurityFilterChain)
            .build();
    }

    @Test
    void shouldRegisterUserAndPersistNormalizedEmailAndPasswordHash() throws Exception {
        String rawPassword = " minha senha segura ";

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": " Integration.Valid@Example.com ",
                      "password": " minha senha segura "
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.email").value("integration.valid@example.com"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(content().string(not(containsString("password"))))
            .andExpect(content().string(not(containsString("passwordHash"))))
            .andExpect(content().string(not(containsString(rawPassword))));

        User persisted = userRepository.findByEmail("integration.valid@example.com").orElseThrow();
        assertThat(persisted.getEmail()).isEqualTo("integration.valid@example.com");
        assertThat(persisted.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, persisted.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("minha senha segura", persisted.getPasswordHash())).isFalse();
    }

    @Test
    void shouldReturnConflictForDuplicateEmailWithDifferentCasing() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "Integration.Case@Example.com",
                      "password": "senhaValida123"
                    }
                    """))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "integration.case@example.com",
                      "password": "outraSenha123"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Resource conflict"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("An account with this email already exists."));
    }

    @Test
    void shouldReturnConflictForDuplicateEmailWithSpaces() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "integration.spaces@example.com",
                      "password": "senhaValida123"
                    }
                    """))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": " integration.spaces@example.com ",
                      "password": "outraSenha123"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void shouldKeepDatabaseUniqueConstraintEffective() {
        userRepository.saveAndFlush(new User("integration.unique@example.com", "hash-1"));

        try {
            userRepository.saveAndFlush(new User(" Integration.Unique@Example.com ", "hash-2"));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            assertThat(ex.getMessage()).doesNotContain("hash-2");
            return;
        }

        throw new AssertionError("Expected database unique constraint violation");
    }
}
