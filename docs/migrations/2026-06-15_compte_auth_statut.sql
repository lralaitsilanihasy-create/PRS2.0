-- Migration manuelle (PostgreSQL / pgAdmin) — pas de Flyway dans le projet (cf. CLAUDE.md).
-- Contexte : ajout du cycle de vie d'inscription sur t_compte_auth (colonne STATUT,
-- + MOTIF_REFUS / DATE_DECISION / IM_VALIDATEUR), créées automatiquement par Hibernate
-- ddl-auto=update au démarrage. Ce script ne fait que le BACKFILL des lignes existantes.
--
-- Identifiants en MAJUSCULES → à citer entre guillemets (globally_quoted_identifiers=true).

-- =====================================================================
-- Backfill du STATUT des comptes existants.
--   IMPORTANT : tout compte déjà ACTIF (ACTIF=true) devient STATUT='ACTIF' afin de NE PAS
--   être bloqué au login. Seuls les comptes déjà inactifs (ACTIF=false, ex. anciennes
--   inscriptions en attente) reçoivent 'EN_ATTENTE' — ils étaient déjà non connectables.
--   Invariant préservé : ACTIF=true ⟺ STATUT='ACTIF'.
-- =====================================================================
UPDATE t_compte_auth
   SET "STATUT" = CASE WHEN "ACTIF" THEN 'ACTIF' ELSE 'EN_ATTENTE' END
 WHERE "STATUT" IS NULL;

-- Vérification (doit renvoyer 0 ligne avec STATUT NULL, et aucun compte ACTIF=true en EN_ATTENTE) :
--   SELECT count(*) FROM t_compte_auth WHERE "STATUT" IS NULL;
--   SELECT count(*) FROM t_compte_auth WHERE "ACTIF" = true AND "STATUT" <> 'ACTIF';
