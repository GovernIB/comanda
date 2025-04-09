package es.caib.comanda.estadistica.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class Dashboard extends BaseResource<Long> {

    @NotNull
    @Size(max = 64)
    private String titol;
    @Size(max = 1024)
    private String descripcio;

    @NotEmpty
    private List<DashboardItem> items;
    
}
