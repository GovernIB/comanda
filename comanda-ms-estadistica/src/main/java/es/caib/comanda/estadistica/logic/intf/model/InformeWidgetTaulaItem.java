package es.caib.comanda.estadistica.logic.intf.model;

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
    private List<Map<String, Object>> columns;

    // Row data
    private List<Map<String, Object>> rows;

    // Dimension values for grouping
    private List<String> dimensionValues;

    // Atributs per a la configuració visual de la taula

    // Configuració general de la taula
    private Boolean mostrarCapcalera;     // Indica si s'ha de mostrar la capçalera de la taula
    private Boolean mostrarBordes;        // Indica si s'han de mostrar els bordes de la taula
    private Boolean mostrarAlternancia;   // Indica si s'han d'alternar els colors de les files
    private String colorAlternancia;      // Color per a les files alternes

    // Configuracions visuals
    private AtributsVisualsTaula atributsVisuals;

}
