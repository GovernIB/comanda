package es.caib.comanda.estadistica.logic.intf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class InformeWidgetParams implements Serializable {

    private Long dashboardItemId;

}
