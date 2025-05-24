package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Informació d'un subsistema.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
public class AppSubsistema implements Serializable {

	private Long id;
	private String codi;
	private String nom;
	private boolean actiu;

}
