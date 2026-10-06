package br.com.equilibra.transaction.api;

import br.com.equilibra.account.application.AccountBalanceQueryService;
import br.com.equilibra.account.api.AssetAccountResponse;
import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.domain.AssetAccountType;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.auth.application.AuthenticatedUser;
import br.com.equilibra.auth.application.JwtTokenService;
import br.com.equilibra.auth.application.LoginTokenResponse;
import br.com.equilibra.auth.application.PasswordService;
import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.domain.CategoryApplicability;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.dashboard.api.DashboardResponse;
import br.com.equilibra.dashboard.application.DashboardQueryService;
import br.com.equilibra.shared.web.filter.RequestIdFilter;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import br.com.equilibra.auth.infrastructure.security.AuthenticatedPrincipal;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class GoldenScenarioIntegrationTest {
    private static final Instant OCCURRED_AT = Instant.parse("2026-10-06T12:00:00Z");

    @Autowired private WebApplicationContext context;
    @Autowired private FilterChainProxy security;
    @Autowired private UserRepository users;
    @Autowired private AssetAccountRepository accounts;
    @Autowired private CategoryRepository categories;
    @Autowired private FinancialTransactionRepository transactions;
    @Autowired private AccountBalanceQueryService balances;
    @Autowired private DashboardQueryService dashboard;
    @Autowired private PasswordService passwords;
    @Autowired private JwtTokenService tokens;
    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilters(new RequestIdFilter(), security)
            .build();
    }

    @Test
    void shouldPreserveGoldenFinancialInvariantsThroughHttpAndMySql() throws Exception {
        User user = user("golden");
        AssetAccount accountA = account(user, "Conta A", "1000.00");
        AssetAccount accountB = account(user, "Conta B", "500.00");
        Category incomeCategory = category(user, "Receitas", CategoryApplicability.INCOME);
        Category expenseCategory = category(user, "Despesas", CategoryApplicability.EXPENSE);
        String bearer = bearer(user);
        authenticate(user);

        assertBalance(accountA, "1000.00");
        assertBalance(accountB, "500.00");
        assertThat(dashboard.query(OCCURRED_AT.minusSeconds(1), OCCURRED_AT.plusSeconds(1)).summary().netWorth())
            .isEqualByComparingTo("1500.00");

        String incomeId = create("/incomes", bearer, incomeBody(accountA, incomeCategory, "700.00"));
        authenticate(user);
        assertBalances(accountA, "1700.00", accountB, "500.00", "2200.00");
        assertDashboardFlow("700.00", "0.00", "700.00");

        String expenseId = create("/expenses", bearer, expenseBody(accountA, expenseCategory, "200.00"));
        authenticate(user);
        assertBalances(accountA, "1500.00", accountB, "500.00", "2000.00");
        assertDashboardFlow("700.00", "200.00", "500.00");

        String transferId = create("/transfers", bearer, transferBody(accountA, accountB, incomeCategory, "300.00"));
        authenticate(user);
        assertBalances(accountA, "1200.00", accountB, "800.00", "2000.00");
        assertDashboardFlow("700.00", "200.00", "500.00");
        mockMvc.perform(get("/transactions?size=20").header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(3));

        cancel("/transfers/" + transferId, bearer);
        authenticate(user);
        assertBalances(accountA, "1500.00", accountB, "500.00", "2000.00");
        cancel("/expenses/" + expenseId, bearer);
        authenticate(user);
        assertBalances(accountA, "1700.00", accountB, "500.00", "2200.00");
        cancel("/incomes/" + incomeId, bearer);
        authenticate(user);
        assertBalances(accountA, "1000.00", accountB, "500.00", "1500.00");
        assertDashboardFlow("0.00", "0.00", "0.00");

        assertThat(transactions.findById(incomeId).orElseThrow().getStatus().name()).isEqualTo("CANCELLED");
        assertThat(transactions.findById(expenseId).orElseThrow().getStatus().name()).isEqualTo("CANCELLED");
        assertThat(transactions.findById(transferId).orElseThrow().getStatus().name()).isEqualTo("CANCELLED");
    }

    @Test
    void shouldKeepPrecisionAndOwnerIsolation() throws Exception {
        User userA = user("golden-precision-a");
        User userB = user("golden-precision-b");
        AssetAccount accountA = account(userA, "Precisão", "0.10");
        AssetAccount accountB = account(userB, "Conta B", "9999.00");
        Category incomeA = category(userA, "Renda", CategoryApplicability.INCOME);
        String tokenA = bearer(userA);
        create("/incomes", tokenA, incomeBody(accountA, incomeA, "0.20"));
        authenticate(userA);

        assertBalance(accountA, "0.30");
        assertThat(balances.list(false)).hasSize(1);
        assertThat(balances.list(false).getFirst().currentBalance()).isEqualByComparingTo("0.30");
        assertThat(accountB.getOwnerId()).isEqualTo(userB.getId());
        mockMvc.perform(get("/transactions?size=20").header(HttpHeaders.AUTHORIZATION, tokenA))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }

    private void assertBalances(AssetAccount a, String expectedA, AssetAccount b, String expectedB, String expectedNetWorth) {
        assertBalance(a, expectedA);
        assertBalance(b, expectedB);
        assertThat(dashboard.query(OCCURRED_AT.minusSeconds(1), OCCURRED_AT.plusSeconds(1)).summary().netWorth())
            .isEqualByComparingTo(expectedNetWorth);
    }

    private void assertDashboardFlow(String income, String expense, String net) {
        DashboardResponse.Summary summary = dashboard.query(OCCURRED_AT.minusSeconds(1), OCCURRED_AT.plusSeconds(1)).summary();
        assertThat(summary.income()).isEqualByComparingTo(income);
        assertThat(summary.expense()).isEqualByComparingTo(expense);
        assertThat(summary.net()).isEqualByComparingTo(net);
    }

    private void assertBalance(AssetAccount account, String expected) {
        AssetAccountResponse response = balances.list(false).stream().filter(value -> value.id().equals(account.getId())).findFirst().orElseThrow();
        assertThat(response.currentBalance()).isEqualByComparingTo(expected);
    }

    private String create(String path, String bearer, String body) throws Exception {
        String response = mockMvc.perform(post(path).header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asText();
    }

    private void cancel(String path, String bearer) throws Exception {
        mockMvc.perform(patch(path + "/cancel").header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private String incomeBody(AssetAccount account, Category category, String amount) {
        return "{\"description\":\"Receita\",\"occurredAt\":\"" + OCCURRED_AT + "\",\"accountId\":\"" + account.getId() + "\",\"amount\":" + amount + ",\"categoryId\":\"" + category.getId() + "\"}";
    }

    private String expenseBody(AssetAccount account, Category category, String amount) {
        return "{\"description\":\"Despesa\",\"occurredAt\":\"" + OCCURRED_AT + "\",\"accountId\":\"" + account.getId() + "\",\"amount\":" + amount + ",\"categoryId\":\"" + category.getId() + "\"}";
    }

    private String transferBody(AssetAccount source, AssetAccount destination, Category category, String amount) {
        return "{\"description\":\"Transferência\",\"occurredAt\":\"" + OCCURRED_AT + "\",\"sourceAccountId\":\"" + source.getId() + "\",\"destinationAccountId\":\"" + destination.getId() + "\",\"amount\":" + amount + ",\"categoryId\":\"" + category.getId() + "\"}";
    }

    private User user(String prefix) {
        return users.saveAndFlush(new User(prefix + "." + UUID.randomUUID() + "@example.test", passwords.encode("senhaValida123")));
    }

    private AssetAccount account(User user, String name, String balance) {
        return accounts.saveAndFlush(new AssetAccount(user.getId(), name, AssetAccountType.CASH, new BigDecimal(balance)));
    }

    private Category category(User user, String name, CategoryApplicability applicability) {
        return categories.saveAndFlush(new Category(user.getId(), name + " " + UUID.randomUUID(), applicability));
    }

    private String bearer(User user) {
        LoginTokenResponse token = tokens.issueAccessToken(new AuthenticatedUser(user.getId(), user.getEmail()));
        return token.tokenType() + " " + token.accessToken();
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(new AuthenticatedPrincipal(user.getId()), null, java.util.List.of()));
    }
}
