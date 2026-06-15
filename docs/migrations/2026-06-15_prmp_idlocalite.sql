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
--       NE PAS exécuter avant l'audit confirmant qu'aucun code ne lit ID_LOCALITE
--       ET la validation explicite. Laissé en commentaire jusque-là.
-- =====================================================================
-- ALTER TABLE t_prmp DROP COLUMN "ID_LOCALITE";
