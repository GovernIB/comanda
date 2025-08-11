package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Informació d'un manual.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
public class AppManual implements Serializable {
	private Long id;
	private String nom;
	private String path;
}
