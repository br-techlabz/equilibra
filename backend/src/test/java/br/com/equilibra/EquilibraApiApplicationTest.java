package br.com.equilibra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
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
class EquilibraApiApplicationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        assertThat(jdbcTemplate).isNotNull();
    }

    @Test
    void flywayRunsBaselineMigration() {
        Integer historyTableCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = 'flyway_schema_history'
            """, Integer.class);

        Integer successfulBaselineCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE version = '0'
              AND description = 'baseline'
              AND success = 1
            """, Integer.class);

        assertThat(historyTableCount).isEqualTo(1);
        assertThat(successfulBaselineCount).isEqualTo(1);
    }
}
