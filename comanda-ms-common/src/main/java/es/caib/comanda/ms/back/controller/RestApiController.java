package es.caib.comanda.ms.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.model.Resource;
import es.caib.comanda.ms.logic.intf.service.ResourceApiService;
import es.caib.comanda.ms.logic.intf.util.TypeUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Hidden
@RestController
@Profile("!visualizacio")
@RequestMapping(BaseConfig.API_PATH)
public class RestApiController {

	@Autowired
	protected ResourceApiService resourceApiService;

	@Autowired(required = false)
	protected List<BaseController> baseControllers;

    @GetMapping(BaseConfig.PING_PATH)
    public ResponseEntity<Void> ping() {
        return ResponseEntity.ok().build();
    }

	@GetMapping
	@Operation(summary = "Consulta l'índex de serveis de l'aplicació")
	public ResponseEntity<CollectionModel<?>> index() {
		List<Class<? extends Resource<?>>> allowedResourceClasses = resourceApiService.resourceFindAllowed();
		List<Link> indexLinks = baseControllers.stream().
				filter(bc -> bc.isVisibleInApiIndex() && isBaseControllerAllowed(bc, allowedResourceClasses)).
				map(BaseController::getIndexLink).
				collect(Collectors.toList());
		indexLinks.add(0, linkTo(ClassUtils.getUserClass(methodOn(getClass()).index())).withSelfRel());
		CollectionModel<?> resources = CollectionModel.of(
				Collections.emptySet(),
				indexLinks.toArray(Link[]::new));
		return ResponseEntity.ok(resources);
	}

	private boolean isBaseControllerAllowed(
			BaseController baseController,
			List<Class<? extends Resource<?>>> allowedResourceClasses) {
		if (baseController instanceof ReadonlyResourceController) {
			Class<?> controllerResourceClass = TypeUtil.getArgumentClassFromGenericSuperclass(
					baseController.getClass(),
					ReadonlyResourceController.class,
					0);
			return allowedResourceClasses.contains(controllerResourceClass);
		} else {
			return true;
		}
	}

}
