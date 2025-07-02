-- Oracle DDL Script for Comanda Project
-- Generated from Liquibase YAML files

-- =============================================
-- Module: comanda-ms-configuracio
-- =============================================

-- Table: com_entorn
CREATE TABLE com_entorn (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codi VARCHAR2(16 CHAR) NOT NULL,
    nom VARCHAR2(255 CHAR)
);

-- Table: com_app
CREATE TABLE com_app (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codi VARCHAR2(16 CHAR) NOT NULL,
    nom VARCHAR2(100 CHAR) NOT NULL,
    descripcio VARCHAR2(1000 CHAR),
    activa NUMBER(1),
    logo BLOB,
    created_by VARCHAR2(64 CHAR) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    lastmod_by VARCHAR2(64 CHAR),
    lastmod_date TIMESTAMP,
    v NUMBER(38)
);

-- Table: com_entorn_app
CREATE TABLE com_entorn_app (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    entorn_id NUMBER(38) NOT NULL,
    app_id NUMBER(38) NOT NULL,
    info_url VARCHAR2(200 CHAR) NOT NULL,
    info_interval NUMBER(10) NOT NULL,
    info_data TIMESTAMP,
    salut_url VARCHAR2(200 CHAR) NOT NULL,
    salut_interval NUMBER(10) NOT NULL,
    estadistica_info_url VARCHAR2(200 CHAR),
    estadistica_url VARCHAR2(200 CHAR),
    estadistica_cron VARCHAR2(40 CHAR),
    versio VARCHAR2(10 CHAR),
    activa NUMBER(1),
    created_by VARCHAR2(64 CHAR) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    lastmod_by VARCHAR2(64 CHAR),
    lastmod_date TIMESTAMP,
    v NUMBER(38)
);

-- Table: com_integracio
CREATE TABLE com_integracio (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codi VARCHAR2(16 CHAR) NOT NULL,
    nom VARCHAR2(100 CHAR) NOT NULL,
    logo BLOB,
    created_by VARCHAR2(64 CHAR) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    lastmod_by VARCHAR2(64 CHAR),
    lastmod_date TIMESTAMP,
    v NUMBER(38)
);

-- Table: com_app_integracio
CREATE TABLE com_app_integracio (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    integracio_id NUMBER(38) NOT NULL,
    entorn_app_id NUMBER(38) NOT NULL,
    activa NUMBER(1),
    created_by VARCHAR2(64 CHAR) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    lastmod_by VARCHAR2(64 CHAR),
    lastmod_date TIMESTAMP,
    v NUMBER(38)
);

-- Table: com_app_subsistema
CREATE TABLE com_app_subsistema (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codi VARCHAR2(16 CHAR) NOT NULL,
    nom VARCHAR2(100 CHAR) NOT NULL,
    actiu NUMBER(1),
    entorn_app_id NUMBER(38) NOT NULL,
    created_by VARCHAR2(64 CHAR) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    lastmod_by VARCHAR2(64 CHAR),
    lastmod_date TIMESTAMP,
    v NUMBER(38)
);

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

-- Grant permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON com_entorn TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_app TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_entorn_app TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_integracio TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_app_integracio TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_app_subsistema TO WWW_COMANDA;

-- =============================================
-- Module: comanda-ms-salut
-- =============================================

-- Table: com_salut
CREATE TABLE com_salut (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    entorn_app_id NUMBER(38) NOT NULL,
    data TIMESTAMP NOT NULL,
    app_estat VARCHAR2(20 CHAR) NOT NULL,
    app_latencia NUMBER(10),
    bd_estat VARCHAR2(20 CHAR),
    bd_latencia NUMBER(10)
);

-- Table: com_salut_integracio
CREATE TABLE com_salut_integracio (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codi VARCHAR2(16 CHAR) NOT NULL,
    estat VARCHAR2(20 CHAR) NOT NULL,
    latencia NUMBER(10),
    total_ok NUMBER(10) NOT NULL,
    total_error NUMBER(10) NOT NULL,
    salut_id NUMBER(38) NOT NULL
);

-- Table: com_salut_subsistema
CREATE TABLE com_salut_subsistema (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codi VARCHAR2(16 CHAR) NOT NULL,
    estat VARCHAR2(20 CHAR) NOT NULL,
    latencia NUMBER(10),
    total_ok NUMBER(10) NOT NULL,
    total_error NUMBER(10) NOT NULL,
    salut_id NUMBER(38) NOT NULL
);

