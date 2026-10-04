package br.com.equilibra.user.api;

import br.com.equilibra.auth.application.PasswordService;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import br.com.equilibra.shared.web.filter.RequestIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class GetCurrentUserIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private org.springframework.security.web.FilterChainProxy springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private br.com.equilibra.auth.application.JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters(new RequestIdFilter(), springSecurityFilterChain)
            .build();
    }

    @Test
    void shouldReturnUnauthorizedWhenNoToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void shouldReturnCurrentUserWhenTokenIsValid() throws Exception {
        User user = userRepository.saveAndFlush(new User(
            "current.user@example.com",
            passwordService.encode("senhaValida123")
        ));

        String token = login("current.user@example.com", "senhaValida123");

        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
            .andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.email").value("current.user@example.com"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldNotMixUsersAAndB() throws Exception {
        User userA = userRepository.saveAndFlush(new User(
            "user.a@example.com",
            passwordService.encode("senhaValida123")
        ));
        User userB = userRepository.saveAndFlush(new User(
            "user.b@example.com",
            passwordService.encode("senhaValida123")
        ));

        String tokenA = login("user.a@example.com", "senhaValida123");
        String tokenB = login("user.b@example.com", "senhaValida123");

        // User A calls /me with token A
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userA.getId()))
            .andExpect(jsonPath("$.email").value("user.a@example.com"));

        // User B calls /me with token B
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userB.getId()))
            .andExpect(jsonPath("$.email").value("user.b@example.com"));

        // Cross-check: token A still returns A, token B still returns B
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userA.getId()));

        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userB.getId()));
    }

    @Test
    void shouldIgnoreXUserIdHeaderWhenPresent() throws Exception {
        User userA = userRepository.saveAndFlush(new User(
            "user.a@example.com",
            passwordService.encode("senhaValida123")
        ));
        User userB = userRepository.saveAndFlush(new User(
            "user.b@example.com",
            passwordService.encode("senhaValida123")
        ));

        String tokenA = login("user.a@example.com", "senhaValida123");

        // Try to spoof with X-User-ID header containing userB's ID
        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                .header("X-User-ID", userB.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userA.getId()))
            .andExpect(jsonPath("$.email").value("user.a@example.com"));
    }

    @Test
    void shouldIgnoreUserIdQueryParameterWhenPresent() throws Exception {
        User userA = userRepository.saveAndFlush(new User(
            "user.a@example.com",
            passwordService.encode("senhaValida123")
        ));
        User userB = userRepository.saveAndFlush(new User(
            "user.b@example.com",
            passwordService.encode("senhaValida123")
        ));

        String tokenA = login("user.a@example.com", "senhaValida123");

        // Try to spoof with ?userId query parameter containing userB's ID
        mockMvc.perform(get("/api/users/me?userId=" + userB.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userA.getId()))
            .andExpect(jsonPath("$.email").value("user.a@example.com"));
    }

    @Test
    void shouldReturnNotFoundWhenAuthenticatedUserDoesNotExist() throws Exception {
        // Create a token for a non-existent user
        String nonExistentUserId = "123e4567-e89b-12d3-a456-426614174999";
        String token = jwtTokenService.issueAccessToken(
            new br.com.equilibra.auth.application.AuthenticatedUser(nonExistentUserId, "nonexistent@example.com")
        ).accessToken();

        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Resource not found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.requestId").exists());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(email, password)))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900))
            .andReturn()
            .getResponse()
            .getContentAsString();

        com.fasterxml.jackson.databind.JsonNode json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
        return json.get("accessToken").asText();
    }
}