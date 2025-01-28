package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Informació d'un subsistema.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
public class AppSubsistema {

	private Long id;
	private String codi;
	private String nom;
	private boolean actiu;

}
