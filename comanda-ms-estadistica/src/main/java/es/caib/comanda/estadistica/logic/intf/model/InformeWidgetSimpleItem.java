package es.caib.comanda.estadistica.logic.intf.model;

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
public class InformeWidgetSimpleItem extends InformeWidgetItem implements Serializable {

    // Contingut
    private String titol;
    private String valor;
    private String unitat;
    private String descripcio;
    private String canviPercentual;
    private String icona;

    // Configuracions visuals
    private AtributsVisualsSimple atributsVisuals;

}
