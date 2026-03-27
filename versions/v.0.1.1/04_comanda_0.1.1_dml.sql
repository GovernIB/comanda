-- Changeset db/changelog/changes/conf/0.1.1/0.1.1_con_005.yaml::conf-stats-enabled-1::limit
-- Afegir paràmetre per activar o ocultar les funcionalitats d'estadístiques
INSERT INTO COM_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, DESCRIPCIO, VALOR, EDITABLE) VALUES ('Estadístiques', 'General', 'BOOLEAN', 'es.caib.comanda.stats.enabled', 'Estadístiques actives', 'Les funcionalitats relacionades amb les estadístiques seran visibles.', 'false', 1);
