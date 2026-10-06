package br.com.equilibra.tag.api;

import br.com.equilibra.auth.application.AuthenticatedUser;
import br.com.equilibra.auth.application.JwtTokenService;
import br.com.equilibra.auth.application.LoginTokenResponse;
import br.com.equilibra.auth.application.PasswordService;
import br.com.equilibra.shared.web.filter.RequestIdFilter;
import br.com.equilibra.tag.domain.Tag;
import br.com.equilibra.tag.infrastructure.TagRepository;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
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
import java.util.UUID;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TagControllerIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired private FilterChainProxy security;
    @Autowired private UserRepository users;
    @Autowired private TagRepository tags;
    @Autowired private PasswordService passwords;
    @Autowired private JwtTokenService tokens;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach void setUp() { mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new RequestIdFilter(), security).build(); }

    @Test void shouldCreateListUpdateDeactivateAndReactivate() throws Exception {
        User user = user("tag.api.crud"); String token = bearer(user);
        String body = "{\"name\":\" Viagem São Paulo \"}";
        String response = mvc.perform(post("/tags").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andExpect(header().exists(HttpHeaders.LOCATION)).andExpect(jsonPath("$.name").value("Viagem São Paulo")).andExpect(jsonPath("$.active").value(true)).andReturn().getResponse().getContentAsString();
        String id = mapper.readTree(response).get("id").asText();
        mvc.perform(get("/tags").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(put("/tags/" + id).header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Trabalho\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Trabalho"));
        mvc.perform(patch("/tags/" + id + "/deactivate").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mvc.perform(get("/tags").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/tags?includeInactive=true").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(patch("/tags/" + id + "/activate").header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));
    }

    @Test void shouldProtectOwnershipAndUniqueness() throws Exception {
        User owner = user("tag.api.owner"); User other = user("tag.api.other");
        String ownerToken = bearer(owner); String otherToken = bearer(other);
        Tag tag = tags.saveAndFlush(new Tag(owner.getId(), "Viagem"));
        mvc.perform(post("/tags").header(HttpHeaders.AUTHORIZATION, ownerToken).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" VIAGEM \"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.requestId").exists());
        mvc.perform(get("/tags/" + tag.getId()).header(HttpHeaders.AUTHORIZATION, otherToken)).andExpect(status().isNotFound());
        mvc.perform(put("/tags/" + tag.getId()).header(HttpHeaders.AUTHORIZATION, otherToken).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Invasiva\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(patch("/tags/" + tag.getId() + "/deactivate").header(HttpHeaders.AUTHORIZATION, otherToken)).andExpect(status().isNotFound());
        mvc.perform(get("/tags")).andExpect(status().isUnauthorized());
        mvc.perform(post("/tags").header(HttpHeaders.AUTHORIZATION, otherToken).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" VIAGEM \"}"))
            .andExpect(status().isCreated());
    }

    @Test void shouldRejectInvalidInputAndIgnoreProtectedFields() throws Exception {
        String token = bearer(user("tag.api.validation"));
        mvc.perform(post("/tags").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \"}"))
            .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(post("/tags").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Segura\",\"active\":false,\"ownerId\":\"" + UUID.randomUUID() + "\",\"normalizedName\":\"invasiva\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.active").value(true)).andExpect(jsonPath("$.ownerId").doesNotExist()).andExpect(jsonPath("$.normalizedName").doesNotExist());
    }

    private User user(String prefix) { return users.saveAndFlush(new User(prefix + "." + UUID.randomUUID() + "@example.test", passwords.encode("senhaValida123"))); }
    private String bearer(User user) { LoginTokenResponse token = tokens.issueAccessToken(new AuthenticatedUser(user.getId(), user.getEmail())); return token.tokenType() + " " + token.accessToken(); }
}
