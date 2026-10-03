-- ============================================================
-- Equilibra - Baseline Migration
-- ============================================================
-- Esta migration cria a tabela de controle do Flyway (baseline v0)
-- As migrations reais de negócio serão criadas nas próximas tasks.
-- ============================================================

-- O Flyway criará automaticamente a tabela flyway_schema_history
-- Esta migration serve apenas para marcar o baseline v0
SELECT 'Baseline v0 - Flyway initialized' AS message;