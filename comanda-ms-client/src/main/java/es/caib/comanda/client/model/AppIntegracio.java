package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Informació d'una integració.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
public class AppIntegracio implements Serializable {

	private Long id;
	private AppRef app;
	private String codi;
	private IntegracioRef integracio;
	private byte[] logo;
	private boolean activa;

}
