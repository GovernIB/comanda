package es.caib.comanda.configuracio.back.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.configuracio.back.config.WebMvcConfig;
import es.caib.comanda.configuracio.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.configuracio.logic.intf.annotation.ResourceField;
import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.configuracio.logic.intf.exception.ArtifactNotFoundException;
import es.caib.comanda.configuracio.logic.intf.exception.ComponentNotFoundException;
import es.caib.comanda.configuracio.logic.intf.exception.ResourceFieldNotFoundException;
import es.caib.comanda.configuracio.logic.intf.model.*;
import es.caib.comanda.configuracio.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.configuracio.logic.intf.service.MutableResourceService;
import es.caib.comanda.configuracio.logic.intf.service.ReadonlyResourceService;
import es.caib.comanda.configuracio.logic.intf.service.ResourceServiceLocator;
import es.caib.comanda.configuracio.logic.intf.util.JsonUtil;
import es.caib.comanda.configuracio.logic.intf.util.RequestSessionUtil;
import es.caib.comanda.configuracio.logic.intf.util.TypeUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.mediatype.ConfigurableAffordance;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * <p>Classe base pels controladors de l'API REST que conté els mètodes
 * necessaris per a modificar recursos.</p>
 * 
 * @param <R>
 *            el tipus del recurs que ha de gestionar aquest Controller. Aquest
 *            tipus ha d'estendre de EntityResource&lt;ID&gt;.
 * @param <ID>
 *            el tipus de la clau primària del recurs. Aquest tipus ha
 *            d'implementar la interfície Serializable.
 * 
 * @author Josep Gayà
 */
