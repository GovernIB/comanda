ALTER TABLE com_tasca_usuari DROP PRIMARY KEY DROP INDEX;
ALTER TABLE com_tasca_usuari ADD CONSTRAINT com_tasca_usuari_pk PRIMARY KEY (tasca_id, usuari);

ALTER TABLE com_tasca_grup DROP PRIMARY KEY DROP INDEX;
ALTER TABLE com_tasca_grup ADD CONSTRAINT com_tasca_grup_pk PRIMARY KEY (tasca_id, grup);