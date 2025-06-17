package es.caib.comanda.estadistica.logic.intf.model.consulta;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTitol;
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
public class InformeWidgetTitolItem extends InformeWidgetItem implements Serializable {

    // Contingut
    private String subtitol;

    // Configuracions visuals
    private AtributsVisualsTitol atributsVisuals;

}