-- Table: com_salut_missatge
CREATE TABLE com_salut_missatge (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data TIMESTAMP NOT NULL,
    nivell VARCHAR2(10 CHAR) NOT NULL,
    missatge VARCHAR2(2048 CHAR),
    salut_id NUMBER(38) NOT NULL
);

-- Table: com_salut_detall
CREATE TABLE com_salut_detall (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codi VARCHAR2(10 CHAR) NOT NULL,
    nom VARCHAR2(100 CHAR) NOT NULL,
    valor VARCHAR2(1024 CHAR),
    salut_id NUMBER(38) NOT NULL
);

-- Constraints
-- Foreign Keys
ALTER TABLE com_salut_integracio ADD CONSTRAINT com_salutint_salut_fk FOREIGN KEY (salut_id) REFERENCES com_salut(id);
ALTER TABLE com_salut_subsistema ADD CONSTRAINT com_salutsub_salut_fk FOREIGN KEY (salut_id) REFERENCES com_salut(id);
ALTER TABLE com_salut_missatge ADD CONSTRAINT com_salutmsg_salut_fk FOREIGN KEY (salut_id) REFERENCES com_salut(id);
ALTER TABLE com_salut_detall ADD CONSTRAINT com_salutdet_salut_fk FOREIGN KEY (salut_id) REFERENCES com_salut(id);

-- Grant permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON com_salut TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_salut_integracio TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_salut_subsistema TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_salut_missatge TO WWW_COMANDA;
GRANT SELECT, UPDATE, INSERT, DELETE ON com_salut_detall TO WWW_COMANDA;

-- =============================================
-- Module: comanda-ms-estadistica
-- =============================================

-- Table: com_est_dimensio
CREATE TABLE com_est_dimensio (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codi VARCHAR2(16 CHAR) NOT NULL,
    nom VARCHAR2(64 CHAR) NOT NULL,
    descripcio VARCHAR2(1024 CHAR),
    entorn_app_id NUMBER(38) NOT NULL
);

-- Table: com_est_dimensio_valor
CREATE TABLE com_est_dimensio_valor (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    valor VARCHAR2(255 CHAR),
    dimensio_id NUMBER(38) NOT NULL
);

-- Table: com_est_indicador
CREATE TABLE com_est_indicador (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codi VARCHAR2(16 CHAR) NOT NULL,
    nom VARCHAR2(64 CHAR) NOT NULL,
    descripcio VARCHAR2(1024 CHAR),
    entorn_app_id NUMBER(38) NOT NULL,
    format VARCHAR2(64 CHAR)
);

-- Table: com_est_temps
CREATE TABLE com_est_temps (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data DATE NOT NULL,
    anualitat NUMBER(10) NOT NULL,
    trimestre NUMBER(10) NOT NULL,
    mes NUMBER(10) NOT NULL,
    setmana NUMBER(10) NOT NULL,
    dia NUMBER(10) NOT NULL,
    dia_setmana VARCHAR2(2 CHAR) NOT NULL
);

-- Table: com_est_fet
CREATE TABLE com_est_fet (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    temps_id NUMBER(38) NOT NULL,
    dimensions_json VARCHAR2(4000 CHAR),
    indicadors_json VARCHAR2(4000 CHAR),
    entorn_app_id NUMBER(38) NOT NULL
);

-- Table: com_est_widget
CREATE TABLE com_est_widget (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    widget_type VARCHAR2(16 CHAR) NOT NULL,
    titol VARCHAR2(64 CHAR) NOT NULL,
    descripcio VARCHAR2(1024 CHAR),
    app_id NUMBER(38) NOT NULL,
    periode_mode VARCHAR2(32 CHAR),
    preset_periode VARCHAR2(32 CHAR),
    preset_count NUMBER,
    relatiu_punt_referencia VARCHAR2(32 CHAR),
    relatiu_count NUMBER,
    relatiu_unitat VARCHAR2(32 CHAR),
    relatiu_alineacio VARCHAR2(32 CHAR),
    absolut_tipus VARCHAR2(32 CHAR),
    absolut_data_inici DATE,
    absolut_data_fi DATE,
    absolut_any_referencia VARCHAR2(32 CHAR),
    absolut_any_valor NUMBER,
    absolut_periode_unitat VARCHAR2(32 CHAR),
    absolut_periode_inici NUMBER,
    absolut_periode_fi NUMBER,
    indicador_id NUMBER(38),
    tipus_grafic VARCHAR2(16 CHAR),
    tipus_dades VARCHAR2(32 CHAR),
    tipus_valors VARCHAR2(16 CHAR),
    temps_agrupacio VARCHAR2(16 CHAR),
    descomposicio_dimensio_id NUMBER(38),
    agrupar_dimensio_descomposicio NUMBER(1),
    llegenda_x VARCHAR2(64 CHAR),
    llegenda_y VARCHAR2(64 CHAR),
    unitat VARCHAR2(64 CHAR),
    agrupament_dimensio_id NUMBER(38),
    agrupament_dimensio_titol VARCHAR2(64 CHAR),
    comparar_periode_anterior NUMBER(1) DEFAULT 0,
    atributs_visuals VARCHAR2(4000 CHAR),
    created_by VARCHAR2(64 CHAR) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    lastmod_by VARCHAR2(64 CHAR),
    lastmod_date TIMESTAMP,
    v NUMBER(38)
);

