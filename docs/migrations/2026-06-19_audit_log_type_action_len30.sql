-- Migration manuelle (PostgreSQL / pgAdmin) — pas de Flyway (cf. CLAUDE.md).
-- Contexte : nouveau TYPE_ACTION d'audit RECTIFICATION_PRMP (18 car.) > varchar(10) → élargissement.
ALTER TABLE t_audit_log ALTER COLUMN "TYPE_ACTION" TYPE varchar(30);
