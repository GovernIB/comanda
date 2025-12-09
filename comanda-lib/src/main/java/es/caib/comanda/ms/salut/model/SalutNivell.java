package es.caib.comanda.ms.salut.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumerat que indica el nivell de gravetat d'un missatge.
 *
 * @author Límit Tecnologies
 */
@Schema(name = "SalutNivell", description = "Nivell de gravetat d'un missatge de salut")
public enum SalutNivell {
	ERROR,
	WARN,
	INFO
}
