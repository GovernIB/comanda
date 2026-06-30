package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.scheduler.TascaBackground;
import es.caib.comanda.estadistica.logic.intf.service.TascaBackgroundService;
import es.caib.comanda.ms.logic.helper.SchedulerTaskRegistryService;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.logic.service.BaseNoDatabaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TascaBackgroundServiceImpl extends BaseNoDatabaseMutableResourceService<TascaBackground, String> implements TascaBackgroundService {

    private final SchedulerTaskRegistryService schedulerTaskRegistry;

    @Override
    public TascaBackground getOne(String id, String[] perspectives) throws ResourceNotFoundException {
        return schedulerTaskRegistry.getById(id)
                .map(this::toResource)
                .orElseThrow(() -> new ResourceNotFoundException(TascaBackground.class, id));
    }

    @Override
    public Page<TascaBackground> findPage(String quickFilter, String filter, String[] namedQueries, String[] perspectives, Pageable pageable) {
        List<TascaBackground> tasques = schedulerTaskRegistry.getAll().stream()
                .map(this::toResource)
                .collect(Collectors.toList());
        return new PageImpl<>(tasques, pageable, tasques.size());
    }

    @Override
    public void delete(String id, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotFoundException, AnswerRequiredException {
        schedulerTaskRegistry.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TascaBackground.class, id));
        schedulerTaskRegistry.trigger(id);
    }

    private TascaBackground toResource(SchedulerTaskRegistryService.SchedulerTaskEntry entry) {
        TascaBackground tasca = TascaBackground.builder()
                .nom(entry.getNom())
                .descripcio(entry.getDescripcio())
                .expressioHuman(entry.getExpressioHuman())
                .ultimaExecucio(entry.getUltimaExecucio())
                .ultimaEstat(entry.getUltimaEstat())
                .ultimaDuracio(entry.getUltimaDuracio())
                .proxExecucio(entry.getProxExecucio())
                .build();
        tasca.setId(entry.getId());
        return tasca;
    }
}
