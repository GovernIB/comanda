package es.caib.comanda.salut.logic.intf.model;

/**
 * Enumerat que indica l'estat d'una app integració o subsistema (aixecat, caigut).
 *
 * @author Límit Tecnologies
 */
public enum SalutEstat {
	UP,
	WARN,
	DEGRADED,
	DOWN,
	MAINTENANCE,
	UNKNOWN,
	ERROR
}
