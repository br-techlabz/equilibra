package br.com.equilibra.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuração explícita do Flyway para garantir que as migrations sejam executadas.
 * <p>
 * Garante que o Flyway execute as migrations na inicialização da aplicação.
 * </p>
 */
@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.enabled:true}")
    private boolean flywayEnabled;

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String locations;

    @Value("${spring.flyway.baseline-on-migrate:false}")
    private boolean baselineOnMigrate;

    @Value("${spring.flyway.baseline-version:0}")
    private String baselineVersion;

    @Value("${spring.flyway.baseline-description:Initial baseline}")
    private String baselineDescription;

    @Value("${spring.flyway.validate-on-migrate:true}")
    private boolean validateOnMigrate;

    @Value("${spring.flyway.clean-disabled:false}")
    private boolean cleanDisabled;

    @Value("${spring.flyway.clean-on-validation-error:true}")
    private boolean cleanOnValidationError;

    @Value("${spring.flyway.out-of-order:false}")
    private boolean outOfOrder;

    @Bean
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(false)
            .cleanDisabled(false)
            .validateOnMigrate(true)
            .outOfOrder(false)
            .load();

        // Executar migrations
        flyway.migrate();

        return flyway;
    }
}