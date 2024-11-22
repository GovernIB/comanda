package es.caib.comanda.configuracio.logic.intf.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * Informació de l'event de canvi del valor d'un camp de formulari del front.
 * 
 * @author Limit Tecnologies
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OnChangeEvent {

	private Serializable id;
	private JsonNode previous;
	private String fieldName;
	private JsonNode fieldValue;
	private Map<String, Object> answers;

}
