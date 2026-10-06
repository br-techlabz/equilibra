package br.com.equilibra.category.infrastructure;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMigrationTest {

    @Test
    void shouldMigrateEmptyDatabase() throws Exception {
        String url = "jdbc:tc:mysql:8.0:///category_clean?useSSL=false&allowPublicKeyRetrieval=true";
        try (Connection connection = DriverManager.getConnection(url, "test", "test")) {
            Flyway flyway = migration(url, null);
            flyway.migrate();
            flyway.validate();
            assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("5");
            try (var query = connection.prepareStatement("SELECT COUNT(*) FROM categories");
                 var result = query.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt(1)).isZero();
            }
        }
    }

    @Test
    void shouldUpgradeV3WithoutChangingExistingUsersOrAccounts() throws Exception {
        String url = "jdbc:tc:mysql:8.0:///category_incremental?useSSL=false&allowPublicKeyRetrieval=true";
        try (Connection connection = DriverManager.getConnection(url, "test", "test")) {
            migration(url, "3").migrate();
            String userId = UUID.randomUUID().toString();
            String accountId = UUID.randomUUID().toString();
            String encoded = PasswordEncoderFactories.createDelegatingPasswordEncoder()
                .encode(UUID.randomUUID().toString());
            try (var insert = connection.prepareStatement(
                "INSERT INTO users (id, email, password_hash) VALUES (?, ?, ?)")) {
                insert.setString(1, userId);
                insert.setString(2, "migration@example.test");
                insert.setString(3, encoded);
                insert.executeUpdate();
            }
            try (var insert = connection.prepareStatement(
                "INSERT INTO asset_accounts (id, owner_id, name, normalized_name, type, initial_balance) VALUES (?, ?, ?, ?, ?, ?)")) {
                insert.setString(1, accountId);
                insert.setString(2, userId);
                insert.setString(3, "Carteira");
                insert.setString(4, "carteira");
                insert.setString(5, "CASH");
                insert.setBigDecimal(6, new BigDecimal("123.45"));
                insert.executeUpdate();
            }

            Flyway upgraded = migration(url, null);
            assertThat(upgraded.migrate().migrationsExecuted).isEqualTo(2);
            upgraded.validate();
            assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("5");
            try (var query = connection.prepareStatement(
                "SELECT a.owner_id, a.name, a.initial_balance, u.email FROM asset_accounts a JOIN users u ON a.owner_id = u.id WHERE a.id = ?")) {
                query.setString(1, accountId);
                try (var result = query.executeQuery()) {
                    assertThat(result.next()).isTrue();
                    assertThat(result.getString("owner_id")).isEqualTo(userId);
                    assertThat(result.getString("name")).isEqualTo("Carteira");
                    assertThat(result.getBigDecimal("initial_balance")).isEqualByComparingTo("123.45");
                    assertThat(result.getString("email")).isEqualTo("migration@example.test");
                }
            }
            try (var query = connection.prepareStatement("SELECT COUNT(*) FROM categories");
                 var result = query.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getInt(1)).isZero();
            }
        }
    }

    private Flyway migration(String url, String target) {
        var configuration = Flyway.configure()
            .dataSource(url, "test", "test")
            .locations("classpath:db/migration")
            .cleanDisabled(true);
        if (target != null) configuration.target(target);
        return configuration.load();
    }
}
