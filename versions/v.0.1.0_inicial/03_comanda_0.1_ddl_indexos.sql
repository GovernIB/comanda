-- =============================================
-- Module: comanda-ms-configuracio
-- =============================================

-- Indexes
CREATE INDEX com_app_context_entorn_fk_i ON com_app_context (entorn_app_id);
CREATE INDEX com_app_ctx_manual_app_fk_i ON com_app_ctx_manual (app_ctx_id);

-- =============================================
-- Module: comanda-ms-estadistica
-- =============================================

-- Indexes
CREATE INDEX com_est_fet_temps_i ON com_est_fet(temps_id);
CREATE INDEX com_est_fet_entornapp_i ON com_est_fet(entorn_app_id);
--CREATE INDEX com_est_temps_data_i ON com_est_temps(data);
CREATE INDEX com_est_temps_anualitat_i ON com_est_temps(anualitat);
CREATE INDEX com_est_temps_trimestre_i ON com_est_temps(anualitat, trimestre);
CREATE INDEX com_est_temps_mes_i ON com_est_temps(anualitat, mes);
CREATE INDEX com_est_temps_setmana_i ON com_est_temps(anualitat, setmana);
CREATE INDEX com_est_temps_dia_i ON com_est_temps(anualitat, mes, dia);
CREATE INDEX com_est_dimensio_valor_dim_i ON com_est_dimensio_valor(dimensio_id);
CREATE INDEX com_est_dim_valor_dim_agr_idx ON com_est_dimensio_valor(dimensio_id, agrupable);
CREATE INDEX com_est_dim_entorn_idx ON com_est_dimensio(entorn_app_id);

-- =============================================
-- Module: comanda-ms-monitor
-- =============================================

-- Indexes
CREATE INDEX com_mon_codi_i ON com_monitor (codi);
CREATE INDEX com_mon_usuari_i ON com_monitor (codi_usuari);
CREATE INDEX com_mon_data_i ON com_monitor (data);
CREATE INDEX com_mon_estat_i ON com_monitor (estat);

-- =============================================
-- Module: comanda-ms-salut
-- =============================================

-- Indexes
CREATE INDEX com_salut_tipus_registre_id ON com_salut(tipus_registre);
CREATE INDEX com_salut_entorn_data_id ON com_salut(entorn_app_id, data);

-- =============================================
-- Module: comanda-ms-avisos
-- =============================================

-- Indexes
CREATE INDEX com_avis_lastmod_date_i ON com_avis(lastmod_date);

-- =============================================
-- Module: comanda-ms-tasques
-- =============================================

-- Indexes
CREATE INDEX com_tasca_datainici_i ON com_tasca(data_inici);
CREATE INDEX com_tasca_datafi_i ON com_tasca(data_fi);
CREATE INDEX com_tasca_datainici_datafi_i ON com_tasca(data_inici, data_fi);
