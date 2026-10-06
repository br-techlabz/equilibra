package br.com.equilibra.account.api;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.domain.AssetAccountType;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.auth.application.JwtTokenService;
import br.com.equilibra.auth.application.LoginTokenResponse;
import br.com.equilibra.auth.application.PasswordService;
import br.com.equilibra.shared.web.filter.RequestIdFilter;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AssetAccountControllerIntegrationTest {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private FilterChainProxy springSecurityFilterChain;
    @Autowired private UserRepository users;
    @Autowired private AssetAccountRepository accounts;
    @Autowired private PasswordService passwordService;
    @Autowired private JwtTokenService jwtTokenService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters(new RequestIdFilter(), springSecurityFilterChain)
            .build();
    }

    @Test
    void shouldIsolateListAndAllMutationsByOwner() throws Exception {
        User owner = user("account.gate.owner");
        User other = user("account.gate.other");
        String ownerToken = bearer(login(owner));
        String otherToken = bearer(login(other));
        AssetAccount active = account(owner, "Conta A", true);
        AssetAccount inactive = account(owner, "Conta inativa A", false);
        account(other, "Conta B", true);

        mockMvc.perform(get("/asset-accounts").header(HttpHeaders.AUTHORIZATION, ownerToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
            .andExpect(content().string(containsString("Conta A")))
            .andExpect(content().string(not(containsString("Conta B"))));
        mockMvc.perform(get("/asset-accounts?includeInactive=true").header(HttpHeaders.AUTHORIZATION, ownerToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));

        String path = "/asset-accounts/" + active.getId();
        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, otherToken)).andExpect(status().isNotFound());
        mockMvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Invasiva\",\"type\":\"CASH\",\"initialBalance\":999}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(patch(path + "/deactivate").header(HttpHeaders.AUTHORIZATION, otherToken)).andExpect(status().isNotFound());
        mockMvc.perform(patch("/asset-accounts/" + inactive.getId() + "/activate")
                .header(HttpHeaders.AUTHORIZATION, otherToken)).andExpect(status().isNotFound());

        assertThat(accounts.findByIdAndOwnerId(active.getId(), owner.getId()).orElseThrow().getName()).isEqualTo("Conta A");
        assertThat(accounts.findByIdAndOwnerId(active.getId(), owner.getId()).orElseThrow().isActive()).isTrue();
        assertThat(accounts.findByIdAndOwnerId(inactive.getId(), owner.getId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    void shouldIgnoreOwnerSpoofingAndKeepResponsePrivate() throws Exception {
        User owner = user("account.gate.spoof");
        String token = bearer(login(owner));
        String spoofedOwner = UUID.randomUUID().toString();
        String response = mockMvc.perform(post("/asset-accounts")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Carteira segura",
                      "type": "CASH",
                      "initialBalance": 10.10,
                      "ownerId": "%s",
                      "active": false,
                      "normalizedName": "alterada"
                    }
                    """.formatted(spoofedOwner)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ownerId").doesNotExist())
            .andExpect(jsonPath("$.normalizedName").doesNotExist())
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asText();
        assertThat(accounts.findByIdAndOwnerId(id, owner.getId())).isPresent();
        assertThat(accounts.findByIdAndOwnerId(id, spoofedOwner)).isEmpty();
    }

    @Test
    void shouldRequireAuthenticationAndRejectMalformedId() throws Exception {
        mockMvc.perform(get("/asset-accounts")).andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        String token = bearer(login(user("account.gate.invalid")));
        mockMvc.perform(get("/asset-accounts/not-a-uuid").header(HttpHeaders.AUTHORIZATION, token))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.requestId").exists());
    }

    private User user(String prefix) {
        return users.saveAndFlush(new User(prefix + "." + UUID.randomUUID() + "@example.test", passwordService.encode("senhaValida123")));
    }

    private AssetAccount account(User owner, String name, boolean active) {
        AssetAccount account = new AssetAccount(owner.getId(), name, AssetAccountType.CASH, new BigDecimal("100.00"));
        if (!active) account.deactivate();
        return accounts.saveAndFlush(account);
    }

    private LoginTokenResponse login(User user) {
        return jwtTokenService.issueAccessToken(new br.com.equilibra.auth.application.AuthenticatedUser(user.getId(), user.getEmail()));
    }

    private String bearer(LoginTokenResponse token) {
        return token.tokenType() + " " + token.accessToken();
    }
}
