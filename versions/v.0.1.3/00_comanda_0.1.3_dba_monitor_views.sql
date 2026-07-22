-- =============================================================================
-- Vistes de monitorització de BD per a l'aplicació COMANDA
-- =============================================================================
-- Executar connectat com a SYSDBA:
--   CONNECT / AS SYSDBA
--
-- Les vistes es creen a l'esquema SYS (propietari: DBA).
-- L'usuari COMANDA rep únicament SELECT sobre les vistes,
-- sense cap permís directe sobre V$* ni DBA_*.
--
-- Passos:
--   1. Crear les vistes (com a SYS)
--   2. Concedir SELECT sobre les vistes a COMANDA
--   3. Crear sinònims a l'esquema COMANDA perquè el codi no necessiti prefix
-- =============================================================================


-- =============================================================================
-- 1. CREACIÓ DE VISTES (executar com a SYS / SYSDBA)
-- =============================================================================

-- Sessions d'usuari actives, agrupades per estat
CREATE OR REPLACE VIEW SYS.comanda_v_sessions AS
SELECT status, COUNT(*) AS quantitat
FROM v$session
WHERE type = 'USER'
GROUP BY status;


-- Estadístiques de segments per a taules COM_
CREATE OR REPLACE VIEW SYS.comanda_v_segment_stats AS
SELECT object_name AS table_name,
       statistic_name,
       value
FROM v$segment_statistics
WHERE object_type = 'TABLE'
  AND object_name LIKE 'COM\_%' ESCAPE '\';


-- Sentències SQL que referencien taules COM_ (per al top SQL)
CREATE OR REPLACE VIEW SYS.comanda_v_top_sql AS
SELECT sql_id,
       elapsed_time,
       executions,
       buffer_gets,
       SUBSTR(sql_text, 1, 300) AS sql_text
FROM v$sqlarea
WHERE executions > 0
  AND elapsed_time > 0
  AND UPPER(sql_text) LIKE '%COM\_%' ESCAPE '\';


-- Estadístiques de caché de buffer necessàries per calcular el hit ratio
CREATE OR REPLACE VIEW SYS.comanda_v_sysstat AS
SELECT name, value
FROM v$sysstat
WHERE name IN ('consistent gets', 'db block gets', 'physical reads');


-- Tablespaces que contenen taules COM_, amb mètriques d'ocupació
CREATE OR REPLACE VIEW SYS.comanda_v_tablespaces AS
SELECT a.tablespace_name,
       ROUND(a.bytes_alloc / 1024 / 1024, 2)                                    AS total_mb,
       ROUND(a.bytes_max   / 1024 / 1024, 2)                                    AS max_mb,
       ROUND((a.bytes_alloc - NVL(b.bytes_free, 0)) / 1024 / 1024, 2)          AS usat_mb,
       ROUND(NVL(b.bytes_free, 0) / 1024 / 1024, 2)                            AS lliure_mb,
       ROUND((a.bytes_alloc - NVL(b.bytes_free, 0)) * 100 / a.bytes_alloc, 2)  AS pct_usat
FROM (SELECT tablespace_name, SUM(bytes) bytes_alloc, SUM(maxbytes) bytes_max
      FROM dba_data_files
      GROUP BY tablespace_name) a
LEFT JOIN (SELECT tablespace_name, SUM(bytes) bytes_free
           FROM dba_free_space
           GROUP BY tablespace_name) b
  ON a.tablespace_name = b.tablespace_name
WHERE a.tablespace_name IN (
    SELECT DISTINCT tablespace_name
    FROM dba_tables
    WHERE owner      = 'COMANDA'
      AND table_name LIKE 'COM\_%' ESCAPE '\'
      AND tablespace_name IS NOT NULL
);


-- Bloquejos actius sobre taules COM_
CREATE OR REPLACE VIEW SYS.comanda_v_bloquejos AS
SELECT s.sid,
       s.serial#                          AS serial_num,
       NVL(s.username, '(intern)')        AS username,
       s.status,
       o.object_name,
       o.object_type,
       DECODE(l.lmode,
              0, 'Cap',       1, 'Null',        2, 'Row-S(SS)',
              3, 'Row-X(SX)', 4, 'Share',       5, 'S/Row-X(SSX)',
              6, 'Exclusive') AS lock_mode,
       DECODE(l.request,
              0, 'Cap',       1, 'Null',        2, 'Row-S(SS)',
              3, 'Row-X(SX)', 4, 'Share',       5, 'S/Row-X(SSX)',
              6, 'Exclusive') AS lock_request,
       l.block                            AS blocking
FROM v$lock l
JOIN v$session s  ON s.sid = l.sid
JOIN dba_objects o ON o.object_id = l.id1
WHERE l.type  = 'TM'
  AND o.owner = 'COMANDA'
  AND o.object_name LIKE 'COM\_%' ESCAPE '\';


-- =============================================================================
-- 2. GRANTS SOBRE LES VISTES A L'USUARI COMANDA
--    Únic permís que necessita COMANDA: SELECT sobre les vistes.
--    No cal cap GRANT directe sobre V$*, DBA_* ni USER_*.
-- =============================================================================

GRANT SELECT ON SYS.comanda_v_sessions      TO WWW_COMANDA; -- comanda;
GRANT SELECT ON SYS.comanda_v_segment_stats TO WWW_COMANDA; -- comanda;
GRANT SELECT ON SYS.comanda_v_top_sql       TO WWW_COMANDA; -- comanda;
GRANT SELECT ON SYS.comanda_v_sysstat       TO WWW_COMANDA; -- comanda;
GRANT SELECT ON SYS.comanda_v_tablespaces   TO WWW_COMANDA; -- comanda;
GRANT SELECT ON SYS.comanda_v_bloquejos     TO WWW_COMANDA; -- comanda;


-- =============================================================================
-- 3. SINÒNIMS A L'ESQUEMA COMANDA
--    Permeten que el codi Java consulti comanda_v_* sense prefix d'esquema.
-- =============================================================================

CREATE OR REPLACE SYNONYM comanda.comanda_v_sessions      FOR SYS.comanda_v_sessions;
CREATE OR REPLACE SYNONYM comanda.comanda_v_segment_stats FOR SYS.comanda_v_segment_stats;
CREATE OR REPLACE SYNONYM comanda.comanda_v_top_sql       FOR SYS.comanda_v_top_sql;
CREATE OR REPLACE SYNONYM comanda.comanda_v_sysstat       FOR SYS.comanda_v_sysstat;
CREATE OR REPLACE SYNONYM comanda.comanda_v_tablespaces   FOR SYS.comanda_v_tablespaces;
CREATE OR REPLACE SYNONYM comanda.comanda_v_bloquejos     FOR SYS.comanda_v_bloquejos;
