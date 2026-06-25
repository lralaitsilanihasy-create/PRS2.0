-- 2026-06-25 — Pièces jointes par type de dossier.
-- Deux tables créées automatiquement par Hibernate (ddl-auto=update) à partir des entités
-- TypePieceJointe (t_type_piece_jointe) et PieceJointeDossier (t_piece_jointe_dossier) :
-- aucune création de table manuelle nécessaire. Ce script ne fait que GARNIR le référentiel
-- t_type_piece_jointe avec les 5 lignes de base.
--
-- Lancer APRÈS un premier démarrage (les tables doivent exister). Idempotent (ON CONFLICT).
-- NB : référence H2 (tests) non concernée — les tests seedent eux-mêmes leurs types de pièces.

INSERT INTO t_type_piece_jointe ("ID_TYPE_PIECE", "LIBELLE_PIECE", "OBLIGATOIRE", "ID_TYPE_DOSSIER", "ORDRE") VALUES
    (1, 'Plan de passation des marchés', true,  'PPM', 1),
    (2, 'Budget prévisionnel',          true,  'PPM', 2),
    (3, 'Dossier d''appel d''offres',   true,  'DAO', 1),
    (4, 'Cahier des charges',           true,  'DAO', 2),
    (5, 'Avis de non-objection',        false, 'DAO', 3)
ON CONFLICT ("ID_TYPE_PIECE") DO NOTHING;

-- Recale la séquence IDENTITY au-delà des PK insérées explicitement, pour que les futurs
-- INSERT générés (POST /api/type-piece-jointes) n'entrent pas en collision avec 1..5.
SELECT setval(pg_get_serial_sequence('t_type_piece_jointe', 'id_type_piece'),
              (SELECT MAX("ID_TYPE_PIECE") FROM t_type_piece_jointe));

-- Réversion :
-- DELETE FROM t_type_piece_jointe WHERE "ID_TYPE_PIECE" BETWEEN 1 AND 5;
