package br.com.equilibra;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Teste de integração básico para verificar se o contexto Spring carrega corretamente.
 * <p>
 * Utiliza Testcontainers para subir um MySQL real durante os testes (profile 'test').
 * O profile 'test' configura o datasource para usar Testcontainers automaticamente.
 * </p>
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class EquilibraApiApplicationTests {

    @Test
    void contextLoads() {
        // Este teste apenas verifica se o ApplicationContext sobe sem erros
        // Testes mais específicos serão adicionados nas próximas tasks
    }
}