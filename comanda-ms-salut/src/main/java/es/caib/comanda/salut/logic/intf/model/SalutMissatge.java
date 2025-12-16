package es.caib.comanda.salut.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.model.v1.salut.SalutNivell;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Missatge que forma part d'una informació de salut.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
public class SalutMissatge extends BaseResource<Long> {

	@NotNull
	private LocalDateTime data;
	@NotNull
	private SalutNivell nivell;
	@NotNull
	@Size(max = 2048)
	private String missatge;

	@Transient
	private ResourceReference<Salut, Long> salut;

}
