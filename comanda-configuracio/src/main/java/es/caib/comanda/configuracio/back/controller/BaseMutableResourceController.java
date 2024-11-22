package es.caib.comanda.configuracio.back.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.configuracio.logic.intf.exception.ComponentNotFoundException;
import es.caib.comanda.configuracio.logic.intf.exception.ResourceFieldNotFoundException;
import es.caib.comanda.configuracio.logic.intf.model.OnChangeEvent;
import es.caib.comanda.configuracio.logic.intf.model.Resource;
import es.caib.comanda.configuracio.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.configuracio.logic.intf.service.MutableResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 * @author Limit Tecnologies
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

	@Override
	@PostMapping
	@Operation(summary = "Crea un nou recurs")
	@PreAuthorize("hasPermission(null, this.getResourceClass().getName(), this.getOperation('CREATE'))")
	public ResponseEntity<EntityModel<R>> create(
			@RequestBody
			@Validated({ Resource.OnCreate.class, Default.class })
			final R resource) {
		log.debug("Creant recurs (resource={})", resource);
		R created = getMutableResourceService().create(resource);
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
								resourceApiService.permissionsCurrentUser(
										getResourceClass(),
										null)).toArray(new Link[0])));
	}

	@Override
	@PutMapping(value = "/{resourceId}")
	@Operation(summary = "Modifica tots els camps d'un recurs")
	@PreAuthorize("hasPermission(#resourceId, this.getResourceClass().getName(), this.getOperation('UPDATE'))")
	public ResponseEntity<EntityModel<R>> update(
			@PathVariable
			@Parameter(description = "Identificador del recurs")
			final ID resourceId,
			@RequestBody
			final R resource,
			BindingResult bindingResult) throws MethodArgumentNotValidException {
		log.debug("Modificant recurs (resourceId={}, resource={})", resourceId, resource);
		updateResourceIdAndPk(resourceId, resource);
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
					resourceId,
					resource);
			return ResponseEntity.ok(
					toEntityModel(
							updated,
							buildSingleResourceLinks(
									updated.getId(),
									null,
									resourceApiService.permissionsCurrentUser(
											getResourceClass(),
											resourceId)).toArray(new Link[0])));
		}
	}

	@Override
	@PatchMapping(value = "/{resourceId}")
	@Operation(summary = "Modifica parcialment un recurs")
	@PreAuthorize("hasPermission(#resourceId, this.getResourceClass().getName(), this.getOperation('PATCH'))")
	public ResponseEntity<EntityModel<R>> patch(
			@PathVariable
			@Parameter(description = "Identificador del recurs")
			final ID resourceId,
			@RequestBody
			final JsonNode jsonNode,
			BindingResult bindingResult) throws JsonProcessingException, MethodArgumentNotValidException {
		log.debug("Modificant parcialment el recurs (resourceId={}, jsonNode={})", resourceId, jsonNode);
		R resource = getMutableResourceService().getOne(resourceId, null);
		fillResourceWithFieldsMap(
				resource,
				fromJsonToMap(jsonNode, getResourceClass()));
		validateResource(
				resource,
				1,
				bindingResult,
				Resource.OnUpdate.class,
				Default.class);
		R updated = getMutableResourceService().update(
				resourceId,
				resource);
		return ResponseEntity.ok(
				toEntityModel(
						updated,
						buildSingleResourceLinks(
								updated.getId(),
								null,
								resourceApiService.permissionsCurrentUser(
										getResourceClass(),
										resourceId)).toArray(new Link[0])));
	}

	@Override
	@DeleteMapping(value = "/{resourceId}")
	@Operation(summary = "Esborra un recurs")
	@PreAuthorize("hasPermission(#resourceId, this.getResourceClass().getName(), this.getOperation('DELETE'))")
	public ResponseEntity<?> delete(
			@PathVariable
			@Parameter(description = "Identificador del recurs")
			final ID resourceId) {
		log.debug("Esborrant recurs (resourceId={})", resourceId);
		getMutableResourceService().delete(resourceId);
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
			fillResourceWithFieldsMap(
					previous,
					fromJsonToMap(onChangeEvent.getPrevious(), resourceClass),
					null,
					null);
		}
		Object fieldValueObject = fillResourceWithFieldsMap(
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
			ResourcePermissions resourcePermissions) {
		List<Link> links = super.buildSingleResourceLinks(
				id,
				perspective,
				resourcePermissions);
		Link selfLink = links.stream().
				filter(l -> l.getRel().value().equals("self")).
				findFirst().orElse(null);
		if (selfLink != null) {
			if (resourcePermissions.isWriteGranted()) {
				ConfigurableAffordance affordance = Affordances.of(selfLink).
						afford(HttpMethod.PUT).
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
								afford(HttpMethod.DELETE).
								withName("delete").
								toLink());
			}
		}
		return links;
	}

	protected List<Link> buildResourceCollectionLinks(
			String quickFilter,
			String filter,
			String[] namedQuery,
			String[] perspective,
			Pageable pageable,
			Page<?> page,
			ResourcePermissions resourcePermissions) {
		List<Link> links = super.buildResourceCollectionLinks(
				quickFilter,
				filter,
				namedQuery,
				perspective,
				pageable,
				page,
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

	protected Map<String, Object> fromJsonToMap(
			JsonNode jsonNode,
			Class<?> resourceClass) throws JsonProcessingException {
		if (jsonNode != null) {
			Map<String, Object> jsonMap = objectMapper.convertValue(
					jsonNode,
					new TypeReference<>(){});
			Object jsonResource = objectMapper.treeToValue(jsonNode, resourceClass);
			Map<String, Object> map = new HashMap<>();
			jsonMap.keySet().stream().forEach(k -> {
				Field field = ReflectionUtils.findField(resourceClass, k);
				if (field != null) {
					if (!k.equals("id")) {
						ReflectionUtils.makeAccessible(field);
						Object value = ReflectionUtils.getField(field, jsonResource);
						map.put(k, value);
					} else {
						// Feim això perquè el camp id no es copia (no sabem per què)
						map.put(k, jsonMap.get(k));
					}
				}
			});
			return map;
		}
		return null;
	}

	protected Object fillResourceWithFieldsMap(
			Object resource,
			Map<String, Object> fields,
			String fieldName,
			JsonNode fieldValue) {
		if (fields != null) {
			fields.forEach((k, v) -> {
				Field field = ReflectionUtils.findField(resource.getClass(), k);
				if (field != null) {
					ReflectionUtils.makeAccessible(field);
					ReflectionUtils.setField(field, resource, v);
				}
			});
		}
		Object fieldValueObject = null;
		if (fieldName != null) {
			Field field = ReflectionUtils.findField(resource.getClass(), fieldName);
			if (field != null) {
				try {
					ReflectionUtils.makeAccessible(field);
					String fieldJson = objectMapper.writeValueAsString(fieldValue);
					JsonNode jsonNode = objectMapper.readTree("{\"" + field.getName() + "\": " + fieldJson + "}");
					Object jsonResource = objectMapper.treeToValue(jsonNode, resource.getClass());
					fieldValueObject = ReflectionUtils.getField(field, jsonResource);
				} catch (JsonProcessingException ex) {
					log.error(
							"Error al parsejar el valor del camp " + fieldName + " del JSON " + fieldValue,
							ex);
				}
			} else {
				throw new ResourceFieldNotFoundException(resource.getClass(), fieldName);
			}
		}
		return fieldValueObject;
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

	private void updateResourceIdAndPk(
			ID resourceId,
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
					resourceId);
		}
	}

	@Getter
	@AllArgsConstructor
	public static class OnChangeForSerialization {
		@JsonInclude
		private Map<String, Object> map;
	}

}
