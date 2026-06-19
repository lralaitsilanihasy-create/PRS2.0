-- Migration manuelle (PostgreSQL / pgAdmin) — pas de Flyway (cf. CLAUDE.md).
-- Contexte : nouveau statut de dossier EN_ATTENTE_DECISION_PRMP (24 car.) — obs. de vérification
-- non levées en attente de décision PRMP. La colonne t_dossier.STATUT était varchar(20) → on
-- l'élargit à varchar(30) (élargissement seul, sans perte de données).
-- H2 (tests) se recrée automatiquement à la nouvelle longueur via l'entité ; rien à faire côté tests.

ALTER TABLE t_dossier ALTER COLUMN "STATUT" TYPE varchar(30);
