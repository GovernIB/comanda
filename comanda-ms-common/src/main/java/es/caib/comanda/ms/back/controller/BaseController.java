package es.caib.comanda.ms.back.controller;


import es.caib.comanda.ms.logic.intf.service.PermissionEvaluatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;

/**
 * Classe base pels controladors de l'API REST.
 *
 * @author Límit Tecnologies
 */
@Slf4j
public abstract class BaseController {

	boolean isVisibleInApiIndex() {
		return true;
	}

	public boolean isPublic() {
		return false;
	}

	public PermissionEvaluatorService.RestApiOperation getOperation(String operationName) {
		return operationName != null ? PermissionEvaluatorService.RestApiOperation.valueOf(operationName) : null;
	}

	protected abstract Link getIndexLink();

}
