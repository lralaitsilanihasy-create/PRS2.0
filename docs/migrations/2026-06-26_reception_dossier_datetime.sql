-- 2026-06-26 — Date/heure de réception et de soumission (enregistrement du secrétariat).
-- 1) t_reception.DATE_RECEPTION : DATE -> TIMESTAMP (préciser l'heure de réception).
-- 2) t_dossier.DATE_SOUMISSION : nouvelle colonne TIMESTAMP, posée à la saisie
--    (POST /api/saisies/ppm -> LocalDateTime.now()) ; NULL pour les dossiers anciens.
--
-- Colonnes en MAJUSCULES quotées (schéma réel). Idempotent (IF NOT EXISTS / type cible).
-- NB : référence H2 (tests) non concernée — schéma recréé depuis les entités (ddl-auto).
-- NB : ddl-auto=update n'altère pas le type d'une colonne existante -> l'ALTER ci-dessous
--      est indispensable (DATE_SOUMISSION serait, lui, ajouté automatiquement).

ALTER TABLE t_reception
    ALTER COLUMN "DATE_RECEPTION" TYPE TIMESTAMP USING "DATE_RECEPTION"::timestamp;

ALTER TABLE t_dossier
    ADD COLUMN IF NOT EXISTS "DATE_SOUMISSION" TIMESTAMP;

-- Réversion :
-- ALTER TABLE t_reception ALTER COLUMN "DATE_RECEPTION" TYPE DATE USING "DATE_RECEPTION"::date;
-- ALTER TABLE t_dossier DROP COLUMN IF EXISTS "DATE_SOUMISSION";
