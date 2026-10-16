-- ============================================================
-- Equilibra - Migration V27: Alinhar CHECKs aos enums do dominio
-- ============================================================
-- Contexto: a reescrita da V12/V14 na Sprint 9 introduziu CHECKs
-- com valores que nao correspondem aos enums do codigo:
--   - ck_recurrence_status aceitava ('ACTIVE','INACTIVE'), mas
--     RecurrenceStatus = ACTIVE, PAUSED, ENDED, CANCELLED
--   - ck_recurrence_frequency aceitava DAILY/WEEKLY, mas
--     RecurrenceFrequency = MONTHLY, YEARLY
--   - ck_recurrence_type e ck_commitment_type aceitavam
--     'TRANSFER', mas RecurrenceType/CommitmentType =
--     EXPENSE, INCOME
-- A troca e condicional (DROP se existir + ADD correto) para
-- valer tanto em bancos novos (reescritos) quanto em bancos
-- antigos (sem essas CHECKs).

CREATE PROCEDURE `sp_replace_check_constraint`(
    IN p_table_name VARCHAR(64),
    IN p_constraint_name VARCHAR(64),
    IN p_add_ddl TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND CONSTRAINT_NAME = p_constraint_name
    ) THEN
        SET @equilibra_drop = CONCAT('ALTER TABLE `', p_table_name, '` DROP CHECK `', p_constraint_name, '`');
        PREPARE equilibra_drop_stmt FROM @equilibra_drop;
        EXECUTE equilibra_drop_stmt;
        DEALLOCATE PREPARE equilibra_drop_stmt;
    END IF;
    SET @equilibra_add = p_add_ddl;
    PREPARE equilibra_add_stmt FROM @equilibra_add;
    EXECUTE equilibra_add_stmt;
    DEALLOCATE PREPARE equilibra_add_stmt;
END;

CALL `sp_replace_check_constraint`(
    'recurrence_rules',
    'ck_recurrence_type',
    'ALTER TABLE `recurrence_rules` ADD CONSTRAINT `ck_recurrence_type` CHECK (`type` IN (''EXPENSE'', ''INCOME''))'
);

CALL `sp_replace_check_constraint`(
    'recurrence_rules',
    'ck_recurrence_frequency',
    'ALTER TABLE `recurrence_rules` ADD CONSTRAINT `ck_recurrence_frequency` CHECK (`frequency` IN (''MONTHLY'', ''YEARLY''))'
);

CALL `sp_replace_check_constraint`(
    'recurrence_rules',
    'ck_recurrence_status',
    'ALTER TABLE `recurrence_rules` ADD CONSTRAINT `ck_recurrence_status` CHECK (`status` IN (''ACTIVE'', ''PAUSED'', ''ENDED'', ''CANCELLED''))'
);

CALL `sp_replace_check_constraint`(
    'financial_commitments',
    'ck_commitment_type',
    'ALTER TABLE `financial_commitments` ADD CONSTRAINT `ck_commitment_type` CHECK (`type` IN (''EXPENSE'', ''INCOME''))'
);

DROP PROCEDURE `sp_replace_check_constraint`;
