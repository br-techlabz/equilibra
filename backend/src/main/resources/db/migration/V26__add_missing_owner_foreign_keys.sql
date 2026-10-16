-- ============================================================
-- Equilibra - Migration V26: Adicionar FKs de owner ausentes
-- ============================================================
-- Contexto: as migrations V10/V12/V14 foram reescritas na
-- Sprint 9 incluindo FKs de owner_id -> users(id). Bancos
-- criados a partir dos arquivos reescritos ja possuem essas
-- constraints; bancos criados antes da reescrita, nao.
-- Os ADDs abaixo sao condicionais (via INFORMATION_SCHEMA)
-- para aplicar com seguranca nos dois casos.
-- Pre-condicao verificada: nao ha owner_id orfao nas 3 tabelas.

CREATE PROCEDURE `sp_add_constraint_if_missing`(
    IN p_table_name VARCHAR(64),
    IN p_constraint_name VARCHAR(64),
    IN p_ddl TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND CONSTRAINT_NAME = p_constraint_name
    ) THEN
        SET @equilibra_ddl = p_ddl;
        PREPARE equilibra_stmt FROM @equilibra_ddl;
        EXECUTE equilibra_stmt;
        DEALLOCATE PREPARE equilibra_stmt;
    END IF;
END;

CALL `sp_add_constraint_if_missing`(
    'category_budgets',
    'fk_category_budgets_owner',
    'ALTER TABLE `category_budgets` ADD CONSTRAINT `fk_category_budgets_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)'
);

CALL `sp_add_constraint_if_missing`(
    'recurrence_rules',
    'fk_recurrence_owner',
    'ALTER TABLE `recurrence_rules` ADD CONSTRAINT `fk_recurrence_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)'
);

CALL `sp_add_constraint_if_missing`(
    'financial_commitments',
    'fk_commitment_owner',
    'ALTER TABLE `financial_commitments` ADD CONSTRAINT `fk_commitment_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)'
);

DROP PROCEDURE `sp_add_constraint_if_missing`;