-- Table: com_est_widget_dim_valor
CREATE TABLE com_est_widget_dim_valor (
    widget_id NUMBER(38) NOT NULL,
    dimensio_valor_id NUMBER(38) NOT NULL
);

-- Table: com_est_indicador_table
CREATE TABLE com_est_indicador_table (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    indicador_id NUMBER(38) NOT NULL,
    widget_id NUMBER(38) NOT NULL,
    agregacio VARCHAR2(16 CHAR),
    unitat_agregacio VARCHAR2(16 CHAR),
    titol VARCHAR2(64 CHAR),
    created_by VARCHAR2(64 CHAR) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    lastmod_by VARCHAR2(64 CHAR),
    lastmod_date TIMESTAMP,
    v NUMBER(38)
);

-- Table: com_est_dashboard
CREATE TABLE com_est_dashboard (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titol VARCHAR2(64 CHAR) NOT NULL,
    descripcio VARCHAR2(1024 CHAR),
    created_by VARCHAR2(64 CHAR) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    lastmod_by VARCHAR2(64 CHAR),
    lastmod_date TIMESTAMP,
    v NUMBER(38)
);

-- Table: com_est_dashboard_item
CREATE TABLE com_est_dashboard_item (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pos_x NUMBER(10) NOT NULL,
    pos_y NUMBER(10) NOT NULL,
    width NUMBER(10) NOT NULL,
    height NUMBER(10) NOT NULL,
    dashboard_id NUMBER(38) NOT NULL,
    widget_id NUMBER(38) NOT NULL,
    entorn_id NUMBER(38) NOT NULL,
    atributs_visuals VARCHAR2(4000 CHAR),
    created_by VARCHAR2(64 CHAR) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    lastmod_by VARCHAR2(64 CHAR),
    lastmod_date TIMESTAMP,
    v NUMBER(38)
);

-- Table: com_est_dashboard_titol
CREATE TABLE com_est_dashboard_titol (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dashboard_id NUMBER(38) NOT NULL,
    titol VARCHAR2(255 CHAR) NOT NULL,
    subtitol VARCHAR2(255 CHAR),
    pos_x NUMBER(10) NOT NULL,
    pos_y NUMBER(10) NOT NULL,
    width NUMBER(10) NOT NULL,
    height NUMBER(10) NOT NULL,
    color_titol VARCHAR2(8 CHAR),
    mida_font_titol NUMBER(10),
    color_subtitol VARCHAR2(8 CHAR),
    mida_font_subtitol NUMBER(10),
    color_fons VARCHAR2(8 CHAR),
    mostrar_vora NUMBER(1),
    color_vora VARCHAR2(8 CHAR),
    ample_vora NUMBER(10),
    created_by VARCHAR2(50 CHAR),
    created_date TIMESTAMP NOT NULL,
    lastmod_by VARCHAR2(50 CHAR),
    lastmod_date TIMESTAMP,
    v NUMBER(38)
);

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

-- Table: com_monitor
CREATE TABLE com_monitor (
    id NUMBER(38) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    entorn_app_id NUMBER(38),
    codi VARCHAR2(16 CHAR) NOT NULL,
    tipus VARCHAR2(255 CHAR) NOT NULL,
    data TIMESTAMP NOT NULL,
    url VARCHAR2(255 CHAR),
    operacio VARCHAR2(255 CHAR) NOT NULL,
    temps_resposta NUMBER(38),
    estat VARCHAR2(255 CHAR) NOT NULL,
    codi_usuari VARCHAR2(64 CHAR),
    error_descripcio VARCHAR2(1024 CHAR),
    excepcio_msg VARCHAR2(1024 CHAR),
    excepcio_stacktrace VARCHAR2(4000 CHAR)
);

-- Grant permissions
GRANT SELECT, UPDATE, INSERT, DELETE ON com_monitor TO WWW_COMANDA;
