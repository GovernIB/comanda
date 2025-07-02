-- Oracle DDL Script for Comanda Project
-- Generated from Liquibase YAML files

-- =============================================
-- Module: comanda-ms-estadistica
-- =============================================

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
