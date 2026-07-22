package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.DbTablespace;
import es.caib.comanda.monitor.logic.intf.model.db.TablespaceDto;
import es.caib.comanda.monitor.logic.intf.service.DbTablespaceService;
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
public class DbTablespaceServiceImpl extends BaseNoDatabaseReadonlyResourceService<DbTablespace, String>
        implements DbTablespaceService {

    @Autowired
    private DbMetricsServiceImpl dbMetricsService;

    @Override
    protected Optional<NoDatabaseResourceEntity<DbTablespace, String>> entityRepositoryFindOne(String id) {
        return dbMetricsService.getTablespaces().stream()
                .filter(dto -> id.equals(dto.getNom()))
                .map(this::toEntity)
                .findFirst();
    }

    @Override
    protected Page<NoDatabaseResourceEntity<DbTablespace, String>> entityRepositoryFindEntities(
            String quickFilter, String filter, String[] namedQueries, Pageable pageable) {
        List<NoDatabaseResourceEntity<DbTablespace, String>> all = dbMetricsService.getTablespaces().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        return toPage(all, pageable);
    }

    private NoDatabaseResourceEntity<DbTablespace, String> toEntity(TablespaceDto dto) {
        DbTablespace r = new DbTablespace();
        r.setId(dto.getNom());
        r.setTotalMb(dto.getTotalMb());
        r.setMaxMb(dto.getMaxMb());
        r.setUsatMb(dto.getUsatMb());
        r.setLliureMb(dto.getLliureMb());
        r.setPctUsat(dto.getPctUsat());
        return NoDatabaseResourceEntity.<DbTablespace, String>builder().id(dto.getNom()).resource(r).build();
    }

    private Page<NoDatabaseResourceEntity<DbTablespace, String>> toPage(
            List<NoDatabaseResourceEntity<DbTablespace, String>> all, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(all, pageable, all.size());
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<NoDatabaseResourceEntity<DbTablespace, String>> page =
                start > all.size() ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(page, pageable, all.size());
    }
}
