-- Changeset db/changelog/changes/salut/0.1.2/0.1.2_salut_001.yaml::salut-nullable-metrics-1::codex
-- Permet valors null a mètriques opcionals de salut d'integracions i subsistemes
ALTER TABLE com_salut_integracio MODIFY total_ok NULL;
ALTER TABLE com_salut_integracio MODIFY total_error NULL;
ALTER TABLE com_salut_integracio MODIFY total_tempsmig NULL;
ALTER TABLE com_salut_integracio MODIFY pet_ok_ultperiode NULL;
ALTER TABLE com_salut_integracio MODIFY pet_error_ultperiode NULL;
ALTER TABLE com_salut_integracio MODIFY temps_mig_ultperiode NULL;

ALTER TABLE com_salut_subsistema MODIFY total_ok NULL;
ALTER TABLE com_salut_subsistema MODIFY total_error NULL;
ALTER TABLE com_salut_subsistema MODIFY total_tempsmig NULL;
ALTER TABLE com_salut_subsistema MODIFY pet_ok_ultperiode NULL;
ALTER TABLE com_salut_subsistema MODIFY pet_error_ultperiode NULL;
ALTER TABLE com_salut_subsistema MODIFY temps_mig_ultperiode NULL;