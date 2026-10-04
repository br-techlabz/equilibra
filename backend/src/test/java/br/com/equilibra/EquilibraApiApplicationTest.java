package br.com.equilibra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração básico para verificar se o contexto Spring carrega corretamente
 * e se o Flyway executa as migrations usando MySQL via Testcontainers.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EquilibraApiApplicationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        assertThat(jdbcTemplate).isNotNull();
    }

    @Test
    void flywayRunsMigrations() {
        // Verifica se a tabela flyway_schema_history existe
        Integer historyTableCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = 'flyway_schema_history'
            """, Integer.class);

        assertThat(historyTableCount).isEqualTo(1);

        // Verifica se a migration V2 foi aplicada
        Integer usersTableCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = 'users'
            """, Integer.class);

        assertThat(usersTableCount).isEqualTo(1);

        // Verifica se a migration V2 foi registrada no Flyway
        Integer migrationV2Count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE version = '2'
              AND description = 'create users'
              AND success = 1
            """, Integer.class);

        assertThat(migrationV2Count).isEqualTo(1);
    }
}
