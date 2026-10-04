package br.com.equilibra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração OpenAPI/Swagger para a Equilibra API.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.application.name:equilibra-api}")
    private String applicationName;

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title(applicationName + " - API")
                .version("v0.0.1")
                .description("API para gerenciamento de finanças domésticas - Equilibra")
                .contact(new Contact()
                    .name("Equilibra Team")
                    .email("suporte@equilibra.com.br")
                    .url("https://github.com/br-techlabz/equilibra"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://github.com/br-techlabz/equilibra/blob/main/LICENSE")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort + "/api")
                    .description("Servidor de desenvolvimento local"),
                new Server()
                    .url("https://api.equilibra.com.br")
                    .description("Servidor de produção")))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Access token JWT obtido via POST /auth/login")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}