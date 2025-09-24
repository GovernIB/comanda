-- Millorar informació de salut
ALTER TABLE com_salut_subsistema MODIFY total_tempsmig NOT NULL;
ALTER TABLE com_salut_subsistema MODIFY pet_ok_ultperiode NOT NULL;
ALTER TABLE com_salut_subsistema MODIFY pet_error_ultperiode NOT NULL;
ALTER TABLE com_salut_subsistema MODIFY temps_mig_ultperiode NOT NULL;

ALTER TABLE com_salut_integracio MODIFY total_tempsmig NOT NULL;
ALTER TABLE com_salut_integracio MODIFY pet_ok_ultperiode NOT NULL;
ALTER TABLE com_salut_integracio MODIFY pet_error_ultperiode NOT NULL;
ALTER TABLE com_salut_integracio MODIFY temps_mig_ultperiode NOT NULL;

ALTER TABLE com_salut MODIFY peticio_error NOT NULL;