package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Informació d'un context.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
public class AppContext implements Serializable {

	private Long id;
	private String codi;
	private String nom;
	private String path;
	private List<AppManual> manuals;
	private String api;
	private boolean actiu;

}
