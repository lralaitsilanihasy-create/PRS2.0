-- 2026-06-22 — refePv : reference du PV derivee du dossier (refeDossier avec /PV avant l'annee).
-- Format : xxxxx/type_dossier/code_localite/PV/annee (ex. 00003/PPM/CRM-ANT/PV/2026).
-- Run-once sur DBPRS20 (la colonne et la contrainte sont aussi creees par Hibernate ddl-auto/H2 via l'entite).

ALTER TABLE t_pv_examen ADD COLUMN IF NOT EXISTS "REFE_PV" varchar(120);

-- Backfill : UNIQUEMENT les dossiers au nouveau format .../YYYY (insere /PV avant l'annee).
-- Les anciennes references (ex. CNM-ANT-2026-000003) ne sont PAS derivables -> laissees NULL.
-- Cela evite (a) le misfire de la regex sur l'ancien format et (b) la collision UNIQUE
-- (ex. dossier 3 porte 4 PV en ancien format -> tous NULL, pas de doublon).
UPDATE t_pv_examen pv
SET "REFE_PV" = regexp_replace(d."REFE_DOSSIER", '/([0-9]{4})$', '/PV/\1')
FROM t_examen e, t_dispatch di, t_reception r, t_dossier d
WHERE e."ID_EXAMEN" = pv."ID_EXAMEN" AND di."ID_DISPATCH" = e."ID_DISPATCH"
  AND r."ID_RECEPTION" = di."ID_RECEPTION" AND d."ID_DOSSIER" = r."ID_DOSSIER"
  AND d."REFE_DOSSIER" ~ '/[0-9]{4}$';

-- Unicite (NULLs multiples autorises pour les PV sans reference derivable).
ALTER TABLE t_pv_examen ADD CONSTRAINT uq_pv_examen_refe_pv UNIQUE ("REFE_PV");
