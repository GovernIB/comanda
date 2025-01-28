package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Informació d'una integració.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
public class AppIntegracio {

	private Long id;
	private String codi;
	private String nom;
	private boolean activa;

}
