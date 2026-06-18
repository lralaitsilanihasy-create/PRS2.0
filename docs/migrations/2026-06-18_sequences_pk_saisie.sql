-- Migration manuelle (PostgreSQL / pgAdmin) — pas de Flyway (cf. CLAUDE.md).
-- Contexte : auto-génération des PK dossier/PPM/marché par SÉQUENCE serveur (Voie B, NON-IDENTITY).
-- Le serveur attribue toujours l'id depuis ces séquences et ignore tout id envoyé par le client.
--
-- ⚠️ DETTE DOCUMENTÉE : choix volontaire d'une séquence applicative plutôt que IDENTITY, pour éviter
-- la refonte massive des seeds de test (id explicites) sur 3 tables très centrales. Migration vers
-- IDENTITY possible ultérieurement.
--
-- Max d'id VÉRIFIÉS au moment de la migration : t_dossier=9, t_ppm=9, t_marche=11.
-- Chaque séquence démarre TRÈS au-dessus du max de SA table ET du range d'ids de test (≤ ~7004) → zéro collision.

CREATE SEQUENCE IF NOT EXISTS seq_dossier START WITH 100001;   -- > max t_dossier (9)
CREATE SEQUENCE IF NOT EXISTS seq_ppm     START WITH 200001;   -- > max t_ppm (9)
CREATE SEQUENCE IF NOT EXISTS seq_marche  START WITH 300001;   -- > max t_marche (11)
