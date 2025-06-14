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
public class InformeWidgetItem implements Serializable {

    protected WidgetTipus tipus;
    protected Long dashboardItemId;
    protected String entornCodi;
    private String titol;
    private String descripcio;

    protected int posX;
    protected int posY;
    protected int width;
    protected int height;

    protected boolean error;
    protected String errorMsg;
    protected String errorTrace;

    protected boolean loading;

}
