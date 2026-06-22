-- 2026-06-22 — Regles de passation pour la nature SERVICES (id 3) en localite ANT.
-- Referentiel manquant : t_seuil/t_regle_passation ne couvraient que Travaux (nature 1) et
-- Fournitures (nature 2) en ANT. Sans regle Services, validerOuAppliquerMode renvoyait idMode=null
-- (MODE_NON_DETERMINE) lors d'une rectification de marche passe en Services -> mode "a recalculer".
--
-- Jeu de regles calque a l'identique sur FOURNITURES (nature 2) en ANT :
--   tranche 0 - 100 000 000        : situation 1 -> mode 4 ; situation 2 -> mode 3
--   tranche 100 000 001 - 500 000 000 : situation 1 -> mode 2 ; situation 2 -> mode 3
--   tranche > 500 000 000          : situation 1 -> mode 1 ; situation 2 -> mode 1
--
-- Applique et verifie sur DBPRS20 le 2026-06-22 (marche 11 : Services 70M sit.1 -> mode 4).
-- Idempotent (ON CONFLICT DO NOTHING sur les PK ID_SEUIL / ID_REGLE).
-- NB : reference H2 (tests) non concernee — les tests seedent leurs propres seuils/regles.

INSERT INTO t_seuil ("ID_SEUIL", "ID_LOCALITE", "ID_NATURE", "MONTANT_MIN", "MONTANT_MAX") VALUES
    (917, 'ANT', 3, 0,           100000000),
    (918, 'ANT', 3, 100000001,   500000000),
    (919, 'ANT', 3, 500000001,   NULL)
ON CONFLICT ("ID_SEUIL") DO NOTHING;

INSERT INTO t_regle_passation ("ID_REGLE", "ID_SITUATION", "ID_SEUIL", "ID_MODE", "PRIORITE") VALUES
    (925, 1, 917, 4, 1),
    (926, 2, 917, 3, 1),
    (927, 1, 918, 2, 1),
    (928, 2, 918, 3, 1),
    (929, 1, 919, 1, 1),
    (930, 2, 919, 1, 1)
ON CONFLICT ("ID_REGLE") DO NOTHING;

-- Reversion :
-- DELETE FROM t_regle_passation WHERE "ID_REGLE" BETWEEN 925 AND 930;
-- DELETE FROM t_seuil WHERE "ID_SEUIL" BETWEEN 917 AND 919;
