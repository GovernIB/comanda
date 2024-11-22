package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.configuracio.logic.intf.model.*;
import es.caib.comanda.configuracio.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.configuracio.logic.intf.service.PermissionEvaluatorService;
import es.caib.comanda.configuracio.logic.intf.service.ReadonlyResourceService;
import es.caib.comanda.configuracio.logic.intf.service.ResourceApiService;
import es.caib.comanda.configuracio.logic.intf.util.HttpRequestUtil;
import es.caib.comanda.configuracio.logic.intf.util.TypeUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.hateoas.*;
import org.springframework.hateoas.TemplateVariable.VariableType;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * <p>Classe base pels controladors de l'API REST que conté els mètodes
 * necessaris per a consultar recursos.</p>
 * 
 * @param <R>
 *            el tipus del recurs que ha de gestionar aquest Controller. Aquest
 *            tipus ha d'estendre de EntityResource&lt;ID&gt;.
 * @param <ID>
 *            el tipus de la clau primària del recurs. Aquest tipus ha
 *            d'implementar la interfície Serializable.
 * 
 * @author Limit Tecnologies
 */
@Slf4j
public abstract class BaseReadonlyResourceController<R extends Resource<? extends Serializable>, ID extends Serializable>
	implements ReadonlyResourceController<R, ID> {

	@Autowired
	protected ReadonlyResourceService<R, ID> readonlyResourceService;
	@Autowired
	protected ResourceApiService resourceApiService;

	private Class<R> resourceClass;

	@PostConstruct
	public void registerResourceService() {
		resourceApiService.resourceRegister(getResourceClass());
	}

	@Override
	@GetMapping(value = "/{resourceId}")
	@Operation(summary = "Consulta la informació d'un recurs")
	@PreAuthorize("hasPermission(#resourceId, this.getResourceClass().getName(), this.getOperation('GET_ONE'))")
	public ResponseEntity<EntityModel<R>> getOne(
			@PathVariable
			@Parameter(description = "Identificador del recurs")
			final ID resourceId,
			@RequestParam(value = "perspective", required = false)
			final String[] perspectives) {
		log.debug("Obtenint entitat (resourceId={})", resourceId);
		R resource = getReadonlyResourceService().getOne(
				resourceId,
				perspectives);
		EntityModel<R> entityModel = toEntityModel(
				resource,
				buildSingleResourceLinks(
						resource.getId(),
						perspectives,
						resourceApiService.permissionsCurrentUser(
								getResourceClass(),
								null)).toArray(new Link[0]));
		return ResponseEntity.ok(entityModel);
	}

	@Override
	@GetMapping
	@Operation(summary = "Consulta paginada de recursos")
	@PreAuthorize("hasPermission(null, this.getResourceClass().getName(), this.getOperation('FIND'))")
	public ResponseEntity<PagedModel<EntityModel<R>>> find(
			@RequestParam(value = "quickFilter", required = false)
			@Parameter(description = "Filtre ràpid (text)")
			final String quickFilter,
			@RequestParam(value = "filter", required = false)
			@Parameter(description = "Consulta en format Spring Filter")
			final String filter,
			@RequestParam(value = "namedQuery", required = false)
			@Parameter(description = "Consultes predefinides")
			final String[] namedQueries,
			@RequestParam(value = "perspective", required = false)
			@Parameter(description = "Perspectives de la consulta")
			final String[] perspectives,
			@Parameter(description = "Paràmetres per a la paginació dels resultats", required = false)
			final Pageable pageable) {
		log.debug("Consultant entitats amb filtre i paginació (quickFilter={},filter={},namedQueries={},perspectives={},pageable={})",
				quickFilter,
				filter,
				Arrays.toString(namedQueries),
				Arrays.toString(perspectives),
				pageable);
		ResourcePermissions resourcePermissions = resourceApiService.permissionsCurrentUser(
				getResourceClass(),
				null);
		if (pageable != null) {
			// Només es fa la consulta de recursos si la petició conté
			// informació depaginació.
			long t0 = System.currentTimeMillis();
			long initialTime = t0;
			String filterWithFieldParams = filterWithFieldParameters(filter, getResourceClass());
			Page<R> page = getReadonlyResourceService().findPage(
					filterWithFieldParams,
					namedQueries,
					perspectives,
					pageable);
			long queryTime = System.currentTimeMillis() - t0;
			log.trace("\ttemps de consulta: {}ms", queryTime);
			t0 = System.currentTimeMillis();
			PagedModel<EntityModel<R>> pagedModel = toPagedModel(
					page,
					perspectives,
					resourcePermissions,
					buildResourceCollectionLinks(
							quickFilter,
							filter,
							namedQueries,
							perspectives,
							pageable,
							page,
							resourcePermissions).toArray(new Link[0]));
			long conversionTime = System.currentTimeMillis() - t0;
			log.trace("\ttemps de conversió dels resultats: {}ms", conversionTime);
			long totalTime = System.currentTimeMillis() - initialTime;
			log.trace("\ttemps total:{}ms", totalTime);
			log.trace("\tnúm. resultats:{}", page.getTotalElements());
			return ResponseEntity.ok(pagedModel);
		} else {
			// Si la petició no conté informació depaginació únicament es
			// retornen els ellaços a les possibles accions sobre aquest
			// recurs.
			return ResponseEntity.ok(
					PagedModel.empty(
							buildResourceCollectionLinks(
									quickFilter,
									filter,
									namedQueries,
									perspectives,
									null,
									null,
									resourcePermissions)));
		}
	}

	public Class<R> getResourceClass() {
		if (resourceClass == null) {
			resourceClass = (Class<R>)TypeUtil.getArgumentTypeFromGenericSuperclass(
					getClass(),
					BaseReadonlyResourceController.class,
					0);
		}
		return resourceClass;
	}

	public PermissionEvaluatorService.RestApiOperation getOperation(String operationName) {
		return operationName != null ? PermissionEvaluatorService.RestApiOperation.valueOf(operationName) : null;
	}

	protected ReadonlyResourceService<R, ID> getReadonlyResourceService() {
		return readonlyResourceService;
	}

	protected EntityModel<R> toEntityModel(
			R resource,
			Link... links) {
		return EntityModel.of(
				resource,
				links);
	}
	protected PagedModel<EntityModel<R>> toPagedModel(
			Page<R> page,
			String[] perspectives,
			ResourcePermissions resourcePermissions,
			Link... links) {
		return PagedModel.of(
				page.getContent().stream().map(resource -> {
					Link[] resourceLinks = resource != null ? buildSingleResourceLinks(
							resource.getId(),
							perspectives,
							resourcePermissions).toArray(new Link[0]) : new Link[0];
					return toEntityModel(resource, resourceLinks);
				}).collect(Collectors.toList()),
				new PagedModel.PageMetadata(
						page.getNumberOfElements(),
						page.getNumber(),
						page.getTotalElements(),
						page.getTotalPages()),
				links);
	}

	protected List<Link> buildSingleResourceLinks(
			Serializable id,
			String[] perspective,
			ResourcePermissions resourcePermissions) {
		List<Link> ls = new ArrayList<>();
		Link selfLink = linkTo(methodOn(getClass()).getOne(id, perspective)).withSelfRel();
		Map<String, Object> expandMap = new HashMap<>();
		expandMap.put("perspective", perspective);
		ls.add(selfLink.expand(expandMap));
		return ls;
	}

	protected List<Link> buildResourceCollectionLinks(
			String quickFilter,
			String filter,
			String[] namedQuery,
			String[] perspective,
			Pageable pageable,
			Page<?> page,
			ResourcePermissions resourcePermissions) {
		List<Link> ls = new ArrayList<>();
		if (pageable == null) {
			// Enllaços que es retornen quan no es fa cap consulta
			Link selfLink = buildFindLinkWithParams(
					linkTo(getClass()).withSelfRel(),
					null,
					null,
					null,
					null,
					null);
			if (resourcePermissions.isReadGranted()) {
				ls.add(Affordances.of(selfLink).
						afford(HttpMethod.GET).
						withOutput(getResourceClass()).
						withName(selfLink.getRel().value()).
						toLink());
				// Els enllaços de les accions find, getOne i create només es
				// retornen si a la petició s'ha especificat informació de
				// paginació.
				ls.add(buildFindLink("find"));
				Link getOneLink = linkTo(methodOn(getClass()).getOne(null, null)).withRel("getOne");
				String getOneLinkHref = getOneLink.getHref().replace("perspective", "perspective*");
				ls.add(Link.of(UriTemplate.of(getOneLinkHref), "getOne"));
			} else {
				ls.add(selfLink);
			}
		} else {
			// Enllaços que es retornen amb els resultats de la consulta
			Link selfLink = buildFindLinkWithParams(
					linkTo(getClass()).withSelfRel(),
					quickFilter,
					filter,
					namedQuery,
					perspective,
					pageable);
			if (resourcePermissions.isReadGranted()) {
				ls.add(Affordances.of(selfLink).
						afford(HttpMethod.GET).
						withOutput(getResourceClass()).
						withName(selfLink.getRel().value()).
						toLink());
				if (pageable.isPaged()) {
					if (pageable.getPageNumber() < page.getTotalPages()) {
						if (!page.isFirst()) {
							ls.add(
									buildFindLinkWithParams(
											linkTo(getClass()).withRel("first"),
											quickFilter,
											filter,
											namedQuery,
											perspective,
											pageable.first()));
						}
						if (page.hasPrevious()) {
							ls.add(
									buildFindLinkWithParams(
											linkTo(getClass()).withRel("previous"),
											quickFilter,
											filter,
											namedQuery,
											perspective,
											pageable.previousOrFirst()));
						}
						if (page.hasNext()) {
							ls.add(
									buildFindLinkWithParams(
											linkTo(getClass()).withRel("next"),
											quickFilter,
											filter,
											namedQuery,
											perspective,
											pageable.next()));
						}
						if (!page.isLast()) {
							ls.add(
									buildFindLinkWithParams(
											linkTo(getClass()).withRel("last"),
											quickFilter,
											filter,
											namedQuery,
											perspective,
											PageRequest.of(page.getTotalPages() - 1, pageable.getPageSize())));
						}
						if (page.getTotalElements() > 0 && page.getTotalPages() > 1) {
							Link findLink = buildFindLinkWithParams(
									linkTo(getClass()).withRel("toPageNumber"),
									quickFilter,
									filter,
									namedQuery,
									perspective,
									PageRequest.of(0, pageable.getPageSize()));
							// Al link generat li eliminam la variable page amb el valor 0
							String findLinkHref = findLink.getHref().replace("page=0&", "").replace("page=0", "");
							TemplateVariables findTemplateVariables = new TemplateVariables(
									new TemplateVariable("page", VariableType.REQUEST_PARAM));
							// I a més hi afegim la variable page
							ls.add(
									Link.of(
											UriTemplate.of(findLinkHref).with(findTemplateVariables),
											"toPageNumber"));
						}
					}
				}
			} else {
				ls.add(selfLink);
			}
		}
		return ls;
	}

	protected Link buildFindLink(String rel) {
		Link findLink = linkTo(methodOn(getClass()).find(null, null, null, null, null)).withRel(rel);
		// Al link generat li canviam les variables namedQuery i
		// perspective perquè no les posa com a múltiples.
		String findLinkHref = findLink.getHref().
				replace("namedQuery", "namedQuery*").
				replace("perspective", "perspective*").
				replace("field", "field*");
		// I a més hi afegim les variables page, size i sort que no les
		// detecta a partir de la classe de tipus Pageable
		TemplateVariables findTemplateVariables = new TemplateVariables(
				new TemplateVariable("page", VariableType.REQUEST_PARAM),
				new TemplateVariable("size", VariableType.REQUEST_PARAM),
				new TemplateVariable("sort", VariableType.REQUEST_PARAM).composite());
		return Link.of(UriTemplate.of(findLinkHref).with(findTemplateVariables), rel);
	}

	protected Link buildFindLinkWithParams(
			Link baseLink,
			String quickFilter,
			String filter,
			String[] namedQuery,
			String[] perspective,
			Pageable pageable) {
		Map<String, Object> expandMap = new HashMap<>();
		if (pageable != null) {
			if (pageable.isPaged()) {
				expandMap.put("page", pageable.getPageNumber());
				expandMap.put("size", pageable.getPageSize());
			} else {
				expandMap.put("page", "UNPAGED");
			}
		}
		if (pageable != null && pageable.getSort() != null) {
			expandMap.put(
					"sort",
					pageable.getSort().stream().
					map(o -> o.getProperty() + "," + o.getDirection().name().toLowerCase()).
					collect(Collectors.toList()));
		}
		if (quickFilter != null) {
			expandMap.put("quickFilter", quickFilter);
		}
		if (filter != null) {
			expandMap.put("filter", filter);
		}
		if (namedQuery != null) {
			expandMap.put("namedQuery", Arrays.asList(namedQuery));
		}
		if (perspective != null) {
			expandMap.put("perspective", Arrays.asList(perspective));
		}
		return Link.of(
				UriTemplate.of(
						baseLink.toUri().toString(),
						new TemplateVariables(
								TemplateVariable.requestParameter("page"),
								TemplateVariable.requestParameter("size"),
								TemplateVariable.requestParameter("sort").composite(),
								TemplateVariable.requestParameter("quickFilter"),
								TemplateVariable.requestParameter("query"),
								TemplateVariable.requestParameter("filter"),
								TemplateVariable.requestParameter("namedQuery").composite(),
								TemplateVariable.requestParameter("perspective").composite())),
				baseLink.getRel()).
				expand(expandMap);
	}

	private String filterWithFieldParameters(String filter, Class<?> resourceClass) {
		Optional<HttpServletRequest> request = HttpRequestUtil.getCurrentHttpRequest();
		Set<String> paramNames = request.get().getParameterMap().keySet();
		List<String> filterResourceFields = new ArrayList<String>();
		ReflectionUtils.doWithFields(resourceClass, new FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				int modifiers = field.getModifiers();
				boolean isStaticFinal = Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
				if (!isStaticFinal) {
					String fieldName = field.getName();
					boolean hasLikeParam = paramNames.contains(fieldName + "~");
					boolean hasBetweenParams = paramNames.contains(fieldName + "1") && paramNames.contains(fieldName + "2");
					if (paramNames.contains(fieldName) || hasLikeParam || hasBetweenParams) {
						filterResourceFields.add(field.getName());
					}
				}
			}
		});
		String[] springFilterParts = filterResourceFields.stream().
				map(p -> requestParamResourceFieldToSpringFilter(p, resourceClass)).
				filter(Objects::nonNull).
				toArray(String[]::new);
		if (springFilterParts.length > 0) {
			String requestParamsFilter = String.join(" and ", springFilterParts);
			if (filter != null && !filter.isBlank()) {
				return "(" + filter + ") and (" + requestParamsFilter + ")";
			} else {
				return requestParamsFilter;
			}
		} else {
			return filter;
		}
	}

	private String requestParamResourceFieldToSpringFilter(String requestParamResource, Class<?> resourceClass) {
		Optional<HttpServletRequest> request = HttpRequestUtil.getCurrentHttpRequest();
		Field field = ReflectionUtils.findField(resourceClass, requestParamResource);
		boolean isFieldReferenceType = field.getType().isAssignableFrom(ResourceReference.class);
		boolean isFieldTextType = field.isEnumConstant() ||
				field.getType().isAssignableFrom(String.class) ||
				field.getType().isAssignableFrom(Date.class) ||
				field.getType().isAssignableFrom(LocalDate.class) ||
				field.getType().isAssignableFrom(LocalDateTime.class) ||
				field.getType().isAssignableFrom(ZonedDateTime.class) ||
				field.getType().isAssignableFrom(Instant.class) ||
				field.getType().isAssignableFrom(YearMonth.class) ||
				field.getType().isAssignableFrom(MonthDay.class);
		if (isFieldReferenceType) {
			String value = request.get().getParameter(requestParamResource);
			return "(" + requestParamResource + ".id:" + value + ")";
		} else {
			String value = request.get().getParameter(requestParamResource);
			if (isFieldTextType && value != null) value = "'" + value + "'";
			String value1 = request.get().getParameter(requestParamResource + "1");
			if (isFieldTextType && value1 != null) value1 = "'" + value1 + "'";
			String value2 = request.get().getParameter(requestParamResource + "2");
			if (isFieldTextType && value2 != null) value2 = "'" + value2 + "'";
			String valueLike = request.get().getParameter(requestParamResource + "~");
			if (value != null) {
				return "(" + requestParamResource + ":" + value + ")";
			} else if (value1 != null && value2 != null) {
				return "(" + requestParamResource + ">:" + value1 + " and " + requestParamResource + "<:" + value2 + ")";
			} else if (valueLike != null) {
				return "(" + requestParamResource + "~'*" + valueLike + "*')";
			} else {
				return null;
			}
		}
	}

	@Getter
	@AllArgsConstructor
	private static class FieldAndClass {
		Field field;
		Class<?> clazz;
	}

}
