package br.com.equilibra.user.infrastructure;

import br.com.equilibra.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository para persistência de usuários.
 * <p>
 * Fornece operações essenciais para busca por email e verificação de existência.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Busca usuário por email (case-insensitive).
     * A normalização de email (lowercase) é feita na entidade antes da persistência.
     *
     * @param email email do usuário
     * @return Optional contendo o usuário se encontrado
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Verifica se existe usuário com o email informado.
     * Útil para validação de duplicidade antes de tentar persistir.
     *
     * @param email email do usuário
     * @return true se existir usuário com o email
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
}