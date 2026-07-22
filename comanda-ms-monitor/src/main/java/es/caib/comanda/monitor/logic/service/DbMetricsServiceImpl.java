package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.*;
import es.caib.comanda.monitor.persist.repository.DbMetricsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class DbMetricsServiceImpl {

    @Autowired
    private DbMetricsRepository repository;

    private volatile List<TaulaStorageDto>   cachedStorage      = Collections.emptyList();
    private volatile List<TaulaActivitatDto> cachedActivitat    = Collections.emptyList();
    private volatile List<SessionsResumDto>  cachedSessions     = Collections.emptyList();
    private volatile List<TopSqlDto>         cachedTopSql       = Collections.emptyList();
    private volatile List<TablespaceDto>     cachedTablespaces  = Collections.emptyList();
    private volatile List<BloqueigDto>       cachedBloqueigs    = Collections.emptyList();
    private volatile List<IndexDto>          cachedIndexos      = Collections.emptyList();
    private volatile Double                  cachedHitRatio     = null;
    private volatile boolean                 bloqueigsDisponibles = false;
    private volatile LocalDateTime           ultimaActualitzacio  = null;

    // 30 min — storage metrics change slowly
    @Scheduled(fixedDelay = 1_800_000, initialDelay = 5_000)
    public void collectStorage() {
        log.debug("Recol·lectant mètriques d'emmagatzematge de la BD");
        cachedStorage     = repository.findTableStorage();
        cachedTablespaces = repository.findTablespaces();
        cachedIndexos     = repository.findIndexos();
    }

    // 5 min — I/O and session metrics
    @Scheduled(fixedDelay = 300_000, initialDelay = 10_000)
    public void collectActivity() {
        log.debug("Recol·lectant mètriques d'activitat de la BD");
        cachedActivitat = repository.findSegmentStats();
        cachedSessions  = repository.findSessions();
        cachedTopSql    = repository.findTopSql();
        cachedHitRatio  = repository.findHitRatioCache();
        ultimaActualitzacio = LocalDateTime.now();
    }

    // 1 min — locks are real-time; empty list can mean 0 locks OR vista no accessible
    @Scheduled(fixedDelay = 60_000, initialDelay = 15_000)
    public void collectBloqueigs() {
        log.debug("Recol·lectant bloquejos de la BD");
        try {
            cachedBloqueigs    = repository.findBloqueigs();
            bloqueigsDisponibles = true;
        } catch (Exception e) {
            log.warn("Error consultant comanda_v_bloquejos (vista no creada?): {}", e.getMessage());
            bloqueigsDisponibles = false;
            cachedBloqueigs    = Collections.emptyList();
        }
    }

    public DbOverviewDto getOverview() {
        long sessionsActives = cachedSessions.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getEstat()))
                .mapToLong(SessionsResumDto::getQuantitat).sum();
        long sessionsTotal = cachedSessions.stream()
                .mapToLong(SessionsResumDto::getQuantitat).sum();
        long totalBytes = cachedStorage.stream()
                .mapToLong(TaulaStorageDto::getBytesReservats).sum();
        int indexosInvalids = (int) cachedIndexos.stream()
                .filter(i -> "UNUSABLE".equalsIgnoreCase(i.getStatus())).count();

        DbOverviewDto dto = new DbOverviewDto();
        dto.setSessionsActives(sessionsActives);
        dto.setSessionsTotal(sessionsTotal);
        dto.setHitRatioCache(cachedHitRatio);
        dto.setTaulesCom(cachedStorage.size());
        dto.setTotalBytesReservats(totalBytes);
        dto.setUltimaActualitzacio(ultimaActualitzacio);
        dto.setSessionsDisponibles(!cachedSessions.isEmpty());
        dto.setActivitatDisponible(!cachedActivitat.isEmpty());
        dto.setSqlDisponible(!cachedTopSql.isEmpty());
        dto.setTablespacesDisponibles(!cachedTablespaces.isEmpty());
        dto.setBloqueigsDisponibles(bloqueigsDisponibles);
        dto.setBloqueigsActius(cachedBloqueigs.size());
        dto.setIndexosInvalidsCount(indexosInvalids);
        return dto;
    }

    public void rebuildIndex(String indexName) {
        if (!indexName.matches("[A-Z0-9_$#]+")) {
            throw new IllegalArgumentException("Nom d'índex no vàlid: " + indexName);
        }
        if (!repository.isIndexForComTable(indexName)) {
            throw new IllegalArgumentException("Índex no pertany a cap taula COM_*: " + indexName);
        }
        log.info("Reconstruint índex: {}", indexName);
        repository.rebuildIndex(indexName);
        cachedIndexos = repository.findIndexos();
    }

    public List<TaulaStorageDto>   getStorage()     { return cachedStorage; }
    public List<TaulaActivitatDto> getActivitat()   { return cachedActivitat; }
    public List<SessionsResumDto>  getSessions()    { return cachedSessions; }
    public List<TopSqlDto>         getTopSql()      { return cachedTopSql; }
    public List<TablespaceDto>     getTablespaces() { return cachedTablespaces; }
    public List<BloqueigDto>       getBloqueigs()   { return cachedBloqueigs; }
    public List<IndexDto>          getIndexos()     { return cachedIndexos; }
}
