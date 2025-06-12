package es.caib.comanda.estadistica.logic.intf.model.consulta;

import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class InformeWidgetItem implements Serializable {

    protected WidgetTipus tipus;
    protected Long dashboardItemId;

    protected int posX;
    protected int posY;
    protected int width;
    protected int height;

}
