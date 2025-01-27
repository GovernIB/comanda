package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Informació d'un subsistema.
 *
 * @author Limit Tecnologies
 */
@Getter
@NoArgsConstructor
public class AppSubsistema {

	private Long id;
	private String codi;
	private String nom;
	private boolean actiu;

}
