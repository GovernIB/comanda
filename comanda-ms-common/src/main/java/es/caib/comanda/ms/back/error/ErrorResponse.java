/**
 * 
 */
package es.caib.comanda.ms.back.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Objecte que conté la informació dels missatges d'error de l'API REST.
 *
 * TODO Fer aquesta classe compatible amb Problem JSON (https://datatracker.ietf.org/doc/html/rfc7807)
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

	protected final int status;
	protected final String title;
	protected String stackTrace;

	public String getMessage() {
		return title;
	}

}
