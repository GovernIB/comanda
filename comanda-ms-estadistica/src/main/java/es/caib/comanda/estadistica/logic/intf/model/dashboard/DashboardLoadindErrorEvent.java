package es.caib.comanda.estadistica.logic.intf.model.dashboard;

import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class DashboardLoadindErrorEvent implements DashboardEvent {

    private Long dashboardId;
    private Long dashboardItemId;
    private InformeWidgetItem informeWidgetItem;
}
