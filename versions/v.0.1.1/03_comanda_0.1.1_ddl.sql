-- Changeset db/changelog/changes/alarmes/0.1.1/0.1.1_alarmes_001.yaml::alarmes-change-24-1::limit
-- Eliminar la constraint NotNull de la columna 'nom', ja que no es considera imprescindible
ALTER TABLE com_alarma_config MODIFY nom NULL;

-- Changeset db/changelog/changes/usu/0.1.1/0.1.1_usu_002.yaml::usu-change-51-02::limit
ALTER TABLE com_usuari ADD estil_menu VARCHAR2(16 CHAR) DEFAULT 'TEMA' NOT NULL;

-- Changeset db/changelog/changes/usu/0.1.1/0.1.1_usu_003.yaml::usu-change-51-03::limit
ALTER TABLE com_usuari ADD tema_aplicacio VARCHAR2(16 CHAR);

-- Changeset db/changelog/changes/usu/0.1.1/0.1.1_usu_004.yaml::usu-change-51-04::limit
ALTER TABLE com_usuari DROP COLUMN tema_obscur;