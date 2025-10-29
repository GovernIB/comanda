-- Oracle DDL Script for Comanda Project
-- Generated from Liquibase YAML files

-- =============================================
-- Module: comanda-ms-monitor
-- =============================================

-- Indexes
CREATE INDEX com_mon_codi_i ON com_monitor (codi);
CREATE INDEX com_mon_usuari_i ON com_monitor (codi_usuari);
CREATE INDEX com_mon_data_i ON com_monitor (data);
CREATE INDEX com_mon_estat_i ON com_monitor (estat);
