-- Changeset db/changelog/changes/00_avis_table_update.yaml::avis-upd-table-1::limit
ALTER TABLE com_avis ADD url VARCHAR2(255);
ALTER TABLE com_avis ADD responsable VARCHAR2(128);
ALTER TABLE com_avis ADD grup VARCHAR2(128);

CREATE TABLE com_avis_usuari (avis_id NUMBER(38, 0) NOT NULL, usuari VARCHAR2(255));
ALTER TABLE com_avis_usuari ADD CONSTRAINT com_avisusr_avis_fk FOREIGN KEY (avis_id) REFERENCES com_avis (id) ON DELETE CASCADE;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_avis_usuari TO WWW_COMANDA;

CREATE TABLE com_avis_grup (avis_id NUMBER(38, 0) NOT NULL, grup VARCHAR2(255));
ALTER TABLE com_avis_grup ADD CONSTRAINT com_avisgrp_avis_fk FOREIGN KEY (avis_id) REFERENCES com_avis (id) ON DELETE CASCADE;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_avis_grup TO WWW_COMANDA;

-- Changeset db/changelog/changes/00_avis_table_update.yaml::avis-upd-table-2::limit
CREATE INDEX com_avis_lastmod_date_i ON com_avis(lastmod_date);

-- Changeset db/changelog/changes/borrat_index.yaml::tasca-change-03::generated
CREATE INDEX com_tasca_datainici_i ON com_tasca(data_inici);
CREATE INDEX com_tasca_datafi_i ON com_tasca(data_fi);
CREATE INDEX com_tasca_datainici_datafi_i ON com_tasca(data_inici, data_fi);