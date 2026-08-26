package es.caib.comanda.estadistica.logic.intf.model.consulta;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Terme ja resolt d'una fórmula d'indicador, preparat per a la capa de generació de SQL (vegeu
 * FetRepositoryDialect): el codi de l'indicador component i l'operador amb què s'aplica.
 *
 * @author Límit Tecnologies
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorFormulaTermeResolt {
    private String indicadorCodi;
    private OperadorFormulaEnum operador;
}
