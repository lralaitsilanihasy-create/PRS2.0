-- 2026-06-26 — Date/heure de dispatch.
-- t_dispatch.DATE_DISPATCH : DATE -> TIMESTAMP (préciser l'heure du dispatch).
-- Colonne en MAJUSCULES quotée (schéma réel). ddl-auto=update n'altère pas le type
-- d'une colonne existante -> cet ALTER est indispensable.
-- NB : référence H2 (tests) non concernée — schéma recréé depuis les entités.
-- NB : aucune notion de « pré-dispatch » en base (pas de t_predispatch) -> pas de datePredispatch.

ALTER TABLE t_dispatch
    ALTER COLUMN "DATE_DISPATCH" TYPE TIMESTAMP USING "DATE_DISPATCH"::timestamp;

-- Réversion :
-- ALTER TABLE t_dispatch ALTER COLUMN "DATE_DISPATCH" TYPE DATE USING "DATE_DISPATCH"::date;
