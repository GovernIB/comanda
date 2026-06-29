package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.DbTopSql;
import es.caib.comanda.monitor.logic.intf.model.db.TopSqlDto;
import es.caib.comanda.monitor.logic.intf.service.DbTopSqlService;
import es.caib.comanda.ms.logic.service.BaseNoDatabaseReadonlyResourceService;
import es.caib.comanda.ms.persist.entity.NoDatabaseResourceEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DbTopSqlServiceImpl extends BaseNoDatabaseReadonlyResourceService<DbTopSql, String>
        implements DbTopSqlService {

    @Autowired
    private DbMetricsServiceImpl dbMetricsService;

    @Override
    protected Optional<NoDatabaseResourceEntity<DbTopSql, String>> entityRepositoryFindOne(String id) {
        return dbMetricsService.getTopSql().stream()
                .filter(dto -> id.equals(dto.getSqlId()))
                .map(this::toEntity)
                .findFirst();
    }

    @Override
    protected Page<NoDatabaseResourceEntity<DbTopSql, String>> entityRepositoryFindEntities(
            String quickFilter, String filter, String[] namedQueries, Pageable pageable) {
        List<NoDatabaseResourceEntity<DbTopSql, String>> all = dbMetricsService.getTopSql().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        return toPage(all, pageable);
    }

    private NoDatabaseResourceEntity<DbTopSql, String> toEntity(TopSqlDto dto) {
        DbTopSql r = new DbTopSql();
        r.setId(dto.getSqlId());
        r.setTempsTotalS(dto.getTempsTotalS());
        r.setExecucions(dto.getExecucions());
        r.setMsPerExec(dto.getMsPerExec());
        r.setBuffersPerExec(dto.getBuffersPerExec());
        r.setSqlText(dto.getSqlText());
        return NoDatabaseResourceEntity.<DbTopSql, String>builder().id(dto.getSqlId()).resource(r).build();
    }

    private Page<NoDatabaseResourceEntity<DbTopSql, String>> toPage(
            List<NoDatabaseResourceEntity<DbTopSql, String>> all, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(all, pageable, all.size());
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<NoDatabaseResourceEntity<DbTopSql, String>> page =
                start > all.size() ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(page, pageable, all.size());
    }
}
