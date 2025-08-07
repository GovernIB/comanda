package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitol;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardTitolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Lògica per a {@link DashboardItem} i {@link DashboardTitol}.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardItemTitolHelper {

    private final DashboardItemRepository dashboardItemRepository;
    private final DashboardTitolRepository dashboardTitolRepository;

    /** Assigna al {@link DashboardItem} el valor {@link DashboardItem#posY PosY} **/
    public void completeResourceItemLogic(DashboardItem resource) {
        if (Objects.nonNull(resource.getDashboard())) {
            resource.setPosY(getPosYValue(resource.getDashboard().getId(), resource.getPosY()));
        }
    }

    /** Assigna al {@link DashboardTitol} el valor {@link DashboardTitol#posY PosY} **/
    public void completeResourceTitolLogic(DashboardTitol resource) {
        if (Objects.nonNull(resource.getDashboard())) {
            resource.setPosY(getPosYValue(resource.getDashboard().getId(), resource.getPosY()));
        }
    }

    private Integer getPosYValue(Long dashboardId, Integer posY) {
        if (Objects.nonNull(posY) || Objects.isNull(dashboardId)){
            return posY;
        }
        Integer maxItems = dashboardItemRepository.findMaxBottomPositionByDashboardId(dashboardId);
        Integer maxTitols = dashboardTitolRepository.findMaxBottomPositionByDashboardId(dashboardId);
        Integer max = Stream.of(maxItems, maxTitols)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0);
        return max;
    }

}
