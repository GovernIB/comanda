package es.caib.comanda.estadistica.logic.intf.model.consulta;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InformeWidgetTaulaItem extends InformeWidgetItem implements Serializable {

    private String titol;
    private String titolAgrupament;

    // Column definitions
    private List<Map<String, String>> columns;

    // Row data
    private List<Map<String, String>> rows;

    // Configuracions visuals
    private AtributsVisualsTaula atributsVisuals;

}
