package br.com.equilibra.transaction.api;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.domain.AssetAccountType;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.auth.application.JwtTokenService;
import br.com.equilibra.auth.application.LoginTokenResponse;
import br.com.equilibra.auth.application.PasswordService;
import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.domain.CategoryApplicability;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.shared.web.filter.RequestIdFilter;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionType;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
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
class ExpenseControllerIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired private FilterChainProxy security;
    @Autowired private UserRepository users;
    @Autowired private AssetAccountRepository accounts;
    @Autowired private CategoryRepository categories;
    @Autowired private FinancialTransactionRepository transactions;
    @Autowired private PasswordService passwords;
    @Autowired private JwtTokenService tokens;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() { mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new RequestIdFilter(), security).build(); }

    @Test
    void shouldCreateListUpdateAndCancelExpense() throws Exception {
        User user = user("expense.crud");
        AssetAccount account = account(user, "Conta");
        Category category = category(user, "Alimentação", CategoryApplicability.EXPENSE);
        String token = bearer(token(user));
        String response = mockMvc.perform(post("/expenses").header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(body("Mercado", account, category, "10.10")))
            .andExpect(status().isCreated()).andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(jsonPath("$.status").value("ACTIVE")).andExpect(jsonPath("$.amount").value(10.10))
            .andExpect(jsonPath("$.ownerId").doesNotExist()).andReturn().getResponse().getContentAsString();
        String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(response).get("id").asText();
        mockMvc.perform(get("/expenses").header(HttpHeaders.AUTHORIZATION, token))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)));
        mockMvc.perform(put("/expenses/" + id).header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(body("Mercado atualizado", account, category, "20.20")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.amount").value(20.20));
        mockMvc.perform(patch("/expenses/" + id + "/cancel").header(HttpHeaders.AUTHORIZATION, token))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        assertThat(transactions.findByIdAndOwnerIdAndType(id, user.getId(), TransactionType.EXPENSE)).isPresent();
    }

    @Test
    void shouldRejectUnauthorizedInvalidMoneyWrongCategoryAndCrossOwner() throws Exception {
        mockMvc.perform(get("/expenses")).andExpect(status().isUnauthorized());
        User owner = user("expense.owner");
        User other = user("expense.other");
        AssetAccount account = account(owner, "Conta");
        Category income = category(owner, "Salário", CategoryApplicability.INCOME);
        String token = bearer(token(other));
        mockMvc.perform(post("/expenses").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON)
                .content(body("Invasiva", account, income, "0.00")))
            .andExpect(status().isNotFound());
        mockMvc.perform(post("/expenses").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON)
                .content(body("Invasiva", account, income, "10.10")))
            .andExpect(status().isNotFound());
        assertThat(transactions.findAllByOwnerIdOrderByOccurredAtDesc(owner.getId())).isEmpty();
    }

    @Test
    void shouldHandlePaginationCancelledAndOtherTypeIsolation() throws Exception {
        User user = user("expense.pagination");
        AssetAccount account = account(user, "Conta");
        Category category = category(user, "Alimentação", CategoryApplicability.BOTH);
        String token = bearer(token(user));
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/expenses").header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON)
                .content(body("Compra " + i, account, category, "1.00"))).andExpect(status().isCreated());
        }
        mockMvc.perform(get("/expenses?page=0&size=1").header(HttpHeaders.AUTHORIZATION, token))
            .andExpect(status().isOk()).andExpect(jsonPath("$.size").value(1)).andExpect(jsonPath("$.totalElements").value(2));
        String otherId = transactions.saveAndFlush(FinancialTransaction.income(user.getId(), "income", new BigDecimal("2.00"), java.time.Instant.now(), category.getId(), account.getId(), null)).getId();
        mockMvc.perform(get("/expenses/" + otherId).header(HttpHeaders.AUTHORIZATION, token)).andExpect(status().isNotFound());
    }

    private User user(String prefix) { return users.saveAndFlush(new User(prefix + "." + UUID.randomUUID() + "@example.test", passwords.encode("senhaValida123"))); }
    private AssetAccount account(User user, String name) { return accounts.saveAndFlush(new AssetAccount(user.getId(), name, AssetAccountType.CASH, new BigDecimal("100.00"))); }
    private Category category(User user, String name, CategoryApplicability applicability) { return categories.saveAndFlush(new Category(user.getId(), name, applicability)); }
    private LoginTokenResponse token(User user) { return tokens.issueAccessToken(new br.com.equilibra.auth.application.AuthenticatedUser(user.getId(), user.getEmail())); }
    private String bearer(LoginTokenResponse token) { return token.tokenType() + " " + token.accessToken(); }
    private String body(String description, AssetAccount account, Category category, String amount) { return "{\"description\":\"" + description + "\",\"occurredAt\":\"2026-10-06T12:00:00Z\",\"accountId\":\"" + account.getId() + "\",\"amount\":" + amount + ",\"categoryId\":\"" + category.getId() + "\"}"; }
}
