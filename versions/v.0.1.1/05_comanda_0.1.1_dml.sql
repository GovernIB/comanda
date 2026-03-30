-- Changeset db/changelog/changes/conf/0.1.1/0.1.1_con_006.yaml::conf-alarma-mail-from-1::limit
-- Afegir paràmetres de remitent per als correus d'alarmes
INSERT INTO COM_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, DESCRIPCIO, VALOR, EDITABLE) VALUES ('Alarmes', 'Correu', 'TEXT', 'es.caib.comanda.alarma.mail.from.address', 'Adreça del remitent dels correus d''alarma', 'Adreça de correu electrònic usada com a remitent als correus d''alarma.', 'comanda@caib.es', 1);
INSERT INTO COM_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, DESCRIPCIO, VALOR, EDITABLE) VALUES ('Alarmes', 'Correu', 'TEXT', 'es.caib.comanda.alarma.mail.from.name', 'Nom del remitent dels correus d''alarma', 'Nom visible del remitent als correus d''alarma.', 'Comanda', 1);
