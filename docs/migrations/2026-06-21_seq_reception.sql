-- 2026-06-21 — PK auto de t_reception (Voie B, sequence serveur ; le N° saisi par le secretaire disparait).
-- START au-dessus du max actuel d'ID_RECEPTION sur DBPRS20 (verifie a l'application : max = 1002325).
-- 1100001 laisse une marge confortable au-dessus de ce max (ids existants eleves, contrairement
-- aux autres tables). Le serveur alloue toujours la PK via nextval('seq_reception') et ignore tout
-- id envoye par le client.
CREATE SEQUENCE IF NOT EXISTS seq_reception START WITH 1100001;
