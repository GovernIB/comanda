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
