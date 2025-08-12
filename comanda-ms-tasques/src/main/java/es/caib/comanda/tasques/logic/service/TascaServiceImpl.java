package es.caib.comanda.tasques.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.tasques.logic.helper.TasquesClientHelper;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import es.caib.comanda.tasques.logic.intf.service.TascaService;
import es.caib.comanda.tasques.persist.entity.TascaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static es.caib.comanda.ms.broker.model.Cues.CUA_TASQUES;

@Slf4j
@Service
@RequiredArgsConstructor
public class TascaServiceImpl extends BaseMutableResourceService<Tasca, Long, TascaEntity> implements TascaService {

    private final AuthenticationHelper authenticationHelper;
    private final TasquesClientHelper tasquesClientHelper;

    @PostConstruct
    public void init() {
        register(Tasca.PERSPECTIVE_PATH, new PathPerspectiveApplicator());
        register(Tasca.PERSPECTIVE_EXPIRATION, new ExpirationPerspectiveApplicator());
    }

    @JmsListener(destination = CUA_TASQUES)
    public void receiveMessage(es.caib.comanda.ms.broker.model.Tasca tasca) {
        log.debug("Tasca rebuda: " + tasca);

        // TODO: Desar tasca a BBDD
    }

    @Override
    protected String additionalSpringFilter(String currentSpringFilter, String[] namedQueries) {
        List<Filter> filters = new ArrayList<>();
        List<String> namedQueriesList = Optional.ofNullable(namedQueries)
                .map(Arrays::asList)
                .orElse(Collections.emptyList());

        Filter filtreBase = (currentSpringFilter != null && !currentSpringFilter.isEmpty())?Filter.parse(currentSpringFilter):null;
        filters.add(filtreBase);

        if (namedQueriesList.contains("USER")) {
            String userName = authenticationHelper.getCurrentUserName();

            filters.add(
                    FilterBuilder.or(
                            FilterBuilder.equal("responsable", userName)
//                            TODO: filtrar por 'usuarisAmbPermis' y 'grupsAmbPermis'
//                            FilterBuilder.exists(
//                                    FilterBuilder.equal("usuarisAmbPermis", userName)
//                            )
                    )
            );
        }

        List<Filter> result = filters.stream()
                .filter(f -> f!=null && !String.valueOf(f).isEmpty())
                .collect(Collectors.toList());

        return result.isEmpty() ? null : FilterBuilder.and(result).generate();
    }

    public class PathPerspectiveApplicator implements PerspectiveApplicator<TascaEntity, Tasca> {
        @Override
        public void applySingle(String code, TascaEntity entity, Tasca resource) throws PerspectiveApplicationException {
            EntornApp entornApp = tasquesClientHelper.entornAppFindById(entity.getEntornAppId());
            resource.setTreePath(new String[]{entornApp.getApp().getNom(), entornApp.getEntorn().getNom(), resource.getIdentificador()});
        }
    }

    public class ExpirationPerspectiveApplicator implements PerspectiveApplicator<TascaEntity, Tasca> {
        @Override
        public void applySingle(String code, TascaEntity entity, Tasca resource) throws PerspectiveApplicationException {
            if (resource.getDataFi() == null) {
                LocalDate hoy = LocalDate.now();
                LocalDate fechaCaducidad = resource.getDataCaducitat(); // Ejemplo

                long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaCaducidad);
                resource.setDiesPerCaducar(diasRestantes);
            }
        }
    }
}
