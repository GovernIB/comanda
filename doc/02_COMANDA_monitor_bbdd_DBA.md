# COMANDA — Dashboard de monitorització de base de dades

**Document per a l'equip de DBA / infraestructura**

| Control de canvis | | |
| :---- | :---- | :---- |
| **Data** | **Versió** | **Canvis** |
| 06/07/2026 | 1.0 | Versió inicial |

---

## 1. Objecte

COMANDA incorpora un nou **dashboard de monitorització de la base de dades Oracle**, pensat perquè els administradors de l'aplicació puguin detectar, sense necessitat d'accedir directament a la base de dades, problemes habituals de rendiment i salut relacionats amb les seves pròpies dades: sessions actives, eficiència de la memòria cau, ocupació de tablespaces, sentències SQL costoses, bloquejos entre sessions i estat dels índexs.

Aquest document explica:
- **Què** es vol aconseguir amb aquesta funcionalitat.
- **Què** necessita l'aplicació de la base de dades per funcionar.
- **Com** s'ha de concedir aquest accés (sense donar permisos directes sobre objectes del diccionari de dades).
- L'**SQL exacte** que cal executar com a DBA.

L'objectiu de fons és que el DBA no hagi de concedir permisos amplis (`SELECT ANY DICTIONARY`, accés directe a `V$*`/`DBA_*`, etc.) a l'usuari d'aplicació `WWW_COMANDA`, sinó únicament `SELECT` sobre un conjunt reduït de vistes intermèdies, creades i mantingudes pel propi DBA, que exposen exclusivament la informació necessària i acotada a les taules pròpies de COMANDA (prefix `COM_`).

## 2. Què es vol aconseguir

El dashboard (accessible només amb rol d'administrador dins l'aplicació) mostra sis blocs d'informació:

| Bloc | Informació que mostra | Font Oracle |
| :---- | :---- | :---- |
| Resum (KPIs) | Sessions actives/totals, ràtio d'encert de la memòria cau, nombre de taules `COM_`, espai reservat total, bloquejos actius, índexs invàlids | Agregat de la resta de blocs |
| Emmagatzematge | Files i bytes reservats/estimats per taula `COM_`, data de l'última anàlisi | `USER_TABLES`, `USER_SEGMENTS` (objectes propis) |
| Activitat | Lectures físiques/lògiques, esperes de buffer i de fila per taula | Vista `comanda_v_segment_stats` |
| Top SQL | Les 50 sentències més costoses que referencien taules `COM_` | Vista `comanda_v_top_sql` |
| Bloquejos | Sessions amb bloquejos actius sobre objectes `COM_`, indicant si bloquegen altres sessions | Vista `comanda_v_bloquejos` |
| Índexs | Estat, unicitat i mètriques dels índexs de les taules `COM_`, amb opció de reconstruir (`REBUILD`) els marcats com `UNUSABLE` | `USER_INDEXES` (objecte propi) |

Els blocs d'**Emmagatzematge** i **Índexs** es nodreixen exclusivament d'objectes propis de l'esquema `COMANDA` (`USER_TABLES`, `USER_SEGMENTS`, `USER_INDEXES`) i **no requereixen cap permís addicional**. La resta de blocs necessita dades de vistes dinàmiques del sistema (`V$SESSION`, `V$SEGMENT_STATISTICS`, `V$SQLAREA`, `V$SYSSTAT`, `V$LOCK`) i del diccionari de dades (`DBA_DATA_FILES`, `DBA_FREE_SPACE`, `DBA_OBJECTS`, `DBA_TABLES`), a les quals l'usuari d'aplicació no té ni ha de tenir accés directe.

La funcionalitat és **opcional i desactivable**: existeix un paràmetre d'aplicació (`es.caib.comanda.monitor.db.actiu`, grup "Monitor" / subgrup "Base de dades") que amaga tot el dashboard si el DBA no pot o no vol concedir els permisos descrits en aquest document. Per defecte ve activat (`true`); si les vistes no existeixen o no són accessibles, cada bloc ho detecta i es mostra com a "no disponible" en lloc de fallar.

## 3. Què necessitam del DBA

