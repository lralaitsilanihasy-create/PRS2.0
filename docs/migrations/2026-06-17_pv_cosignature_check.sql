-- Migration manuelle (PostgreSQL / pgAdmin) — pas de Flyway (cf. CLAUDE.md).
-- Contexte : défense en profondeur pour la co-signature du PV d'examen. Un PV ne peut être
-- SIGNE que si le Membre a signé ET qu'au moins un co-signataire (Président OU CC) a signé.
-- Le contrôle métier est déjà fait dans PvExamenService.signer ; cette contrainte garantit
-- l'invariant au niveau SGBD (Hibernate ddl-auto=update ne crée pas les CHECK applicatifs).
--
-- Identifiants en MAJUSCULES → à citer entre guillemets (globally_quoted_identifiers=true).

-- Vérification préalable : aucune ligne SIGNE ne doit violer l'invariant (sinon l'ALTER échoue).
-- SELECT "ID_PV" FROM t_pv_examen
--  WHERE "STATUT_PV" = 'SIGNE'
--    AND NOT ("DATE_SIGNATURE_MEMBRE" IS NOT NULL
--             AND ("DATE_SIGNATURE_PRESIDENT" IS NOT NULL OR "DATE_SIGNATURE_CC" IS NOT NULL));

ALTER TABLE t_pv_examen
  ADD CONSTRAINT t_pv_examen_cosignataire_check
  CHECK ("STATUT_PV" <> 'SIGNE'
         OR ("DATE_SIGNATURE_MEMBRE" IS NOT NULL
             AND ("DATE_SIGNATURE_PRESIDENT" IS NOT NULL OR "DATE_SIGNATURE_CC" IS NOT NULL)));
