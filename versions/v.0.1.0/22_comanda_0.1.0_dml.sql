-- Changeset db/changelog/changes/params_stats_auth.yaml::con-change-params-stats-auth-01::limit
INSERT INTO COM_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, VALOR, EDITABLE) VALUES ('Estadístiques', 'autenticacio', 'TEXT', 'es.caib.comanda.stats.auth.usuari', 'Usuari', '', 0);
INSERT INTO COM_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, VALOR, EDITABLE) VALUES ('Estadístiques', 'autenticacio', 'PASSWORD', 'es.caib.comanda.stats.auth.password', 'Contrasenya', '', 0);
