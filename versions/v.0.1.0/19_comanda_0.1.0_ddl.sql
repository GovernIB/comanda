-- Changeset db/changelog/changes/2473.yaml::salut-change-2473-1::Limit
ALTER TABLE com_salut_integracio MODIFY codi VARCHAR2(32 CHAR);
ALTER TABLE com_salut_subsistema MODIFY codi VARCHAR2(64 CHAR);

-- Changeset db/changelog/changes/conf_2473.yaml::conf-change-2473-1::Limit
ALTER TABLE com_integracio MODIFY nom VARCHAR2(255 CHAR);
ALTER TABLE com_app_subsistema MODIFY codi VARCHAR2(64 CHAR);
ALTER TABLE com_app_subsistema MODIFY nom VARCHAR2(255 CHAR);
ALTER TABLE com_app_context MODIFY nom VARCHAR2(255 CHAR);