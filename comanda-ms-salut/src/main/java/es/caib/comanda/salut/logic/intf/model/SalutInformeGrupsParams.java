package es.caib.comanda.salut.logic.intf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Paràmetres per a l'informe d'agrupacions temporals de salut
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalutInformeGrupsParams implements Serializable {
	@NotNull
	private LocalDateTime dataReferencia;
	@NotNull
	private SalutInformeAgrupacio agrupacio;
}
