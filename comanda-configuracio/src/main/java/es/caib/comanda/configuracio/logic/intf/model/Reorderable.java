package es.caib.comanda.configuracio.logic.intf.model;

/**
 * Interfície que han d'implementar tots els recursos reordenables.
 * 
 * @author Limit Tecnologies
 */
public interface Reorderable {

	public Long getOrder();
	public void setOrder(Long order);

}
