package es.caib.comanda.estadistica.logic.intf.model.consulta;

import es.caib.comanda.estadistica.logic.helper.PeriodeResolverHelper.PeriodeDates;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DadesComunsWidgetConsulta {
    private Long entornAppId;
    private String entornCodi;
    private PeriodeDates periodeDates;
    private AtributsVisuals atributsVisuals;
}
