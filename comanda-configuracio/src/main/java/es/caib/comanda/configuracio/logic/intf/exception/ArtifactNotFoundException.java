package es.caib.comanda.configuracio.logic.intf.exception;

import es.caib.comanda.configuracio.logic.intf.model.ResourceArtifactType;
import lombok.Getter;

/**
 * Excepció que es llença quan no es troba l'informe.
 * 
 * @author Limit Tecnologies
 */
@Getter
public class ArtifactNotFoundException extends NotFoundException {

	private final Class<?> resourceClass;
	private final ResourceArtifactType type;
	private final String code;

	public ArtifactNotFoundException(
			Class<?> resourceClass,
			ResourceArtifactType type,
			String code) {
		super("Report " + getArtifactId(resourceClass, type, code));
		this.resourceClass = resourceClass;
		this.type = type;
		this.code = code;
	}

	private static String getArtifactId(
			Class<?> resourceClass,
			ResourceArtifactType type,
			String code) {
		return "(resourceClass=" + resourceClass.getName() + ", type=" + type + ", code=" + code + ")";
	}

}
