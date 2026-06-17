-- Migration manuelle (PostgreSQL / pgAdmin) — pas de Flyway (cf. CLAUDE.md).
-- Contexte : branchement du circuit À LA SIGNATURE DU PV selon l'avis (⚠️ règle ajoutée).
--   * 4e valeur d'avis « Ne se prononce pas » (NSP) dans le référentiel tr_avis.
--   * EN_VERIFICATION devient l'état de repos des dossiers FAVR signés ; PV_SIGNE n'est plus
--     un état de repos → correctif des dossiers legacy restés à PV_SIGNE.
-- Identifiants en MAJUSCULES cités (globally_quoted_identifiers=true).

-- 1) Référentiel tr_avis : ajout de « Ne se prononce pas » (FAV, FAVR, DEF existent déjà).
INSERT INTO tr_avis ("ID_AVIS", "LIBELLE_AVIS")
VALUES ('NSP', 'Ne se prononce pas')
ON CONFLICT ("ID_AVIS") DO NOTHING;

-- 2) Correctif de données : PV_SIGNE n'est plus un état de repos du dossier.
--    Un dossier ayant au moins un PV signé FAVR (réserves non levées) → EN_VERIFICATION.
UPDATE t_dossier d
   SET "STATUT" = 'EN_VERIFICATION'
 WHERE d."STATUT" = 'PV_SIGNE'
   AND EXISTS (
        SELECT 1
          FROM t_reception r, t_dispatch di, t_examen e, t_pv_examen pv
         WHERE r."ID_DOSSIER"   = d."ID_DOSSIER"
           AND di."ID_RECEPTION" = r."ID_RECEPTION"
           AND e."ID_DISPATCH"   = di."ID_DISPATCH"
           AND pv."ID_EXAMEN"    = e."ID_EXAMEN"
           AND pv."STATUT_PV"    = 'SIGNE'
           AND pv."ID_AVIS"      = 'FAVR');

--    Les dossiers PV_SIGNE restants (aucun PV signé FAVR) → CLOTURE (avis FAV/DEF/NSP).
UPDATE t_dossier
   SET "STATUT" = 'CLOTURE'
 WHERE "STATUT" = 'PV_SIGNE';
