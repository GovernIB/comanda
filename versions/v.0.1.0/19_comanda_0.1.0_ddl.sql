-- Changeset db/changelog/changes/2473.yaml::salut-change-2473-1::Limit
ALTER TABLE com_salut_integracio MODIFY codi VARCHAR2(32 CHAR);
ALTER TABLE com_salut_subsistema MODIFY codi VARCHAR2(32 CHAR);