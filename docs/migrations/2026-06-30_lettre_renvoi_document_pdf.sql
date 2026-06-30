-- 2026-06-30 — Document PDF de la lettre de renvoi signée (t_lettre_renvoi.DOCUMENT_PDF).
-- Le PDF est généré programmatiquement (OpenPDF) à la signature et stocké en bytea.
-- Colonne créée par Hibernate (ddl-auto) depuis l'entité LettreRenvoi ; ce script (idempotent)
-- en garantit l'existence sur une base neuve. NB : référence H2 (tests) non concernée.

ALTER TABLE t_lettre_renvoi ADD COLUMN IF NOT EXISTS "DOCUMENT_PDF" BYTEA;

-- Réversion :
-- ALTER TABLE t_lettre_renvoi DROP COLUMN IF EXISTS "DOCUMENT_PDF";
