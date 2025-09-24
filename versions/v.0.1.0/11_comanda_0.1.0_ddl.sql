-- Millorar informació de salut
ALTER TABLE com_salut_subsistema ADD total_tempsmig INTEGER;
ALTER TABLE com_salut_subsistema ADD pet_ok_ultperiode INTEGER;
ALTER TABLE com_salut_subsistema ADD pet_error_ultperiode INTEGER;
ALTER TABLE com_salut_subsistema ADD temps_mig_ultperiode INTEGER;

ALTER TABLE com_salut_integracio ADD total_tempsmig INTEGER;
ALTER TABLE com_salut_integracio ADD pet_ok_ultperiode INTEGER;
ALTER TABLE com_salut_integracio ADD pet_error_ultperiode INTEGER;
ALTER TABLE com_salut_integracio ADD temps_mig_ultperiode INTEGER;
ALTER TABLE com_salut_integracio ADD endpoint VARCHAR2(255 CHAR);
ALTER TABLE com_salut_integracio ADD pare_id NUMBER(38, 0);
ALTER TABLE com_salut_integracio ADD CONSTRAINT com_salutint_pare_fk FOREIGN KEY (pare_id) REFERENCES com_salut_integracio (id);

ALTER TABLE com_salut ADD peticio_error NUMBER(1);