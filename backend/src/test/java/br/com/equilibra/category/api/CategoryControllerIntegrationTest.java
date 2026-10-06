package br.com.equilibra.category.api;

import br.com.equilibra.auth.application.JwtTokenService;
import br.com.equilibra.auth.application.LoginTokenResponse;
import br.com.equilibra.auth.application.PasswordService;
import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.domain.CategoryApplicability;
import br.com.equilibra.category.infrastructure.CategoryRepository;
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
class CategoryControllerIntegrationTest {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private FilterChainProxy springSecurityFilterChain;
    @Autowired private UserRepository users;
    @Autowired private CategoryRepository categories;
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
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/categories"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void shouldCreateCategoryWithoutAcceptingOwnerSpoofing() throws Exception {
        User owner = user("category.create");
        LoginTokenResponse token = login(owner, "senhaValida123");
        String spoofedOwner = UUID.randomUUID().toString();

        String response = mockMvc.perform(post("/categories")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": " Alimentação ",
                      "applicability": "EXPENSE",
                      "ownerId": "%s"
                    }
                    """.formatted(spoofedOwner)))
            .andExpect(status().isCreated())
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.name").value("Alimentação"))
            .andExpect(jsonPath("$.applicability").value("EXPENSE"))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.ownerId").doesNotExist())
            .andExpect(jsonPath("$.normalizedName").doesNotExist())
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(response).get("id").asText();
        assertThat(categories.findByIdAndOwnerId(id, owner.getId())).isPresent();
        assertThat(categories.findByIdAndOwnerId(id, spoofedOwner)).isEmpty();
    }

    @Test
    void shouldValidateBodyAndEnum() throws Exception {
        LoginTokenResponse token = login(user("category.invalid"), "senhaValida123");
        mockMvc.perform(post("/categories")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \",\"applicability\":\"WHATEVER\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void shouldListOnlyOwnedActiveAndSupportInactiveAndContextFilters() throws Exception {
        User owner = user("category.list.owner");
        User other = user("category.list.other");
        LoginTokenResponse token = login(owner, "senhaValida123");
        category(owner, "Alimentação", CategoryApplicability.EXPENSE, true);
        category(owner, "Outros", CategoryApplicability.BOTH, true);
        category(owner, "Salário", CategoryApplicability.INCOME, true);
        category(owner, "Arquivada", CategoryApplicability.BOTH, false);
        category(other, "De outra pessoa", CategoryApplicability.BOTH, true);

        mockMvc.perform(get("/categories").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)))
            .andExpect(content().string(not(containsString("Arquivada"))))
            .andExpect(content().string(not(containsString("De outra pessoa"))));
        mockMvc.perform(get("/categories?includeInactive=true&applicability=EXPENSE")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)))
            .andExpect(content().string(containsString("Alimentação")))
            .andExpect(content().string(containsString("Outros")))
            .andExpect(content().string(containsString("Arquivada")))
            .andExpect(content().string(not(containsString("Salário"))));
        mockMvc.perform(get("/categories?applicability=INCOME")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
            .andExpect(content().string(containsString("Salário")))
            .andExpect(content().string(containsString("Outros")));
    }

    @Test
    void shouldReturn404ForOtherOwnersOnGetAndMutations() throws Exception {
        User owner = user("category.idor.owner");
        User attacker = user("category.idor.attacker");
        Category category = category(owner, "Privada", CategoryApplicability.BOTH, true);
        LoginTokenResponse token = login(attacker, "senhaValida123");
        String path = "/categories/" + category.getId();

        mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, bearer(token))).andExpect(status().isNotFound());
        mockMvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Alterada\",\"applicability\":\"BOTH\"}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(patch(path + "/deactivate").header(HttpHeaders.AUTHORIZATION, bearer(token))).andExpect(status().isNotFound());
        mockMvc.perform(patch(path + "/activate").header(HttpHeaders.AUTHORIZATION, bearer(token))).andExpect(status().isNotFound());
        assertThat(categories.findByIdAndOwnerId(category.getId(), owner.getId()).orElseThrow().getName()).isEqualTo("Privada");
    }

    @Test
    void shouldUpdateDeactivateAndActivateOwnedCategory() throws Exception {
        User owner = user("category.mutations");
        Category category = category(owner, "Original", CategoryApplicability.EXPENSE, true);
        LoginTokenResponse token = login(owner, "senhaValida123");
        String path = "/categories/" + category.getId();

        mockMvc.perform(put(path).header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Atualizada\",\"applicability\":\"BOTH\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Atualizada"))
            .andExpect(jsonPath("$.applicability").value("BOTH"));
        mockMvc.perform(patch(path + "/deactivate").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(patch(path + "/deactivate").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(patch(path + "/activate").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));
        mockMvc.perform(patch(path + "/activate").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturn409ForDuplicateAndReactivationConflict() throws Exception {
        User owner = user("category.conflict");
        LoginTokenResponse token = login(owner, "senhaValida123");
        Category old = category(owner, "Alimentação", CategoryApplicability.EXPENSE, false);
        category(owner, "Alimentação", CategoryApplicability.INCOME, true);
        mockMvc.perform(post("/categories").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" ALIMENTAÇÃO \",\"applicability\":\"BOTH\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.requestId").exists())
            .andExpect(content().string(not(containsString("constraint"))));
        mockMvc.perform(patch("/categories/" + old.getId() + "/activate")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404ForUnknownAnd400ForMalformedIds() throws Exception {
        LoginTokenResponse token = login(user("category.ids"), "senhaValida123");
        mockMvc.perform(get("/categories/" + UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/categories/not-a-uuid").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.requestId").exists());
    }

    private User user(String prefix) {
        return users.saveAndFlush(new User(prefix + "." + UUID.randomUUID() + "@example.test", passwordService.encode("senhaValida123")));
    }

    private Category category(User owner, String name, CategoryApplicability applicability, boolean active) {
        Category category = new Category(owner.getId(), name, applicability);
        if (!active) category.deactivate();
        return categories.saveAndFlush(category);
    }

    private LoginTokenResponse login(User user, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(user.getEmail(), password)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return new LoginTokenResponse(json.get("accessToken").asText(), json.get("tokenType").asText(), json.get("expiresIn").asLong());
    }

    private String bearer(LoginTokenResponse token) {
        return token.tokenType() + " " + token.accessToken();
    }
}
