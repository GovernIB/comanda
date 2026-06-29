package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.DbTaulaStorage;
import es.caib.comanda.monitor.logic.intf.model.db.TaulaStorageDto;
import es.caib.comanda.monitor.logic.intf.service.DbTaulaStorageService;
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
public class DbTaulaStorageServiceImpl extends BaseNoDatabaseReadonlyResourceService<DbTaulaStorage, String>
        implements DbTaulaStorageService {

    @Autowired
    private DbMetricsServiceImpl dbMetricsService;

    @Override
    protected Optional<NoDatabaseResourceEntity<DbTaulaStorage, String>> entityRepositoryFindOne(String id) {
        return dbMetricsService.getStorage().stream()
                .filter(dto -> id.equals(dto.getTaula()))
                .map(this::toEntity)
                .findFirst();
    }

    @Override
    protected Page<NoDatabaseResourceEntity<DbTaulaStorage, String>> entityRepositoryFindEntities(
            String quickFilter, String filter, String[] namedQueries, Pageable pageable) {
        List<NoDatabaseResourceEntity<DbTaulaStorage, String>> all = dbMetricsService.getStorage().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        return toPage(all, pageable);
    }

    private NoDatabaseResourceEntity<DbTaulaStorage, String> toEntity(TaulaStorageDto dto) {
        DbTaulaStorage r = new DbTaulaStorage();
        r.setId(dto.getTaula());
        r.setNumFiles(dto.getNumFiles());
        r.setBytesReservats(dto.getBytesReservats());
        r.setBytesEstimats(dto.getBytesEstimats());
        r.setUltimaAnalisi(dto.getUltimaAnalisi());
        return NoDatabaseResourceEntity.<DbTaulaStorage, String>builder().id(dto.getTaula()).resource(r).build();
    }

    private Page<NoDatabaseResourceEntity<DbTaulaStorage, String>> toPage(
            List<NoDatabaseResourceEntity<DbTaulaStorage, String>> all, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(all, pageable, all.size());
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<NoDatabaseResourceEntity<DbTaulaStorage, String>> page =
                start > all.size() ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(page, pageable, all.size());
    }
}
