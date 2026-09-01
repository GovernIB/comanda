package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Classe per exportar un terme d'una fórmula d'indicador (vegeu {@code IndicadorFormulaTerme}).
 *
 * L'indicador component es referencia pel seu codi (únic dins de l'entornApp de la fórmula a la qual pertany,
 * vegeu {@link IndicadorExport}), no per id, ja que els id no es preserven entre entorns/instàncies.
 *
 * @author Límit Tecnologies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorFormulaTermeExport implements Serializable {

    @NotBlank
    private String indicadorComponentCodi;

    @NotNull
    private OperadorFormulaEnum operador;

    @NotNull
    private Integer ordre;

}
