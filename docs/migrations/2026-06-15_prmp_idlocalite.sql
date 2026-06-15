-- Migration manuelle (PostgreSQL / pgAdmin) — pas de Flyway dans le projet (cf. CLAUDE.md).
-- Contexte : la PRMP n'a plus de localité propre ; la localité d'un dossier vient de l'entité
-- contractante choisie à la saisie. La colonne t_prmp.ID_LOCALITE est dépréciée.
--
-- Identifiants en MAJUSCULES → à citer entre guillemets (spring.jpa.properties.hibernate.globally_quoted_identifiers=true).

-- =====================================================================
-- E.1 — Relâcher la contrainte NOT NULL (étape non destructive, réversible).
--       À appliquer après le déploiement du code qui cesse d'écrire ID_LOCALITE.
-- =====================================================================
ALTER TABLE t_prmp ALTER COLUMN "ID_LOCALITE" DROP NOT NULL;

-- Réversible si besoin (uniquement si aucune ligne n'a ID_LOCALITE NULL) :
--   ALTER TABLE t_prmp ALTER COLUMN "ID_LOCALITE" SET NOT NULL;


-- =====================================================================
-- E.2 — Suppression définitive de la colonne (DESTRUCTIF, difficilement réversible).
--       APPLIQUÉE le 2026-06-15 après audit (aucun code/requête/vue ne référence
--       plus ID_LOCALITE) et validation. Le code (Prmp, PrmpDto, PrmpMapper,
--       PrmpService) ne mappe plus la colonne ; la suite de tests est verte.
-- =====================================================================
ALTER TABLE t_prmp DROP COLUMN "ID_LOCALITE";
