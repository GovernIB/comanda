package es.caib.comanda.estadistica.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;

import javax.validation.constraints.NotNull;

public class DashboardItem extends BaseResource<Long> {

    @NotNull
    private int posX;
    @NotNull
    private int posY;
    @NotNull
    private int width;
    @NotNull
    private int height;

    @NotNull
    private ResourceReference<Dashboard, Long> dashboard;
    @NotNull
    private ResourceReference<EstadisticaView, Long> view;

}
