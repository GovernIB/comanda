-- Millorar informació de salut
UPDATE com_salut_subsistema SET pet_error_ultperiode = 0, pet_ok_ultperiode = 0, temps_mig_ultperiode = 0, total_tempsmig = 0;
UPDATE com_salut_integracio SET pet_error_ultperiode = 0, pet_ok_ultperiode = 0, temps_mig_ultperiode = 0, total_tempsmig = 0;
UPDATE com_salut SET peticio_error = 0;