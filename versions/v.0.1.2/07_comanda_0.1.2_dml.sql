-- Changeset db/changelog/changes/conf/0.1.2/0.1.2_con_004.yaml::con-change-0.1.2.004-1::limit
-- Afegir paràmetre per activar logs detallats de comprovació i enviament de correus d'alarmes
INSERT INTO com_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, DESCRIPCIO, VALOR, EDITABLE) VALUES ('Alarmes', 'Log', 'BOOLEAN', 'es.caib.comanda.alarma.log.activacio', 'Activar logs detallats d''alarmes', 'Activa logs de nivell INFO per a la comprovació d''alarmes i l''enviament de correus. Permet diagnosticar problemes d''activació i notificació.', 'true', 1);
