package es.caib.comanda.monitor.persist.repository;

import es.caib.comanda.monitor.logic.intf.model.db.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@Slf4j
public class DbMetricsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // USER_TABLES i USER_SEGMENTS no requereixen permisos addicionals (objectes propis).
    private static final String SQL_TABLE_STORAGE =
            "SELECT t.TABLE_NAME, NVL(t.NUM_ROWS, 0), NVL(s.BYTES, 0), " +
            "  CASE WHEN NVL(t.NUM_ROWS, 0) > 0 AND NVL(t.AVG_ROW_LEN, 0) > 0 " +
            "       THEN t.NUM_ROWS * t.AVG_ROW_LEN ELSE 0 END, t.LAST_ANALYZED " +
            "FROM USER_TABLES t " +
            "LEFT JOIN USER_SEGMENTS s ON s.SEGMENT_NAME = t.TABLE_NAME AND s.SEGMENT_TYPE = 'TABLE' " +
            "WHERE t.TABLE_NAME LIKE 'COM\\_%' ESCAPE '\\' " +
            "ORDER BY NVL(s.BYTES, 0) DESC";

    // Conduit des de USER_TABLES per incloure totes les taules COM_* (també les buides).
    private static final String SQL_SEGMENT_STATS =
            "SELECT t.TABLE_NAME, " +
            "  NVL(SUM(CASE WHEN ss.statistic_name = 'physical reads'    THEN ss.value ELSE 0 END), 0), " +
            "  NVL(SUM(CASE WHEN ss.statistic_name = 'logical reads'     THEN ss.value ELSE 0 END), 0), " +
            "  NVL(SUM(CASE WHEN ss.statistic_name = 'buffer busy waits' THEN ss.value ELSE 0 END), 0), " +
            "  NVL(SUM(CASE WHEN ss.statistic_name = 'row lock waits'    THEN ss.value ELSE 0 END), 0) " +
            "FROM user_tables t " +
            "LEFT JOIN comanda_v_segment_stats ss ON ss.table_name = t.TABLE_NAME " +
            "WHERE t.TABLE_NAME LIKE 'COM\\_%' ESCAPE '\\' " +
            "GROUP BY t.TABLE_NAME " +
            "ORDER BY 2 DESC";

    private static final String SQL_SESSIONS =
            "SELECT status, quantitat FROM comanda_v_sessions";

    private static final String SQL_TOP_SQL =
            "SELECT sql_id, ROUND(elapsed_time / 1000000, 2), executions, " +
            "  CASE WHEN executions > 0 THEN ROUND(elapsed_time / executions / 1000, 2) ELSE 0 END, " +
            "  ROUND(buffer_gets / GREATEST(executions, 1), 0), sql_text " +
            "FROM comanda_v_top_sql " +
            "ORDER BY elapsed_time DESC " +
            "FETCH FIRST 50 ROWS ONLY";

    private static final String SQL_TABLESPACES =
            "SELECT tablespace_name, total_mb, max_mb, usat_mb, lliure_mb, pct_usat " +
            "FROM comanda_v_tablespaces " +
            "ORDER BY pct_usat DESC";

    private static final String SQL_HIT_RATIO =
            "SELECT ROUND(" +
            "  SUM(CASE WHEN name IN ('consistent gets','db block gets') THEN value ELSE 0 END) / " +
            "  GREATEST(SUM(value), 1) * 100, 2) " +
            "FROM comanda_v_sysstat";

    // Sense try-catch: el servei captura l'excepció per distingir "sense dades" de "vista inaccessible".
    private static final String SQL_BLOQUEIGS =
            "SELECT sid, serial_num, username, status, object_name, object_type, " +
            "  lock_mode, lock_request, blocking " +
            "FROM comanda_v_bloquejos " +
            "ORDER BY blocking DESC, sid";

    // USER_INDEXES no requereix permisos addicionals (objectes propis).
    private static final String SQL_INDEXOS =
            "SELECT index_name, table_name, status, uniqueness, " +
            "  NVL(num_rows, 0), last_analyzed, NVL(blevel, 0), NVL(leaf_blocks, 0) " +
            "FROM user_indexes " +
            "WHERE table_name LIKE 'COM\\_%' ESCAPE '\\' " +
            "ORDER BY CASE WHEN status = 'UNUSABLE' THEN 0 ELSE 1 END, table_name, index_name";

    private static final String SQL_COUNT_INDEX_FOR_COM =
            "SELECT COUNT(*) FROM user_indexes " +
            "WHERE index_name = ? AND table_name LIKE 'COM\\_%' ESCAPE '\\'";

    public List<TaulaStorageDto> findTableStorage() {
        try {
            return jdbcTemplate.query(SQL_TABLE_STORAGE, (rs, i) -> {
                TaulaStorageDto dto = new TaulaStorageDto();
                dto.setTaula(rs.getString(1));
                dto.setNumFiles(rs.getLong(2));
                dto.setBytesReservats(rs.getLong(3));
                dto.setBytesEstimats(rs.getLong(4));
                dto.setUltimaAnalisi(rs.getTimestamp(5));
                return dto;
            });
        } catch (Exception e) {
            log.warn("Error consultant USER_SEGMENTS/USER_TABLES: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<TaulaActivitatDto> findSegmentStats() {
        try {
            return jdbcTemplate.query(SQL_SEGMENT_STATS, (rs, i) -> {
                TaulaActivitatDto dto = new TaulaActivitatDto();
                dto.setTaula(rs.getString(1));
                dto.setLecturesFisiques(rs.getLong(2));
                dto.setLecturesLogiques(rs.getLong(3));
                dto.setEsperesBuffer(rs.getLong(4));
                dto.setEsperesFila(rs.getLong(5));
                return dto;
            });
        } catch (Exception e) {
            log.warn("Error consultant comanda_v_segment_stats (vista no creada?): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<SessionsResumDto> findSessions() {
        try {
            return jdbcTemplate.query(SQL_SESSIONS, (rs, i) -> {
                SessionsResumDto dto = new SessionsResumDto();
                dto.setEstat(rs.getString(1));
                dto.setQuantitat(rs.getLong(2));
                return dto;
            });
        } catch (Exception e) {
            log.warn("Error consultant comanda_v_sessions (vista no creada?): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<TopSqlDto> findTopSql() {
        try {
            return jdbcTemplate.query(SQL_TOP_SQL, (rs, i) -> {
                TopSqlDto dto = new TopSqlDto();
                dto.setSqlId(rs.getString(1));
                dto.setTempsTotalS(rs.getDouble(2));
                dto.setExecucions(rs.getLong(3));
                dto.setMsPerExec(rs.getDouble(4));
                dto.setBuffersPerExec(rs.getLong(5));
                dto.setSqlText(rs.getString(6));
                return dto;
            });
        } catch (Exception e) {
            log.warn("Error consultant comanda_v_top_sql (vista no creada?): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<TablespaceDto> findTablespaces() {
        try {
            return jdbcTemplate.query(SQL_TABLESPACES, (rs, i) -> {
                TablespaceDto dto = new TablespaceDto();
                dto.setNom(rs.getString(1));
                dto.setTotalMb(rs.getDouble(2));
                dto.setMaxMb(rs.getDouble(3));
                dto.setUsatMb(rs.getDouble(4));
                dto.setLliureMb(rs.getDouble(5));
                dto.setPctUsat(rs.getDouble(6));
                return dto;
            });
        } catch (Exception e) {
            log.warn("Error consultant comanda_v_tablespaces (vista no creada?): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Double findHitRatioCache() {
        try {
            return jdbcTemplate.queryForObject(SQL_HIT_RATIO, Double.class);
        } catch (Exception e) {
            log.warn("Error consultant comanda_v_sysstat (vista no creada?): {}", e.getMessage());
            return null;
        }
    }

    /** Llança excepció si la vista no és accessible (el servei la captura per marcar disponibilitat). */
    public List<BloqueigDto> findBloqueigs() {
        return jdbcTemplate.query(SQL_BLOQUEIGS, (rs, i) -> {
            BloqueigDto dto = new BloqueigDto();
            dto.setSid(rs.getLong(1));
            dto.setSerialNum(rs.getLong(2));
            dto.setUsername(rs.getString(3));
            dto.setStatus(rs.getString(4));
            dto.setObjectName(rs.getString(5));
            dto.setObjectType(rs.getString(6));
            dto.setLockMode(rs.getString(7));
            dto.setLockRequest(rs.getString(8));
            dto.setBlocking(rs.getInt(9) > 0);
            return dto;
        });
    }

    public List<IndexDto> findIndexos() {
        try {
            return jdbcTemplate.query(SQL_INDEXOS, (rs, i) -> {
                IndexDto dto = new IndexDto();
                dto.setIndexName(rs.getString(1));
                dto.setTableName(rs.getString(2));
                dto.setStatus(rs.getString(3));
                dto.setUniqueness(rs.getString(4));
                dto.setNumRows(rs.getLong(5));
                dto.setLastAnalyzed(rs.getTimestamp(6));
                dto.setBlevel(rs.getInt(7));
                dto.setLeafBlocks(rs.getLong(8));
                return dto;
            });
        } catch (Exception e) {
            log.warn("Error consultant user_indexes: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean isIndexForComTable(String indexName) {
        Integer count = jdbcTemplate.queryForObject(SQL_COUNT_INDEX_FOR_COM, Integer.class, indexName);
        return count != null && count > 0;
    }

    public void rebuildIndex(String indexName) {
        jdbcTemplate.execute("ALTER INDEX " + indexName + " REBUILD");
    }
}
