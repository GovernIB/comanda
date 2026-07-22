package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.DbIndex;
import es.caib.comanda.monitor.logic.intf.model.db.IndexDto;
import es.caib.comanda.monitor.logic.intf.service.DbIndexService;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.service.BaseNoDatabaseMutableResourceService;
import es.caib.comanda.ms.persist.entity.NoDatabaseResourceEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DbIndexServiceImpl extends BaseNoDatabaseMutableResourceService<DbIndex, String>
        implements DbIndexService {

    @Autowired
    private DbMetricsServiceImpl dbMetricsService;

    @PostConstruct
    public void init() {
        register(DbIndex.ACTION_REBUILD, new RebuildActionExecutor());
    }

    @Override
    protected Optional<NoDatabaseResourceEntity<DbIndex, String>> entityRepositoryFindOne(String id) {
        return dbMetricsService.getIndexos().stream()
                .filter(dto -> id.equalsIgnoreCase(dto.getIndexName()))
                .map(this::toEntity)
                .findFirst();
    }

    @Override
    protected Page<NoDatabaseResourceEntity<DbIndex, String>> entityRepositoryFindEntities(
            String quickFilter, String filter, String[] namedQueries, Pageable pageable) {
        List<NoDatabaseResourceEntity<DbIndex, String>> all = dbMetricsService.getIndexos().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        return toPage(all, pageable);
    }

    private NoDatabaseResourceEntity<DbIndex, String> toEntity(IndexDto dto) {
        DbIndex r = new DbIndex();
        r.setId(dto.getIndexName());
        r.setTableName(dto.getTableName());
        r.setStatus(dto.getStatus());
        r.setUniqueness(dto.getUniqueness());
        r.setNumRows(dto.getNumRows());
        r.setLastAnalyzed(dto.getLastAnalyzed());
        r.setBlevel(dto.getBlevel());
        r.setLeafBlocks(dto.getLeafBlocks());
        return NoDatabaseResourceEntity.<DbIndex, String>builder().id(dto.getIndexName()).resource(r).build();
    }

    private Page<NoDatabaseResourceEntity<DbIndex, String>> toPage(
            List<NoDatabaseResourceEntity<DbIndex, String>> all, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(all, pageable, all.size());
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<NoDatabaseResourceEntity<DbIndex, String>> page =
                start > all.size() ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(page, pageable, all.size());
    }

    private class RebuildActionExecutor
            implements ActionExecutor<NoDatabaseResourceEntity<DbIndex, String>, Serializable, String> {

        @Override
        public void onChange(Serializable id, Serializable previous, String fieldName, Object fieldValue,
                Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames,
                Serializable target) {
        }

        @Override
        public String exec(String code, NoDatabaseResourceEntity<DbIndex, String> entity, Serializable params)
                throws ActionExecutionException {
            try {
                dbMetricsService.rebuildIndex(entity.getId());
                return entity.getId();
            } catch (IllegalArgumentException e) {
                throw new ActionExecutionException(DbIndex.class, entity.getId(), code, e.getMessage(), e);
            }
        }
    }
}
