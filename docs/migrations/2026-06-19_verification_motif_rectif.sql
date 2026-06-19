-- Migration manuelle (PostgreSQL / pgAdmin) — pas de Flyway (cf. CLAUDE.md).
-- Contexte : motif de rectification saisi par la PRMP à la resoumission d'un dossier
-- EN_ATTENTE_DECISION_PRMP, enregistré sur la dernière vérification (passage) pour être visible
-- par le vérificateur. Ajout d'une colonne nullable sur t_verification.
ALTER TABLE t_verification ADD COLUMN "MOTIF_RECTIF" varchar(255);
