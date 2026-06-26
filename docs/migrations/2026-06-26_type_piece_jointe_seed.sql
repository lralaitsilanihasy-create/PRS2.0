-- 2026-06-26 — Jeu de données initial du référentiel t_type_piece_jointe (20 lignes).
-- Pièces jointes attendues par type de dossier : PPM (5), DAO (8), MAOO (7).
-- La table est créée par Hibernate (ddl-auto) ; ce script ne fait que la GARNIR.
--
-- ⚠️ Schéma réel : colonnes en MAJUSCULES quotées ("LIBELLE_PIECE", …) et PK IDENTITY
-- (ID_TYPE_PIECE auto, non fournie). FK "ID_TYPE_DOSSIER" -> tr_type_dossier (PPM/DAO/MAOO existent).
-- Idempotent : n'insère que si le référentiel est encore vide.
-- NB : remplace l'ébauche provisoire à 5 lignes (2026-06-25) — jamais appliquée à DBPRS20.

INSERT INTO t_type_piece_jointe ("LIBELLE_PIECE", "OBLIGATOIRE", "ID_TYPE_DOSSIER", "ORDRE")
SELECT v.libelle, v.obligatoire, v.type_dossier, v.ordre
FROM (VALUES
    -- PPM (Plan de passation des marchés)
    ('Plan de passation des marchés signé',               true,  'PPM',  1),
    ('Budget prévisionnel de l''exercice',                true,  'PPM',  2),
    ('Arrêté ou décision portant nomination de la PRMP',  true,  'PPM',  3),
    ('Tableau récapitulatif des marchés',                 true,  'PPM',  4),
    ('Avis de non-objection (si requis)',                 false, 'PPM',  5),

    -- DAO (Dossier d'appel d'offres)
    ('Dossier d''appel d''offres complet',                true,  'DAO',  1),
    ('Cahier des clauses administratives générales',      true,  'DAO',  2),
    ('Cahier des clauses techniques particulières',       true,  'DAO',  3),
    ('Avis d''appel d''offres',                           true,  'DAO',  4),
    ('Estimation du coût des travaux/fournitures',        true,  'DAO',  5),
    ('Garantie de soumission',                            true,  'DAO',  6),
    ('Avis de non-objection (si requis)',                 false, 'DAO',  7),
    ('Rapport d''évaluation des offres',                  false, 'DAO',  8),

    -- MAOO (Marché par appel d'offres ouvert)
    ('Projet de marché signé',                            true,  'MAOO', 1),
    ('Cahier des charges',                                true,  'MAOO', 2),
    ('Devis estimatif détaillé',                          true,  'MAOO', 3),
    ('Procès-verbal d''ouverture des offres',             true,  'MAOO', 4),
    ('Rapport d''analyse des offres',                     true,  'MAOO', 5),
    ('Attestation de capacité financière',                false, 'MAOO', 6),
    ('Avis de non-objection (si requis)',                 false, 'MAOO', 7)
) AS v(libelle, obligatoire, type_dossier, ordre)
WHERE NOT EXISTS (SELECT 1 FROM t_type_piece_jointe);

-- Vérifications :
--   SELECT count(*) FROM t_type_piece_jointe;  -> 20
--   SELECT * FROM t_type_piece_jointe ORDER BY "ID_TYPE_DOSSIER", "ORDRE";

-- Réversion :
-- DELETE FROM t_type_piece_jointe;
