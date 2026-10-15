package br.com.equilibra.account.api;

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
import br.com.equilibra.transaction.domain.TransactionStatus;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AssetAccountReconciliationIntegrationTest {

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private FilterChainProxy springSecurityFilterChain;
    @Autowired private UserRepository users;
    @Autowired private AssetAccountRepository accounts;
    @Autowired private CategoryRepository categories;
    @Autowired private FinancialTransactionRepository transactions;
    @Autowired private PasswordService passwordService;
    @Autowired private JwtTokenService jwtTokenService;

    private MockMvc mockMvc;
    private User owner;
    private String ownerToken;
    private AssetAccount checkingAccount;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters(new RequestIdFilter(), springSecurityFilterChain)
            .build();

        owner = users.saveAndFlush(new User("reconcile.test." + UUID.randomUUID() + "@example.test", passwordService.encode("senhaValida123")));
        ownerToken = bearer(jwtTokenService.issueAccessToken(new br.com.equilibra.auth.application.AuthenticatedUser(owner.getId(), owner.getEmail())));

        checkingAccount = accounts.saveAndFlush(new AssetAccount(owner.getId(), "Conta Corrente", AssetAccountType.CHECKING, new BigDecimal("1000.00")));
    }

    @Test
    void shouldReconcileWithPositiveDifferenceCreateIncome() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        mockMvc.perform(post("/asset-accounts/" + checkingAccount.getId() + "/reconcile")
                .header(HttpHeaders.AUTHORIZATION, ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reconciliationDate": "%s",
                      "informedBalance": 1200.00
                    }
                    """.formatted(today)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.calculatedBalance").value(1000.00))
            .andExpect(jsonPath("$.informedBalance").value(1200.00))
            .andExpect(jsonPath("$.difference").value(200.00))
            .andExpect(jsonPath("$.adjustmentType").value("INCOME"))
            .andExpect(jsonPath("$.createdTransactionId").isNotEmpty());

        var activeTransactions = transactions.findAllByOwnerIdAndStatusOrderByOccurredAtAsc(owner.getId(), TransactionStatus.ACTIVE);
        assertThat(activeTransactions).hasSize(1);
        FinancialTransaction tx = activeTransactions.get(0);
        assertThat(tx.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(tx.getAmount()).isEqualByComparingTo("200.00");
        assertThat(tx.getDescription()).isEqualTo("Ajuste de Saldo");
        assertThat(tx.getDestinationAccountId()).isEqualTo(checkingAccount.getId());
    }

    @Test
    void shouldReconcileWithNegativeDifferenceCreateExpense() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        mockMvc.perform(post("/asset-accounts/" + checkingAccount.getId() + "/reconcile")
                .header(HttpHeaders.AUTHORIZATION, ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reconciliationDate": "%s",
                      "informedBalance": 850.00
                    }
                    """.formatted(today)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.calculatedBalance").value(1000.00))
            .andExpect(jsonPath("$.informedBalance").value(850.00))
            .andExpect(jsonPath("$.difference").value(-150.00))
            .andExpect(jsonPath("$.adjustmentType").value("EXPENSE"))
            .andExpect(jsonPath("$.createdTransactionId").isNotEmpty());

        var activeTransactions = transactions.findAllByOwnerIdAndStatusOrderByOccurredAtAsc(owner.getId(), TransactionStatus.ACTIVE);
        assertThat(activeTransactions).hasSize(1);
        FinancialTransaction tx = activeTransactions.get(0);
        assertThat(tx.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(tx.getAmount()).isEqualByComparingTo("150.00");
        assertThat(tx.getDescription()).isEqualTo("Ajuste de Saldo");
        assertThat(tx.getSourceAccountId()).isEqualTo(checkingAccount.getId());
    }

    @Test
    void shouldReconcileWithZeroDifferenceCreateNoTransaction() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        mockMvc.perform(post("/asset-accounts/" + checkingAccount.getId() + "/reconcile")
                .header(HttpHeaders.AUTHORIZATION, ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reconciliationDate": "%s",
                      "informedBalance": 1000.00
                    }
                    """.formatted(today)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.calculatedBalance").value(1000.00))
            .andExpect(jsonPath("$.informedBalance").value(1000.00))
            .andExpect(jsonPath("$.difference").value(0.00))
            .andExpect(jsonPath("$.adjustmentType").value("NONE"))
            .andExpect(jsonPath("$.createdTransactionId").isEmpty());

        assertThat(transactions.findAllByOwnerIdAndStatusOrderByOccurredAtAsc(owner.getId(), TransactionStatus.ACTIVE)).isEmpty();
    }

    @Test
    void shouldRejectFutureReconciliationDate() throws Exception {
        LocalDate futureDate = LocalDate.now(ZoneOffset.UTC).plusDays(1);

        mockMvc.perform(post("/asset-accounts/" + checkingAccount.getId() + "/reconcile")
                .header(HttpHeaders.AUTHORIZATION, ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reconciliationDate": "%s",
                      "informedBalance": 1000.00
                    }
                    """.formatted(futureDate)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInactiveAccount() throws Exception {
        checkingAccount.deactivate();
        accounts.saveAndFlush(checkingAccount);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        mockMvc.perform(post("/asset-accounts/" + checkingAccount.getId() + "/reconcile")
                .header(HttpHeaders.AUTHORIZATION, ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reconciliationDate": "%s",
                      "informedBalance": 1000.00
                    }
                    """.formatted(today)))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectOtherUserAccount() throws Exception {
        User other = users.saveAndFlush(new User("other." + UUID.randomUUID() + "@example.test", passwordService.encode("senhaValida123")));
        AssetAccount otherAccount = accounts.saveAndFlush(new AssetAccount(other.getId(), "Conta Outro", AssetAccountType.CASH, new BigDecimal("500.00")));
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        mockMvc.perform(post("/asset-accounts/" + otherAccount.getId() + "/reconcile")
                .header(HttpHeaders.AUTHORIZATION, ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reconciliationDate": "%s",
                      "informedBalance": 500.00
                    }
                    """.formatted(today)))
            .andExpect(status().isNotFound());
    }

    private String bearer(LoginTokenResponse token) {
        return token.tokenType() + " " + token.accessToken();
    }
}
