package br.com.equilibra.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuração explícita do Flyway.
 * <p>
 * Garante que o Flyway execute as migrations na inicialização da aplicação.
 * </p>
 */
@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.enabled:true}")
    private boolean flywayEnabled;

    @Value("${spring.flyway.baseline-on-migrate:true}")
    private boolean baselineOnMigrate;

    @Value("${spring.flyway.baseline-version:0}")
    private String baselineVersion;

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String locations;

    @Value("${spring.flyway.validate-on-migrate:false}")
    private boolean validateOnMigrate;

    @Bean
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(baselineOnMigrate)
            .baselineVersion(baselineVersion)
            .locations(locations)
            .validateOnMigrate(validateOnMigrate)
            .load();

        // Executar migrations
        flyway.migrate();

        return flyway;
    }
}