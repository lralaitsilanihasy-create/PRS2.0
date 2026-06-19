-- Migration manuelle (PostgreSQL / pgAdmin) — pas de Flyway (cf. CLAUDE.md).
-- Contexte : IM_ACTEUR (varchar 7) doit pouvoir porter un id PRMP (t_prmp.ID_PRMP = varchar 10)
-- pour la trace de rectification PRMP — évite toute troncature future. Élargissement à varchar(10).
ALTER TABLE t_audit_log ALTER COLUMN "IM_ACTEUR" TYPE varchar(10);