Necessitam que, en cada entorn (SYS/SYSDBA), es creïn **sis vistes de només lectura** propietat de `SYS`, que filtren i acoten la informació de `V$*`/`DBA_*` únicament a les taules `COM_%` propietat de l'usuari `COMANDA`, i que es concedeixi `SELECT` sobre aquestes vistes (mai sobre les vistes `V$*`/`DBA_*` originals) a l'usuari d'aplicació `WWW_COMANDA`.

Resum de les vistes:

| Vista | Origen | Contingut |
| :---- | :---- | :---- |
| `comanda_v_sessions` | `V$SESSION` | Recompte de sessions d'usuari per estat |
| `comanda_v_segment_stats` | `V$SEGMENT_STATISTICS` | Estadístiques d'E/S per taula `COM_` |
| `comanda_v_top_sql` | `V$SQLAREA` | Sentències SQL que referencien taules `COM_`, amb temps i execucions |
| `comanda_v_sysstat` | `V$SYSSTAT` | Comptadors globals per calcular el hit ratio de la memòria cau |
| `comanda_v_tablespaces` | `DBA_DATA_FILES`, `DBA_FREE_SPACE`, `DBA_TABLES` | Ocupació dels tablespaces que contenen taules `COM_` |
| `comanda_v_bloquejos` | `V$LOCK`, `V$SESSION`, `DBA_OBJECTS` | Bloquejos actius (`TM`) sobre taules `COM_` |

No es necessita cap permís addicional més enllà de `SELECT` sobre aquestes sis vistes.

## 4. Com aconseguir-ho

El procediment consta de tres passos, tots executats connectats com a `SYS`/`SYSDBA`:

1. **Crear les vistes** a l'esquema `SYS`, cadascuna acotant la consulta a les taules `COM_%` (i, quan aplica, a l'`owner = 'COMANDA'`).
2. **Concedir `SELECT`** sobre cada vista a l'usuari d'aplicació (`WWW_COMANDA`).
3. **Crear sinònims** a l'esquema `COMANDA` que apunten a les vistes de `SYS`, perquè el codi de l'aplicació pugui consultar-les sense prefix d'esquema (`comanda_v_sessions` en lloc de `SYS.comanda_v_sessions`).

D'aquesta manera l'usuari `WWW_COMANDA` no rep mai accés directe a `V$*` ni a `DBA_*`: només veu el resultat, ja filtrat, d'aquestes sis vistes.

Aquest script s'ha d'executar **un cop per entorn** (cada base de dades on es desplegui COMANDA), i es pot tornar a executar sense risc (`CREATE OR REPLACE VIEW`) si cal actualitzar-lo en el futur.

## 5. SQL a executar

> Font: `comanda-ms-monitor/src/main/resources/db/dba_monitor_views.sql` (versionat també a `versions/v.0.1.3/00_comanda_0.1.3_dba_monitor_views.sql`).

```sql
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
```

**Notes d'execució:**
- Substituir `WWW_COMANDA` pel nom real de l'usuari d'aplicació de cada entorn si difereix (comentat a l'script com a `comanda`).
- L'script és idempotent: `CREATE OR REPLACE VIEW`/`SYNONYM` permeten re-executar-lo sense esborrar res abans.
- No cal reiniciar l'aplicació després d'executar-lo: el dashboard sondeja les vistes periòdicament (cada 1, 5 o 30 minuts segons el bloc) i, si abans fallaven, es recuperen soles en el següent cicle.

## 6. Què passa si no es concedeixen els permisos

Si en algun entorn no es pot o no es vol executar aquest script (per exemple, per política de seguretat de la infraestructura), l'aplicació **no falla**: cada consulta a una vista capta l'excepció d'accés denegat i marca aquell bloc concret com a "no disponible" al dashboard, deixant la resta de blocs (Emmagatzematge, Índexs) operatius perquè no depenen de cap vista de `SYS`.

Alternativament, es pot desactivar completament el dashboard establint el paràmetre d'aplicació `es.caib.comanda.monitor.db.actiu` a `false` (grup "Monitor" / subgrup "Base de dades"), de manera que la pantalla no aparegui al menú.