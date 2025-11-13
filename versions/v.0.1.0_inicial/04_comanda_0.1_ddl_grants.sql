-- =============================================
-- Module: comanda-ms-configuracio
-- =============================================

-- Grant permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON com_entorn TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_app TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_entorn_app TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_integracio TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_app_integracio TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_app_subsistema TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_app_context TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_app_ctx_manual TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_parametre TO WWW_COMANDA;

-- =============================================
-- Module: comanda-ms-salut
-- =============================================

-- Grant permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON com_salut TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_salut_integracio TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_salut_subsistema TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_salut_missatge TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_salut_detall TO WWW_COMANDA;

-- =============================================
-- Module: comanda-ms-estadistica
-- =============================================

-- Grant permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_dimensio TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_dimensio_valor TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_indicador TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_temps TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_fet TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_widget TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_widget_dim_valor TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_indicador_table TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_dashboard TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_dashboard_item TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_est_dashboard_titol TO WWW_COMANDA;

-- =============================================
-- Module: comanda-ms-monitor
-- =============================================

-- Grant permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON com_monitor TO WWW_COMANDA;

-- =============================================
-- Module: comanda-ms-avisos
-- =============================================

-- Grant permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON com_avis TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_avis_usuari TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_avis_grup TO WWW_COMANDA;

-- =============================================
-- Module: comanda-ms-tasques
-- =============================================

-- Grant permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON com_tasca TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_tasca_usuari TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_tasca_grup TO WWW_COMANDA;

-- =============================================
-- Module: comanda-ms-ususaris
-- =============================================

-- Grant permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON com_usuari TO WWW_COMANDA;

-- =============================================
-- Module: comanda-ms-permisos
-- =============================================

GRANT SELECT, UPDATE, INSERT, DELETE ON com_objecte TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_acces TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_permis TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_permis_acces TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_permis_hereus TO WWW_COMANDA;
