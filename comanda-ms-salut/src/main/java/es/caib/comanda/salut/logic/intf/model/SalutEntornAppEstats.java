package es.caib.comanda.salut.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Informació de salut sobre un entorn aplicació.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
public class SalutEntornAppEstats extends BaseResource<Long> {

    @NotNull
    private Long entornAppId;

	private LocalDateTime darrerActiu;
    private LocalDateTime darrerAdvertencia;
    private LocalDateTime darrerDegradada;
    private LocalDateTime darrerError;
    private LocalDateTime darrerCaiguda;
    private LocalDateTime darrerManteniment;
    private LocalDateTime darrerDesconegut;
    private LocalDateTime darrerPeticioError;

}
