package br.com.equilibra.auth.api;

import br.com.equilibra.auth.application.JwtTokenService;
import br.com.equilibra.auth.application.LoginTokenResponse;
import br.com.equilibra.auth.application.PasswordService;
import br.com.equilibra.auth.infrastructure.security.AuthenticatedPrincipal;
import br.com.equilibra.shared.web.filter.RequestIdFilter;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
@Import(JwtProtectedEndpointIntegrationTest.ProtectedTestController.class)
class JwtProtectedEndpointIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters(new RequestIdFilter(), springSecurityFilterChain)
            .build();
    }

    @Test
    void shouldReturnUnauthorizedForMissingToken() throws Exception {
        mockMvc.perform(get("/test/protected/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void shouldAccessProtectedEndpointWithValidTokenAndExposePrincipal() throws Exception {
        User user = userRepository.saveAndFlush(new User(
            "jwt.valid@example.com",
            passwordService.encode("senhaValida123")
        ));

        LoginTokenResponse login = login("jwt.valid@example.com", "senhaValida123");

        mockMvc.perform(get("/test/protected/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken()))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
            .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    void shouldReturnUnauthorizedForMalformedToken() throws Exception {
        mockMvc.perform(get("/test/protected/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(content().string(not(containsString("Base64"))));
    }

    @Test
    void shouldReturnUnauthorizedForTamperedToken() throws Exception {
        LoginTokenResponse token = jwtTokenService.issueAccessToken(
            new br.com.equilibra.auth.application.AuthenticatedUser("123e4567-e89b-12d3-a456-426614174000", "jwt.tampered@example.com")
        );
        String tampered = token.accessToken().substring(0, token.accessToken().length() - 2) + "xx";

        mockMvc.perform(get("/test/protected/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldAuthenticateRegisterLoginAndUseTokenWithoutSessionDependency() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": " jwt.flow@example.com ",
                      "password": "senhaValida123"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        LoginTokenResponse login = login("JWT.FLOW@EXAMPLE.COM", "senhaValida123");

        mockMvc.perform(get("/test/protected/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken()))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
            .andExpect(jsonPath("$.userId").isNotEmpty());
    }

    private LoginTokenResponse login(String email, String password) throws Exception {
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
        return new LoginTokenResponse(json.get("accessToken").asText(), json.get("tokenType").asText(), json.get("expiresIn").asLong());
    }

    @RestController
    @RequestMapping("/test/protected")
    static class ProtectedTestController {

        @GetMapping("/me")
        java.util.Map<String, String> me(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
            assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
            return java.util.Map.of("userId", principal.userId());
        }
    }
}
