-- 2026-06-21 — Référence officielle à la réception (chantier reference-reception)
-- Compteur de référence PAR COMBINAISON (TYPE_DOSSIER, CODE_LOCALITE, ANNEE_EXERCICE).
-- DERNIERE_VALEUR est incrémentée COTE SGBD (UPDATE +1 atomique si le contexte existe, sinon
-- INSERT a 1), per-combinaison, sans SELECT FOR UPDATE ni compteur applicatif.
-- La PK composite garantit l'unicite (jamais de doublon).
-- NB : en dev, Hibernate (ddl-auto=update) crée aussi cette table depuis l'entité SequenceReference.
CREATE TABLE IF NOT EXISTS t_sequence_reference (
    "TYPE_DOSSIER"    varchar(10) NOT NULL,
    "CODE_LOCALITE"   varchar(20) NOT NULL,
    "ANNEE_EXERCICE"  integer     NOT NULL,
    "DERNIERE_VALEUR" bigint      NOT NULL,
    CONSTRAINT pk_sequence_reference
        PRIMARY KEY ("TYPE_DOSSIER", "CODE_LOCALITE", "ANNEE_EXERCICE")
);
