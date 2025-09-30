package es.caib.comanda.salut.logic.intf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Informació per a l'informe del llistat de salut.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalutInformeLlistatParams implements Serializable {
    @NotNull
    private LocalDateTime dataFi; // TODO Canviar nom a dataReferencia al fer merge de la branca WIP
	@NotNull
	private List<Long> entornAppIdList;
	@NotNull
	private SalutInformeAgrupacio agrupacio;
}
