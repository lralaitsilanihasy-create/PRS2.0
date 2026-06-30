-- 2026-06-30 — Chemin du PDF de lettre de renvoi stocké sur le système de fichiers (FSX, LR/).
-- Le PDF généré à la signature est désormais écrit sur disque ; la colonne CHEMIN_DOCUMENT porte
-- le chemin du fichier. Colonne créée par Hibernate (ddl-auto) ; script idempotent pour base neuve.
-- NB : la colonne DOCUMENT_PDF (bytea) est conservée pour compatibilité.

ALTER TABLE t_lettre_renvoi ADD COLUMN IF NOT EXISTS "CHEMIN_DOCUMENT" VARCHAR(500);

-- Réversion :
-- ALTER TABLE t_lettre_renvoi DROP COLUMN IF EXISTS "CHEMIN_DOCUMENT";
