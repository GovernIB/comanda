-- Oracle DDL Script for Comanda Project
-- Generated from Liquibase YAML files

-- =============================================
-- Module: comanda-ms-configuracio
-- =============================================

-- Constraints
-- Foreign Keys
ALTER TABLE com_entorn_app ADD CONSTRAINT com_entorn_app_entorn_fk FOREIGN KEY (entorn_id) REFERENCES com_entorn(id);
ALTER TABLE com_entorn_app ADD CONSTRAINT com_entorn_app_app_fk FOREIGN KEY (app_id) REFERENCES com_app(id);
ALTER TABLE com_app_integracio ADD CONSTRAINT com_app_integ_entorn_app_fk FOREIGN KEY (entorn_app_id) REFERENCES com_entorn_app(id);
ALTER TABLE com_app_integracio ADD CONSTRAINT com_app_integ_integracio_fk FOREIGN KEY (integracio_id) REFERENCES com_integracio(id);
ALTER TABLE com_app_subsistema ADD CONSTRAINT com_app_subs_entorn_app_fk FOREIGN KEY (entorn_app_id) REFERENCES com_entorn_app(id);

-- Unique Constraints
ALTER TABLE com_entorn ADD CONSTRAINT com_entorn_codi_uk UNIQUE (codi);
ALTER TABLE com_entorn_app ADD CONSTRAINT com_entorn_app_entapp_uk UNIQUE (entorn_id, app_id);
ALTER TABLE com_app ADD CONSTRAINT com_app_codi_uk UNIQUE (codi);
ALTER TABLE com_app_integracio ADD CONSTRAINT com_app_integracio_app_int_uk UNIQUE (entorn_app_id, integracio_id);
ALTER TABLE com_app_subsistema ADD CONSTRAINT com_app_subsistema_appcodi_uk UNIQUE (entorn_app_id, codi);
ALTER TABLE com_integracio ADD CONSTRAINT com_integracio_codi_uk UNIQUE (codi);

-- Lobs
-- ALTER TABLE com_app MOVE LOB(logo) STORE AS com_app_logo_lob(TABLESPACE comanda_lob INDEX com_app_logo_lob_i);
-- ALTER TABLE com_integracio MOVE LOB(logo) STORE AS com_integracio_logo_lob(TABLESPACE comanda_lob INDEX com_integracio_logo_lob_i);

-- =============================================
-- Module: comanda-ms-salut
-- =============================================

-- Constraints
-- Foreign Keys
ALTER TABLE com_salut_integracio ADD CONSTRAINT com_salutint_salut_fk FOREIGN KEY (salut_id) REFERENCES com_salut(id);
ALTER TABLE com_salut_subsistema ADD CONSTRAINT com_salutsub_salut_fk FOREIGN KEY (salut_id) REFERENCES com_salut(id);
ALTER TABLE com_salut_missatge ADD CONSTRAINT com_salutmsg_salut_fk FOREIGN KEY (salut_id) REFERENCES com_salut(id);
ALTER TABLE com_salut_detall ADD CONSTRAINT com_salutdet_salut_fk FOREIGN KEY (salut_id) REFERENCES com_salut(id);

-- =============================================
-- Module: comanda-ms-estadistica
-- =============================================

-- Constraints
-- Unique Constraints
ALTER TABLE com_est_dimensio ADD CONSTRAINT com_dim_nom_uk UNIQUE (nom, entorn_app_id);
ALTER TABLE com_est_indicador ADD CONSTRAINT com_ind_nom_uk UNIQUE (nom, entorn_app_id);
ALTER TABLE com_est_widget ADD CONSTRAINT com_widget_titol_uk UNIQUE (titol, app_id);
ALTER TABLE com_est_dashboard ADD CONSTRAINT com_dashboard_titol_uk UNIQUE (titol);

-- Foreign Keys
ALTER TABLE com_est_dimensio_valor ADD CONSTRAINT com_dim_valor_dim_fk FOREIGN KEY (dimensio_id) REFERENCES com_est_dimensio(id);
ALTER TABLE com_est_fet ADD CONSTRAINT com_fet_temps_fk FOREIGN KEY (temps_id) REFERENCES com_est_temps(id);
ALTER TABLE com_est_widget ADD CONSTRAINT com_widget_indicador_fk FOREIGN KEY (indicador_id) REFERENCES com_est_indicador(id);
ALTER TABLE com_est_widget ADD CONSTRAINT com_widget_taula_dimensio_fk FOREIGN KEY (agrupament_dimensio_id) REFERENCES com_est_dimensio(id);
ALTER TABLE com_est_widget ADD CONSTRAINT com_widget_descomposicio_fk FOREIGN KEY (descomposicio_dimensio_id) REFERENCES com_est_dimensio(id);
ALTER TABLE com_est_widget_dim_valor ADD CONSTRAINT com_widget_dvalor_widget_fk FOREIGN KEY (widget_id) REFERENCES com_est_widget(id);
ALTER TABLE com_est_widget_dim_valor ADD CONSTRAINT com_widget_dvalor_dvalor_fk FOREIGN KEY (dimensio_valor_id) REFERENCES com_est_dimensio_valor(id);
ALTER TABLE com_est_indicador_table ADD CONSTRAINT com_indtab_indicador_fk FOREIGN KEY (indicador_id) REFERENCES com_est_indicador(id);
ALTER TABLE com_est_indicador_table ADD CONSTRAINT com_indtab_widget_fk FOREIGN KEY (widget_id) REFERENCES com_est_widget(id);
ALTER TABLE com_est_dashboard_item ADD CONSTRAINT com_dboard_item_dboard_fk FOREIGN KEY (dashboard_id) REFERENCES com_est_dashboard(id);
ALTER TABLE com_est_dashboard_item ADD CONSTRAINT com_dboard_item_widget_fk FOREIGN KEY (widget_id) REFERENCES com_est_widget(id);
ALTER TABLE com_est_dashboard_titol ADD CONSTRAINT com_dboard_titol_dboard_fk FOREIGN KEY (dashboard_id) REFERENCES com_est_dashboard(id);

-- Indexes
CREATE INDEX com_est_fet_temps_i ON com_est_fet(temps_id);
CREATE INDEX com_est_fet_entornapp_i ON com_est_fet(entorn_app_id);
CREATE INDEX com_est_temps_data_i ON com_est_temps(data);
CREATE INDEX com_est_temps_anualitat_i ON com_est_temps(anualitat);
CREATE INDEX com_est_temps_trimestre_i ON com_est_temps(anualitat, trimestre);
CREATE INDEX com_est_temps_mes_i ON com_est_temps(anualitat, mes);
CREATE INDEX com_est_temps_setmana_i ON com_est_temps(anualitat, setmana);
CREATE INDEX com_est_temps_dia_i ON com_est_temps(anualitat, mes, dia);
CREATE INDEX com_est_dimensio_valor_dim_i ON com_est_dimensio_valor(dimensio_id);
