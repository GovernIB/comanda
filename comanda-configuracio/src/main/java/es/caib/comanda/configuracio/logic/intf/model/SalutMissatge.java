package es.caib.comanda.configuracio.logic.intf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Informació de salut retornada per una app.
 *
 * @author Limit Tecnologies
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
