-- ACL
CREATE INDEX com_acloid_parent_i ON com_acl_object_identity(parent_object);
CREATE INDEX com_aclentry_sid_i ON com_acl_entry(sid);
CREATE INDEX com_acloid_ownersid_i ON com_acl_object_identity(owner_sid);

-- ALARMES
CREATE INDEX com_alarma_alarmcfg_i ON com_alarma(alarma_config_id);
CREATE INDEX com_alarmausu_alarma_i ON com_alarma_usuari(alarma_id);

-- AVISOS
CREATE INDEX com_avisusr_avis_i ON com_avis_usuari(avis_id);
CREATE INDEX com_avisgrp_avis_i ON com_avis_grup(avis_id);

-- CONFIGURACIO
CREATE INDEX com_entapp_app_i ON com_entorn_app(app_id);
CREATE INDEX com_appinteg_integ_i ON com_app_integracio(integracio_id);

-- ESTADISTICA
CREATE INDEX com_estind_compact_i ON com_est_indicador(compactacio_indicador_id);
CREATE INDEX com_estwid_ind_i ON com_est_widget(indicador_id);
CREATE INDEX com_estwid_agrdim_i ON com_est_widget(agrupament_dimensio_id);
CREATE INDEX com_estwid_descdim_i ON com_est_widget(descomposicio_dimensio_id);
CREATE INDEX com_estwdv_wid_i ON com_est_widget_dim_valor(widget_id);
CREATE INDEX com_estwdv_dval_i ON com_est_widget_dim_valor(dimensio_valor_id);
CREATE INDEX com_estindt_ind_i ON com_est_indicador_table(indicador_id);
CREATE INDEX com_estindt_wid_i ON com_est_indicador_table(widget_id);
CREATE INDEX com_estdbi_dsh_i ON com_est_dashboard_item(dashboard_id);
CREATE INDEX com_estdbi_wid_i ON com_est_dashboard_item(widget_id);
CREATE INDEX com_estdbt_dsh_i ON com_est_dashboard_titol(dashboard_id);

-- PERMISOS
CREATE INDEX com_permacc_acc_i ON com_permis_acces(acces_id);
CREATE INDEX com_permis_obj_i ON com_permis(objecte_id);
CREATE INDEX com_permher_hereu_i ON com_permis_hereus(hereu_id);

-- SALUT
CREATE INDEX com_salutint_pare_i ON com_salut_integracio(pare_id);
CREATE INDEX com_salutint_salut_i ON com_salut_integracio(salut_id);
CREATE INDEX com_salutsub_salut_i ON com_salut_subsistema(salut_id);
CREATE INDEX com_salutmsg_salut_i ON com_salut_missatge(salut_id);
CREATE INDEX com_salutdet_salut_i ON com_salut_detall(salut_id);
