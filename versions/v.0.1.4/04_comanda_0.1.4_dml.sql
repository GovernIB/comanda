-- Changeset db/changelog/changes/conf/0.1.4/0.1.4_con_001.yaml::con-change-0.1.4-001::limit
-- Afegir paràmetre per limitar el nombre màxim de resultats als selects
INSERT INTO com_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, DESCRIPCIO, VALOR, EDITABLE) VALUES ('General', 'Configuració', 'NUMERIC', 'es.caib.comanda.monitor.general.max.result.selects', 'Màxim de resultats als selectors', 'Nombre màxim d''elements que es mostren als selectors. En canviar el valor, és necessari refrescar la pàgina.', '20', 1);
