package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.BloqueigDto;
import es.caib.comanda.monitor.logic.intf.model.db.DbBloqueig;
import es.caib.comanda.monitor.logic.intf.service.DbBloqueigService;
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
public class DbBloqueigServiceImpl extends BaseNoDatabaseReadonlyResourceService<DbBloqueig, Long>
        implements DbBloqueigService {

    @Autowired
    private DbMetricsServiceImpl dbMetricsService;

    @Override
    protected Optional<NoDatabaseResourceEntity<DbBloqueig, Long>> entityRepositoryFindOne(Long id) {
        return dbMetricsService.getBloqueigs().stream()
                .filter(dto -> id.equals(dto.getSid()))
                .map(this::toEntity)
                .findFirst();
    }

    @Override
    protected Page<NoDatabaseResourceEntity<DbBloqueig, Long>> entityRepositoryFindEntities(
            String quickFilter, String filter, String[] namedQueries, Pageable pageable) {
        List<NoDatabaseResourceEntity<DbBloqueig, Long>> all = dbMetricsService.getBloqueigs().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        return toPage(all, pageable);
    }

    private NoDatabaseResourceEntity<DbBloqueig, Long> toEntity(BloqueigDto dto) {
        DbBloqueig r = new DbBloqueig();
        r.setId(dto.getSid());
        r.setSerialNum(dto.getSerialNum());
        r.setUsername(dto.getUsername());
        r.setStatus(dto.getStatus());
        r.setObjectName(dto.getObjectName());
        r.setObjectType(dto.getObjectType());
        r.setLockMode(dto.getLockMode());
        r.setLockRequest(dto.getLockRequest());
        r.setBlocking(dto.isBlocking());
        return NoDatabaseResourceEntity.<DbBloqueig, Long>builder().id(dto.getSid()).resource(r).build();
    }

    private Page<NoDatabaseResourceEntity<DbBloqueig, Long>> toPage(
            List<NoDatabaseResourceEntity<DbBloqueig, Long>> all, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(all, pageable, all.size());
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<NoDatabaseResourceEntity<DbBloqueig, Long>> page =
                start > all.size() ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(page, pageable, all.size());
    }
}