@Slf4j
public abstract class BaseMutableResourceController<R extends Resource<? extends Serializable>, ID extends Serializable>
	extends BaseReadonlyResourceController<R, ID>
	implements MutableResourceController<R, ID> {

	@Value("${" + BaseConfig.PROP_HTTP_HEADER_ANSWERS + ":Bb-Answers}")
	private String httpHeaderAnswers;

	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected SmartValidator validator;

	private ExpressionParser parser;

	@Override
	@PostMapping
	@Operation(summary = "Crea un nou recurs")
	@PreAuthorize("hasPermission(null, this.getResourceClass().getName(), this.getOperation('CREATE'))")
	public ResponseEntity<EntityModel<R>> create(
			@RequestBody
			@Validated({ Resource.OnCreate.class, Default.class })
			final R resource) {
		log.debug("Creant recurs (resource={})", resource);
		R created = getMutableResourceService().create(
				resource,
				getAnswersFromHeaderOrRequest(null));
		final URI uri = MvcUriComponentsBuilder.fromController(getClass()).
				path("/{id}").
				buildAndExpand(created.getId()).
				toUri();
		return ResponseEntity.created(uri).body(
				toEntityModel(
						created,
						buildSingleResourceLinks(
								created.getId(),
								null,
								null,
								resourceApiService.permissionsCurrentUser(
										getResourceClass(),
										null)).toArray(new Link[0])));
	}

	@Override
	@PutMapping(value = "/{id}")
	@Operation(summary = "Modifica tots els camps d'un recurs")
	@PreAuthorize("hasPermission(#id, this.getResourceClass().getName(), this.getOperation('UPDATE'))")
	public ResponseEntity<EntityModel<R>> update(
			@PathVariable
			@Parameter(description = "Identificador del recurs")
			final ID id,
			@RequestBody
			final R resource,
			BindingResult bindingResult) throws MethodArgumentNotValidException {
		log.debug("Modificant recurs (id={}, resource={})", id, resource);
		updateResourceIdAndPk(id, resource);
		validator.validate(
				resource,
				bindingResult,
				Resource.OnUpdate.class,
				Default.class);
		if (bindingResult.hasErrors()) {
			throw new MethodArgumentNotValidException(
					new MethodParameter(
							new Object() {}.getClass().getEnclosingMethod(),
							2),
					bindingResult);
		} else {
			R updated = getMutableResourceService().update(
					id,
					resource,
					getAnswersFromHeaderOrRequest(null));
			return ResponseEntity.ok(
					toEntityModel(
							updated,
							buildSingleResourceLinks(
									updated.getId(),
									null,
									null,
									resourceApiService.permissionsCurrentUser(
											getResourceClass(),
											id)).toArray(new Link[0])));
		}
	}

	@Override
	@PatchMapping(value = "/{id}")
	@Operation(summary = "Modifica parcialment un recurs")
	@PreAuthorize("hasPermission(#id, this.getResourceClass().getName(), this.getOperation('PATCH'))")
	public ResponseEntity<EntityModel<R>> patch(
			@PathVariable
			@Parameter(description = "Identificador del recurs")
			final ID id,
			@RequestBody
			final JsonNode jsonNode,
			BindingResult bindingResult) throws JsonProcessingException, MethodArgumentNotValidException {
		log.debug("Modificant parcialment el recurs (id={}, jsonNode={})", id, jsonNode);
		R resource = getMutableResourceService().getOne(id, null);
		fillResourceWithFieldsMap(
				resource,
				JsonUtil.getInstance().fromJsonToMap(jsonNode, getResourceClass()));
		validateResource(
				resource,
				1,
				bindingResult,
				Resource.OnUpdate.class,
				Default.class);
		R updated = getMutableResourceService().update(
				id,
				resource,
				getAnswersFromHeaderOrRequest(null));
		return ResponseEntity.ok(
				toEntityModel(
						updated,
						buildSingleResourceLinks(
								updated.getId(),
								null,
								null,
								resourceApiService.permissionsCurrentUser(
										getResourceClass(),
										id)).toArray(new Link[0])));
	}

	@Override
	@DeleteMapping(value = "/{id}")
	@Operation(summary = "Esborra un recurs")
	@PreAuthorize("hasPermission(#id, this.getResourceClass().getName(), this.getOperation('DELETE'))")
	public ResponseEntity<?> delete(
			@PathVariable
			@Parameter(description = "Identificador del recurs")
			final ID id) {
		log.debug("Esborrant recurs (id={})", id);
		getMutableResourceService().delete(
				id,
				getAnswersFromHeaderOrRequest(null));
		return ResponseEntity.ok().build();
	}

	@Override
	@PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Processa els canvis en els camps del recurs")
	@PreAuthorize("hasPermission(null, this.getResourceClass().getName(), this.getOperation('ONCHANGE'))")
	public ResponseEntity<String> onChange(
			@RequestBody @Valid
			final OnChangeEvent onChangeEvent) throws JsonProcessingException {
		log.debug("Processant canvis en els camps del recurs (onChangeEvent={})", onChangeEvent);
		Class<R> resourceClass = getResourceClass();
		R previous = null;
		if (onChangeEvent.getPrevious() != null) {
			previous = (R)ReflectUtils.newInstance(resourceClass);
			JsonUtil.getInstance().fillResourceWithFieldsMap(
					previous,
					JsonUtil.getInstance().fromJsonToMap(onChangeEvent.getPrevious(), resourceClass),
					null,
					null);
		}
		Object fieldValueObject = JsonUtil.getInstance().fillResourceWithFieldsMap(
				ReflectUtils.newInstance(resourceClass),
				null,
				onChangeEvent.getFieldName(),
				onChangeEvent.getFieldValue());
		Map<String, AnswerRequiredException.AnswerValue> answers = getAnswersFromHeaderOrRequest(onChangeEvent.getAnswers());
		Map<String, Object> processat = getMutableResourceService().onChange(
				previous,
				onChangeEvent.getFieldName(),
				fieldValueObject,
				answers);
		if (processat != null) {
			String serialized = objectMapper.writeValueAsString(new OnChangeForSerialization(processat));
			String response = serialized.substring(serialized.indexOf("\":{") + 2, serialized.length() - 1);
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.ok("{}");
		}
	}

	@Override
	@GetMapping(value = "/fields/{fieldName}/options")
	@Operation(summary = "Consulta paginada de les opcions disponibles per a emplenar un camp de tipus ResourceReference")
	@PreAuthorize("hasPermission(null, this.getResourceClass().getName(), this.getOperation('OPTIONS'))")
	public <RR extends Resource<?>> ResponseEntity<PagedModel<EntityModel<RR>>> fieldOptionsFind(
			@PathVariable
			@Parameter(description = "Nom del camp")
			final String fieldName,
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
			@Parameter(description = "Paginació dels resultats", required = false)
			final Pageable pageable) {
		log.debug("Consultant possibles valors del camp amb filtre i paginació (" +
				"fieldName={},quickFilter={},filter={},namedQueries={},perspectives={},pageable={})",
				fieldName,
				quickFilter,
				filter,
				namedQueries,
				perspectives,
				pageable);
		Class<?> resourceClass = getResourceClass();
		Optional<FieldAndClass> referencedResourceFieldAndClass = findReferenceFieldAndClass(resourceClass, fieldName);
		if (referencedResourceFieldAndClass.isPresent()) {
			Link resourceCollectionBaseSelfLink = linkTo(methodOn(getClass()).fieldOptionsFind(
					fieldName,
					quickFilter,
					filter,
					namedQueries,
					perspectives,
					pageable)).withSelfRel();
			if (pageable != null) {
				// Només es fa la consulta de recursos si la petició conté informació de paginació.
				Class<?> referencedResourceClass = referencedResourceFieldAndClass.get().getClazz();
				ReadonlyResourceService<RR, ?> resourceService = (ReadonlyResourceService<RR, ?>) ResourceServiceLocator.getInstance().
						getReadOnlyEntityResourceServiceForResourceClass(referencedResourceClass);
				Page<RR> page = resourceService.findPage(
						quickFilter,
						fieldOptionsProcessedFilterWithFieldAnnotation(
								referencedResourceFieldAndClass.get().getField(),
								filter),
						fieldOptionsProcessedNamedQueriesWithFieldAnnotation(
								referencedResourceFieldAndClass.get().getField(),
								namedQueries),
						perspectives,
						fieldOptionsProcessedPageableWithResourceAnnotation(
								pageable,
								referencedResourceClass));
				Link singleResourceSelfLink = linkTo(methodOn(getClass()).fieldOptionsGetOne(
						fieldName,
						SELF_RESOURCE_ID_TOKEN,
						null)).withSelfRel();
				return ResponseEntity.ok(
						toPagedModel(
								page,
								perspectives,
								singleResourceSelfLink,
								ResourcePermissions.readOnly(),
								buildOptionsLinks(
										quickFilter,
										filter,
										namedQueries,
										perspectives,
										pageable,
										page,
										resourceCollectionBaseSelfLink,
										ResourcePermissions.readOnly()).toArray(new Link[0])));
			} else {
				// Si la petició no conté informació depaginació únicament es retornen els ellaços
				// a les possibles accions sobre aquest recurs.
				return ResponseEntity.ok(
						PagedModel.empty(
								buildOptionsLinks(
										quickFilter,
										filter,
										namedQueries,
										perspectives,
										null,
										null,
										resourceCollectionBaseSelfLink,
										ResourcePermissions.readOnly())));
			}
		}
		throw new ResourceFieldNotFoundException(resourceClass, fieldName);
	}

	@Override
	@GetMapping(value = "/fields/{fieldName}/options/{id}")
	@Operation(summary = "Consulta d'una de les opcions disponibles per a emplenar un camp de tipus ResourceReferencee")
	@PreAuthorize("hasPermission(null, this.getResourceClass().getName(), this.getOperation('OPTIONS'))")
	public <RR extends Resource<RID>, RID extends Serializable> ResponseEntity<EntityModel<RR>> fieldOptionsGetOne(
			@PathVariable
			@Parameter(description = "Nom del camp")
			final String fieldName,
			@PathVariable
			@Parameter(description = "Id de l'element")
			final RID id,
			@RequestParam(value = "perspective", required = false)
			@Parameter(description = "Perspectives de la consulta")
			final String[] perspectives) {
		log.debug("Consultant un dels possibles valors del camp (fieldName={},id={},perspectives={})",
				fieldName,
				id,
				perspectives);
		Class<?> resourceClass = getResourceClass();
		Optional<FieldAndClass> referencedResourceFieldAndClass = findReferenceFieldAndClass(resourceClass, fieldName);
		if (referencedResourceFieldAndClass.isPresent()) {
			Class<?> referencedResourceClass = referencedResourceFieldAndClass.get().getClazz();
			ReadonlyResourceService<RR, RID> resourceService = (ReadonlyResourceService<RR, RID>)ResourceServiceLocator.getInstance().
					getReadOnlyEntityResourceServiceForResourceClass(referencedResourceClass);
			RR resource = resourceService.getOne(id, perspectives);
			Link singleResourceSelfLink = linkTo(methodOn(getClass()).fieldOptionsGetOne(
					fieldName,
					SELF_RESOURCE_ID_TOKEN,
					null)).withSelfRel();
			EntityModel<RR> entityModel = toEntityModel(
					resource,
					buildSingleResourceLinks(
							resource.getId(),
							perspectives,
							singleResourceSelfLink,
							ResourcePermissions.readOnly()).toArray(new Link[0]));
			return ResponseEntity.ok(entityModel);
		}
		throw new ResourceFieldNotFoundException(resourceClass, fieldName);
	}

	@Override
	@PostMapping("/artifacts/action/{code}")
	@Operation(summary = "Execució d'una acció associada a un recurs")
	@PreAuthorize("hasPermission(null, this.getResourceClass().getName(), this.getOperation('ACTION'))")
	public ResponseEntity<?> artifactActionExec(
			@PathVariable
			@Parameter(description = "Codi de l'acció")
			final String code,
			@RequestBody(required = false)
			final JsonNode params,
			BindingResult bindingResult) throws ArtifactNotFoundException, JsonProcessingException, MethodArgumentNotValidException {
		Object paramsObject = getArtifactFormClass(
				ResourceArtifactType.ACTION,
				code,
				params,
				bindingResult);
		Object result = getMutableResourceService().actionExec(code, paramsObject);
		return ResponseEntity.ok(result);
	}

	protected MutableResourceService<R, ID> getMutableResourceService() {
		if (readonlyResourceService instanceof MutableResourceService) {
			return (MutableResourceService<R, ID>)readonlyResourceService;
		} else {
			throw new ComponentNotFoundException(MutableResourceService.class, getResourceClass().getName());
		}
	}

	@Override
	protected List<Link> buildSingleResourceLinks(
			Serializable id,
			String[] perspective,
			Link singleResourceSelfLink,
			ResourcePermissions resourcePermissions) {
		List<Link> links = super.buildSingleResourceLinks(
				id,
				perspective,
				singleResourceSelfLink,
				resourcePermissions);
		Link selfLink = links.stream().
				filter(l -> l.getRel().value().equals("self")).
				findFirst().orElse(null);
		if (selfLink != null) {
			if (resourcePermissions.isWriteGranted()) {
				ConfigurableAffordance affordance = Affordances.of(selfLink).
						afford(HttpMethod.OPTIONS).
						withName("default").
						andAfford(HttpMethod.PUT).
						withInputAndOutput(getResourceClass()).
						withName("update").
						andAfford(HttpMethod.PATCH).
						withInputAndOutput(getResourceClass()).
						withName("patch");
				if (resourcePermissions.isDeleteGranted()) {
					affordance = affordance.
							andAfford(HttpMethod.DELETE).
							withName("delete");
				}
				links.set(links.indexOf(selfLink), affordance.toLink());
			} else if (resourcePermissions.isDeleteGranted()) {
				links.set(
						links.indexOf(selfLink),
						Affordances.of(selfLink).
								afford(HttpMethod.OPTIONS).
								withName("default").
								andAfford(HttpMethod.DELETE).
								withName("delete").
								toLink());
			}
		}
		return links;
	}

	@Override
	protected List<Link> buildResourceCollectionLinks(
			String quickFilter,
			String filter,
			String[] namedQuery,
			String[] perspective,
			Pageable pageable,
			Page<?> page,
			Link resourceCollectionBaseSelfLink,
			ResourcePermissions resourcePermissions) {
		List<Link> links = super.buildResourceCollectionLinks(
				quickFilter,
				filter,
				namedQuery,
				perspective,
				pageable,
				page,
				resourceCollectionBaseSelfLink,
				resourcePermissions);
		Link selfLink = links.stream().
				filter(l -> l.getRel().value().equals("self")).
				findFirst().orElse(null);
		if (selfLink != null) {
			if (resourcePermissions.isCreateGranted()) {
				links.set(
						links.indexOf(selfLink),
						Affordances.of(selfLink).
								afford(HttpMethod.POST).
								withInputAndOutput(getResourceClass()).
								withName("create").
								toLink());
			}
		}
		return links;
	}

	protected List<Link> buildOptionsLinks(
			String quickFilter,
			String filter,
			String[] namedQuery,
			String[] perspective,
			Pageable pageable,
			Page<?> page,
			Link resourceCollectionBaseSelfLink,
			ResourcePermissions resourcePermissions) {
		return buildResourceCollectionLinks(
				quickFilter,
				filter,
				namedQuery,
				perspective,
				pageable,
				page,
				resourceCollectionBaseSelfLink,
				resourcePermissions).stream().
				filter(l -> !l.getRel().value().equals("getOne")).
				collect(Collectors.toList());
	}

	@Override
	protected Link[] buildArtifactsLinks(List<ResourceArtifact> artifacts) {
		List<Link> ls = new ArrayList<>();
		Link selfLink = linkTo(methodOn(getClass()).artifacts()).withSelfRel();
		ls.add(selfLinkWithDefaultProperties(selfLink, false));
		artifacts.forEach(a -> {
			if (ResourceArtifactType.ACTION == a.getType()) {
				ls.add(buildActionLinkWithAffordances(a));
			}
		});
		ls.addAll(Arrays.asList(super.buildArtifactsLinks(artifacts)));
		return ls.toArray(new Link[0]);
	}

	protected <T extends Resource<?>> void validateResource(
			T resource,
			int paramIndex,
			BindingResult bindingResult,
			Object... validationHints) throws MethodArgumentNotValidException {
		BindingResult resourceBindingResult = new BeanPropertyBindingResult(resource, bindingResult.getObjectName());
		Object[] finalValidationHints = validationHints;
		if (validationHints == null || validationHints.length == 0) {
			finalValidationHints = new Object[] { Default.class };
		}
		validator.validate(
				resource,
				resourceBindingResult,
				finalValidationHints);
		if (resourceBindingResult.hasErrors()) {
			bindingResult.addAllErrors(resourceBindingResult);
			throw new MethodArgumentNotValidException(
					new MethodParameter(
							new Object() {}.getClass().getEnclosingMethod(),
							paramIndex),
					bindingResult);
		}
	}

	protected void fillResourceWithFieldsMap(
			Object resource,
			Map<String, Object> fields) {
		if (fields != null) {
			fields.forEach((k, v) -> {
				Field field = ReflectionUtils.findField(resource.getClass(), k);
				if (field != null) {
					ReflectionUtils.makeAccessible(field);
					ReflectionUtils.setField(field, resource, v);
				}
			});
		}
	}

	protected Map<String, AnswerRequiredException.AnswerValue> getAnswersFromHeaderOrRequest(
			Map<String, Object> requestAnswers) {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes) {
			HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
			Map<String, AnswerRequiredException.AnswerValue> answers = null;
			if (requestAnswers != null) {
				Map<String, AnswerRequiredException.AnswerValue> answersFromRequest = requestAnswers.entrySet().stream().
						collect(Collectors.toMap(
								Map.Entry::getKey,
								e -> {
									if (e.getValue() == null) {
										return new AnswerRequiredException.AnswerValue();
									} else if (e.getValue() instanceof Boolean) {
										return new AnswerRequiredException.AnswerValue((Boolean)e.getValue());
									} else {
										return new AnswerRequiredException.AnswerValue(e.getValue().toString());
									}
								}));
				answers = new HashMap<>(answersFromRequest);
			}
			String headerAnswers = request.getHeader(httpHeaderAnswers);
			if (headerAnswers != null) {
				try {
					JsonNode headerAnswersJson = objectMapper.readTree(headerAnswers);
					Map<String, Object> headerAnswersMap = objectMapper.convertValue(
							headerAnswersJson,
							new TypeReference<>(){});
					Map<String, AnswerRequiredException.AnswerValue> answersFromHeader = headerAnswersMap.entrySet().stream().
							collect(Collectors.toMap(
									Map.Entry::getKey,
									e -> {
										if (e.getValue() == null) {
											return new AnswerRequiredException.AnswerValue();
										} else if (e.getValue() instanceof Boolean) {
											return new AnswerRequiredException.AnswerValue((Boolean)e.getValue());
										} else {
											return new AnswerRequiredException.AnswerValue(e.getValue().toString());
										}
									}));
					if (answers == null) {
						answers = new HashMap<>();
					}
					answers.putAll(answersFromHeader);
				} catch (JsonProcessingException ex) {
					log.warn("Error al parsejar la capçalera {}", httpHeaderAnswers, ex);
					return null;
				}
			}
			return answers;
		} else {
			return null;
		}
	}

	private Optional<FieldAndClass> findReferenceFieldAndClass(
			Class<?> resourceClass,
			String fieldName) {
		if (!fieldName.isBlank()) {
			String currentFieldName;
			if (fieldName.contains(".")) {
				String[] fieldNameParts = fieldName.split("\\.");
				currentFieldName = fieldNameParts[0];
			} else {
				currentFieldName = fieldName;
			}
			Field currentField = ReflectionUtils.findField(resourceClass, currentFieldName);
			if (currentField != null && ResourceReference.class.isAssignableFrom(currentField.getType())) {
				Class<?> referencedResourceClass = TypeUtil.getReferencedResourceClass(currentField);
				if (fieldName.contains(".")) {
					return findReferenceFieldAndClass(
							referencedResourceClass,
							fieldName.substring(currentFieldName.length() + 1));
				} else {
					return Optional.of(
							new FieldAndClass(currentField, referencedResourceClass));
				}
			}
		}
		return Optional.empty();
	}

	private String fieldOptionsProcessedFilterWithFieldAnnotation(Field field, String filterFromRequest) {
		String processedFilter = filterFromRequest;
		ResourceField resourceFieldAnnotation = field.getAnnotation(ResourceField.class);
		if (resourceFieldAnnotation != null && resourceFieldAnnotation.springFilter().isEmpty()) {
			String springFilter = resourceFieldAnnotation.springFilter();
			if (springFilter.startsWith("#{") && springFilter.endsWith("}")) {
				String expressionToParse = springFilter.substring(2, springFilter.length() - 1);
				StandardEvaluationContext context = new StandardEvaluationContext(
						new ProcessedOptionsQueryExpressionContext(RequestSessionUtil.getRequestSession()));
				String parsedSpringFilter = getExpressionParser().
						parseExpression(expressionToParse).
						getValue(context, String.class);
				processedFilter = parsedSpringFilter + (filterFromRequest != null ? " and (" + filterFromRequest + ")" : "");
			} else {
				processedFilter = springFilter + (filterFromRequest != null ? " and (" + filterFromRequest + ")" : "");
			}
		}
		return processedFilter;
	}

	private String[] fieldOptionsProcessedNamedQueriesWithFieldAnnotation(Field field, String[] namedQueries) {
		List<String> processedNamedQueries = Arrays.asList(namedQueries != null ? namedQueries : new String[0]);
		ResourceField resourceFieldAnnotation = field.getAnnotation(ResourceField.class);
		if (resourceFieldAnnotation != null) {
			Collections.addAll(processedNamedQueries, resourceFieldAnnotation.namedQueries());
		}
		return processedNamedQueries.toArray(new String[0]);
	}

	private Pageable fieldOptionsProcessedPageableWithResourceAnnotation(
			Pageable pageable,
			Class<?> resourceClass) {
		if (pageable != null) {
			ResourceConfig resourceConfigAnotation = resourceClass.getAnnotation(ResourceConfig.class);
			if (resourceConfigAnotation != null) {
				Sort sort = Sort.unsorted();
				if (resourceConfigAnotation.defaultSortFields().length > 0) {
					List<Sort.Order> orders = new ArrayList<>();
					for (ResourceConfig.ResourceSort sortField: resourceConfigAnotation.defaultSortFields()) {
						orders.add(new Sort.Order(sortField.direction(), sortField.field()));
					}
					sort = Sort.by(orders);
				}/* else if (!resourceConfigAnotation.descriptionField().isEmpty()) {
					sort = Sort.by(Sort.Order.asc(resourceConfigAnotation.descriptionField()));
				}*/
				if (pageable.isPaged()) {
					return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSortOr(sort));
				} else {
					return new WebMvcConfig.UnpagedButSorted(pageable.getSortOr(sort));
				}
			} else {
				return pageable;
			}
		} else {
			return Pageable.unpaged();
		}
	}

	private void updateResourceIdAndPk(
			ID id,
			R resource) {
		// Posa valor al camp id del recurs per a assegurar que aquest
		// camp estigui emplenat a l'hora de fer validacions.
		// Això ho feim perquè res ens assegura que aquests camps tenguin valor
		// en la petició que ens arriba del front.
		Field idField = ReflectionUtils.findField(resource.getClass(), "id");
		if (idField != null) {
			ReflectionUtils.makeAccessible(idField);
			ReflectionUtils.setField(
					idField,
					resource,
					id);
		}
	}

	private ExpressionParser getExpressionParser() {
		if (parser == null) {
			parser = new SpelExpressionParser();
		}
		return parser;
	}

	@SneakyThrows
	private Link buildActionLink(ResourceArtifact artifact) {
		Link reportLink = linkTo(methodOn(getClass()).artifacts()).withSelfRel();
		return Link.of(reportLink.toUri().toString() + "/action/" + artifact.getCode()).withSelfRel();
	}
	private Link buildActionLinkWithAffordances(ResourceArtifact artifact) {
		String rel = "exec_" + artifact.getCode();
		Link reportLink = buildActionLink(artifact);
		if (artifact.getFormClass() != null) {
			return Affordances.of(Link.of(reportLink.toUri().toString()).withRel(rel)).
					afford(HttpMethod.POST).
					withInputAndOutput(artifact.getFormClass()).
					withName(rel).
					toLink();
		} else {
			return Affordances.of(Link.of(reportLink.toUri().toString()).withRel(rel)).
					afford(HttpMethod.POST).
					withName(rel).
					toLink();
		}
	}

	@Getter
	@AllArgsConstructor
	public static class OnChangeForSerialization {
		@JsonInclude
		private Map<String, Object> map;
	}

	@Getter
	@AllArgsConstructor
	private static class FieldAndClass {
		private Field field;
		private Class<?> clazz;
	}

	@Getter
	@AllArgsConstructor
	public static class ProcessedOptionsQueryExpressionContext {
		private Object session;
	}

}
