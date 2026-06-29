package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.DbTaulaActivitat;
import es.caib.comanda.monitor.logic.intf.model.db.TaulaActivitatDto;
import es.caib.comanda.monitor.logic.intf.service.DbTaulaActivitatService;
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
public class DbTaulaActivitatServiceImpl extends BaseNoDatabaseReadonlyResourceService<DbTaulaActivitat, String>
        implements DbTaulaActivitatService {

    @Autowired
    private DbMetricsServiceImpl dbMetricsService;

    @Override
    protected Optional<NoDatabaseResourceEntity<DbTaulaActivitat, String>> entityRepositoryFindOne(String id) {
        return dbMetricsService.getActivitat().stream()
                .filter(dto -> id.equals(dto.getTaula()))
                .map(this::toEntity)
                .findFirst();
    }

    @Override
    protected Page<NoDatabaseResourceEntity<DbTaulaActivitat, String>> entityRepositoryFindEntities(
            String quickFilter, String filter, String[] namedQueries, Pageable pageable) {
        List<NoDatabaseResourceEntity<DbTaulaActivitat, String>> all = dbMetricsService.getActivitat().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        return toPage(all, pageable);
    }

    private NoDatabaseResourceEntity<DbTaulaActivitat, String> toEntity(TaulaActivitatDto dto) {
        DbTaulaActivitat r = new DbTaulaActivitat();
        r.setId(dto.getTaula());
        r.setLecturesFisiques(dto.getLecturesFisiques());
        r.setLecturesLogiques(dto.getLecturesLogiques());
        r.setEsperesBuffer(dto.getEsperesBuffer());
        r.setEsperesFila(dto.getEsperesFila());
        return NoDatabaseResourceEntity.<DbTaulaActivitat, String>builder().id(dto.getTaula()).resource(r).build();
    }

    private Page<NoDatabaseResourceEntity<DbTaulaActivitat, String>> toPage(
            List<NoDatabaseResourceEntity<DbTaulaActivitat, String>> all, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(all, pageable, all.size());
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<NoDatabaseResourceEntity<DbTaulaActivitat, String>> page =
                start > all.size() ? Collections.emptyList() : all.subList(start, end);
        return new PageImpl<>(page, pageable, all.size());
    }
}
