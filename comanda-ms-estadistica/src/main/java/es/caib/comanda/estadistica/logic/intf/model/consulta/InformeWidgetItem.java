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
    protected Long dashboardTitolId;
    protected String entornCodi;
    protected Long widgetId;
    private String titol;
    private String descripcio;

    protected int posX;
    protected int posY;
    protected int width;
    protected int height;
    protected boolean destacat;

    protected boolean error;
    protected String errorMsg;
    protected String errorTrace;

    protected boolean loading;

    // Cert quan l'usuari autenticat no té cap permís d'entitat ni d'òrgan: no s'ha arribat a consultar cap dada,
    // el frontend ha de mostrar un missatge informatiu enlloc de contingut buit.
    protected boolean senseAccesDades;

}
