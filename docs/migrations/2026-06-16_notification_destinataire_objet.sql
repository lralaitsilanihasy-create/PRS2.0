-- Migration manuelle (PostgreSQL / pgAdmin) — pas de Flyway (cf. CLAUDE.md).
-- Contexte : unification du destinataire (DESTINATAIRE_REF + DESTINATAIRE_TYPE) et référence
-- d'objet générique (ID_OBJET + TYPE_OBJET) sur t_notification. Colonnes créées automatiquement
-- par Hibernate ddl-auto=update ; ce script ne fait que le BACKFILL des lignes existantes.
--
-- Identifiants en MAJUSCULES → à citer entre guillemets (globally_quoted_identifiers=true).

-- Destinataire unifié : les notifications adressées à un contrôleur portent DESTINATAIRE_IM.
UPDATE t_notification
   SET "DESTINATAIRE_REF" = "DESTINATAIRE_IM",
       "DESTINATAIRE_TYPE" = 'CONTROLEUR'
 WHERE "DESTINATAIRE_IM" IS NOT NULL
   AND "DESTINATAIRE_REF" IS NULL;
-- NB : les notifications PRMP antérieures n'ont qu'un e-mail (DESTINATAIRE_EMAIL) ; elles restent
-- retrouvées via le repli e-mail de la requête « mes notifications » jusqu'à enrichissement.

-- Référence d'objet : les notifications liées à un dossier portent ID_DOSSIER.
UPDATE t_notification
   SET "ID_OBJET" = "ID_DOSSIER",
       "TYPE_OBJET" = 'DOSSIER'
 WHERE "ID_DOSSIER" IS NOT NULL
   AND "TYPE_OBJET" IS NULL;
