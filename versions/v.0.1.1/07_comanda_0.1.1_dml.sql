-- Changeset db/changelog/changes/conf/0.1.1/0.1.1_con_007.yaml::conf-alarma-recovery-control-1::limit
-- Afegir paràmetres de frescor i estabilització per al tancament d'alarmes
INSERT INTO COM_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, DESCRIPCIO, VALOR, EDITABLE) VALUES ('Alarmes', 'Recuperació', 'NUMERIC', 'es.caib.comanda.alarma.salut.freshness.seconds', 'Màxim d''antiguitat del darrer registre de salut', 'Nombre màxim de segons que pot tenir el darrer registre de salut per considerar-lo vigent. Si és més antic, no s''activa ni es finalitza cap alarma a partir d''aquesta mostra.', '120', 1);
INSERT INTO COM_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, DESCRIPCIO, VALOR, EDITABLE) VALUES ('Alarmes', 'Recuperació', 'NUMERIC', 'es.caib.comanda.alarma.recovery.stability.seconds', 'Temps d''estabilitat per finalitzar l''alarma', 'Nombre de segons durant els quals la recuperació s''ha de mantenir estable abans de finalitzar una alarma activa. Evita tancaments per recuperacions puntuals o mostres transitòries.', '180', 1);

-- Changeset db/changelog/changes/conf/0.1.1/0.1.1_con_008.yaml::conf-salut-hist-retencio-1::codex
-- Afegir paràmetre de retenció de l'històric d'estat de salut
INSERT INTO COM_PARAMETRE (GRUP, SUBGRUP, TIPUS, CODI, NOM, DESCRIPCIO, VALOR, EDITABLE) VALUES ('Salut', 'Històric', 'NUMERIC', 'es.caib.comanda.salut.hist.retencio.dies', 'Dies de retenció de l''històric d''estat', 'Nombre de dies que s''ha de conservar l''històric de canvis d''estat de salut per entorn d''aplicació abans d''eliminar-lo automàticament.', '30', 1);
