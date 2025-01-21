package es.caib.comanda.ms.logic.intf.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Informació sobre un artefacte d'un recurs.
 * 
 * @author Limit Tecnologies
 */
@Getter
@Setter
@AllArgsConstructor
public class ResourceArtifact {

	private ResourceArtifactType type;
	private String code;
	@JsonIgnore
	private Class<?> formClass;

	public boolean isFormClassActive() {
		return formClass != null;
	}

}
