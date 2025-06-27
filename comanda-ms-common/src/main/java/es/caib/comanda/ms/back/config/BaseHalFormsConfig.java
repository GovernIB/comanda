package es.caib.comanda.ms.back.config;


import es.caib.comanda.ms.back.controller.MutableResourceController;
import es.caib.comanda.ms.back.controller.ReadonlyResourceController;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceField;
import es.caib.comanda.ms.logic.intf.model.Resource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.intf.util.TypeUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Configuració de HAL-FORMS.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Configuration
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL_FORMS)
public abstract class BaseHalFormsConfig {

	@Lazy
	@Autowired(required = false)
	private Set<ReadonlyResourceController> resourceControllers;

	@Bean
	@Lazy
	HalFormsConfiguration halFormsConfiguration() {
		return createHalFormsConfiguration(
			TypeUtil.findAssignableClasses(
				ReadonlyResourceController.class,
				getControllerPackages()));
	}

	protected abstract String[] getControllerPackages();

	private HalFormsConfiguration createHalFormsConfiguration(
			Set<Class<ReadonlyResourceController>> resourceControllerClasses) {
		HalFormsConfiguration halFormsConfiguration = new HalFormsConfiguration();
		if (resourceControllerClasses != null) {
			for (Class<ReadonlyResourceController> rc: resourceControllerClasses) {
				Class<?> resourceClass = TypeUtil.getArgumentClassFromGenericSuperclass(
						rc,
						ReadonlyResourceController.class,
						0);
				halFormsConfiguration = withResourceClass(halFormsConfiguration, resourceClass, resourceControllerClasses);
			}
		}
		return halFormsConfiguration;
	}

	private HalFormsConfiguration withResourceClass(
			HalFormsConfiguration halFormsConfiguration,
			Class<?> resourceClass,
			Set<Class<ReadonlyResourceController>> resourceControllerClasses) {
		MutableHolder<HalFormsConfiguration> halFormsConfigurationHolder = new MutableHolder<>(halFormsConfiguration);
		ReflectionUtils.doWithFields(
				resourceClass,
				field -> configurationWithEnumOptions(
						halFormsConfigurationHolder,
						resourceClass,
						field),
				this::isEnumTypeMultipleAware);
		ReflectionUtils.doWithFields(
				resourceClass,
				field -> configurationWithResourceReferenceOptions(
						halFormsConfigurationHolder,
						resourceClass,
						null,
						field,
						resourceControllerClasses),
				this::isResourceReferenceTypeMultipleAware);
		ReflectionUtils.doWithFields(
				resourceClass,
				field -> configurationWithFieldEnumOptions(
						halFormsConfigurationHolder,
						resourceClass,
						null,
						field,
						resourceControllerClasses),
				this::isFieldEnumOptions);
		ResourceConfig resourceConfig = resourceClass.getAnnotation(ResourceConfig.class);
		if (resourceConfig != null) {
			for (ResourceArtifact artifact: resourceConfig.artifacts()) {
				if (!Serializable.class.equals(artifact.formClass())) {
					ReflectionUtils.doWithFields(
							artifact.formClass(),
							field -> configurationWithEnumOptions(
									halFormsConfigurationHolder,
									artifact.formClass(),
									field),
							this::isEnumTypeMultipleAware);
					ReflectionUtils.doWithFields(
							artifact.formClass(),
							field -> configurationWithResourceReferenceOptions(
									halFormsConfigurationHolder,
									resourceClass,
									artifact,
									field,
									resourceControllerClasses),
							this::isResourceReferenceTypeMultipleAware);
					ReflectionUtils.doWithFields(
							artifact.formClass(),
							field -> configurationWithFieldEnumOptions(
									halFormsConfigurationHolder,
									resourceClass,
									artifact,
									field,
									resourceControllerClasses),
							this::isFieldEnumOptions);
				}
			}
		}
		return halFormsConfigurationHolder.getValue();
	}

	private void configurationWithEnumOptions(
			MutableHolder<HalFormsConfiguration> halFormsConfigurationHolder,
			Class<?> resourceClass,
			Field resourceField) {
		log.debug("New HAL-FORMS enum options (class={}, field={})", resourceClass, resourceField.getName());
		halFormsConfigurationHolder.setValue(
				halFormsConfigurationHolder.getValue().withOptions(
						resourceClass,
						resourceField.getName(),
						metadata -> HalFormsOptions.
								inline(getInlineOptionsEnumConstants(resourceField)).
								withValueField("id").
								withPromptField("description").
								withMinItems(TypeUtil.isNotNullField(resourceField) ? 1L : 0L).
								withMaxItems(TypeUtil.isMultipleFieldType(resourceField) ? null : 1L)));
	}

	private void configurationWithResourceReferenceOptions(
			MutableHolder<HalFormsConfiguration> halFormsConfigurationHolder,
			Class<?> resourceClass,
			ResourceArtifact artifact,
			Field resourceField,
			Set<Class<ReadonlyResourceController>> resourceControllerClasses) {
		Link remoteOptionsLink = getRemoteOptionsLink(
				resourceClass,
				artifact,
				resourceField,
				resourceControllerClasses);
		if (remoteOptionsLink != null) {
			Class<?> optionsResourceClass = artifact != null ? artifact.formClass() : resourceClass;
			log.debug("New HAL-FORMS resource reference options (class={}, field={})", optionsResourceClass, resourceField.getName());
			halFormsConfigurationHolder.setValue(
					halFormsConfigurationHolder.getValue().withOptions(
							optionsResourceClass,
							resourceField.getName(),
							metadata -> {
								// Aquí hem de tornar a calcular el remoteOptionsLink perquè si no ho feim
								// l'enllaç no inclou el prefix 'http://localhost:8080/webcontext'
								Link repeatedRemoteOptionsLink = getRemoteOptionsLink(
										resourceClass,
										artifact,
										resourceField,
										resourceControllerClasses);
								return HalFormsOptions.
										remote(repeatedRemoteOptionsLink).
										withValueField("id").
										withPromptField(getRemoteOptionsPromptField(resourceField)).
										withMinItems(TypeUtil.isNotNullField(resourceField) ? 1L : 0L).
										withMaxItems(TypeUtil.isCollectionFieldType(resourceField) ? null : 1L);
							}));
		}
	}

	private void configurationWithFieldEnumOptions(
			MutableHolder<HalFormsConfiguration> halFormsConfigurationHolder,
			Class<?> resourceClass,
			ResourceArtifact artifact,
			Field resourceField,
			Set<Class<ReadonlyResourceController>> resourceControllerClasses) {
		log.debug("New HAL-FORMS field enum options (class={}, field={})", resourceClass, resourceField.getName());
		Link remoteOptionsLink = getRemoteFieldEnumOptionsLink(
				resourceClass,
				artifact,
				resourceField,
				resourceControllerClasses);
		if (remoteOptionsLink != null) {
			Class<?> optionsResourceClass = artifact != null ? artifact.formClass() : resourceClass;
			log.debug("New HAL-FORMS resource reference options (class={}, field={})", optionsResourceClass, resourceField.getName());
			halFormsConfigurationHolder.setValue(
					halFormsConfigurationHolder.getValue().withOptions(
							optionsResourceClass,
							resourceField.getName(),
							metadata -> {
								// Aquí hem de tornar a calcular el remoteOptionsLink perquè si no ho feim
								// l'enllaç no inclou el prefix 'http://localhost:8080/webcontext'
								Link repeatedRemoteOptionsLink = getRemoteFieldEnumOptionsLink(
										resourceClass,
										artifact,
										resourceField,
										resourceControllerClasses);
								return HalFormsOptions.
										remote(repeatedRemoteOptionsLink).
										withValueField("value").
										withPromptField("description").
										withMinItems(TypeUtil.isNotNullField(resourceField) ? 1L : 0L).
										withMaxItems(TypeUtil.isCollectionFieldType(resourceField) ? null : 1L);
							}));
		}
	}

	private boolean isEnumTypeMultipleAware(Field field) {
		Class<?> fieldType = TypeUtil.getFieldTypeMultipleAware(field);
		return fieldType != null && fieldType.isEnum();
	}

	private boolean isResourceReferenceTypeMultipleAware(Field field) {
		Class<?> fieldType = TypeUtil.getFieldTypeMultipleAware(field);
		return fieldType != null && ResourceReference.class.isAssignableFrom(fieldType);
	}

	private boolean isFieldEnumOptions(Field field) {
		ResourceField resourceField = field.getAnnotation(ResourceField.class);
		return resourceField != null && resourceField.enumType();
	}

	private FieldOption[] getInlineOptionsEnumConstants(Field field) {
		Object[] enumConstants;
		if (field.getType().isArray()) {
			enumConstants = field.getType().getComponentType().getEnumConstants();
		} else {
			enumConstants = field.getType().getEnumConstants();
		}

		// Comprova i obté les constants només si és de tipus Enum
		if (field.getType().isEnum()) {
			enumConstants = field.getType().getEnumConstants();
		} else if (field.getType().isArray() && field.getType().getComponentType().isEnum()) {
			enumConstants = field.getType().getComponentType().getEnumConstants();
		} else {
			// Llença una excepció o retorna un valor per defecte
			log.warn("El camp no és un tipus enum: {}", field.getName());
			return new FieldOption[0];
		}

		return Arrays.stream(enumConstants).
				map(e -> new FieldOption(
						e.toString(),
						I18nUtil.getInstance().getI18nEnumDescription(
								field,
								e.toString()))).
				toArray(FieldOption[]::new);
	}

	private Link getRemoteOptionsLink(
			Class<?> resourceClass,
			ResourceArtifact artifact,
			Field resourceField,
			Set<Class<ReadonlyResourceController>> resourceControllerClasses) {
		Optional<Class<ReadonlyResourceController>> resourceControllerClass = resourceControllerClasses.stream().
				filter(rc -> {
					Class<?> controllerResourceClass = TypeUtil.getArgumentClassFromGenericSuperclass(
							rc,
							ReadonlyResourceController.class,
							0);
					return controllerResourceClass.equals(resourceClass);
				}).findFirst();
		if (resourceControllerClass.isPresent()) {
			Link findLink = getFindLinkWithSelfRel(
					resourceControllerClass.get(),
					artifact,
					resourceField.getName());
			if (findLink != null) {
				// Al link generat li canviam les variables namedQuery i
				// perspective perquè no les posa com a múltiples.
				String findLinkHref = findLink.getHref().
						replace("namedQuery", "namedQuery*").
						replace("perspective", "perspective*");
				// I a més hi afegim les variables page, size i sort que no les
				// detecta a partir de la classe de tipus Pageable
				TemplateVariables findTemplateVariables = new TemplateVariables(
						new TemplateVariable("page", TemplateVariable.VariableType.REQUEST_PARAM),
						new TemplateVariable("size", TemplateVariable.VariableType.REQUEST_PARAM),
						new TemplateVariable("sort", TemplateVariable.VariableType.REQUEST_PARAM).composite());
				return Link.of(UriTemplate.of(findLinkHref).with(findTemplateVariables), findLink.getRel());
			} else {
				Class<?> referencedResourceClass = TypeUtil.getReferencedResourceClass(resourceField);
				log.error("Couldn't generate find link from field (" +
						"resourceClass=" + resourceClass + "," +
						"fieldName=" + resourceField.getName() + "," +
						"referencedResourceClass=" + referencedResourceClass + ")");
				return null;
			}
		} else {
			Class<?> referencedResourceClass = TypeUtil.getReferencedResourceClass(resourceField);
			log.error("Couldn't find resource controller class from field (" +
					"resourceClass=" + resourceClass + "," +
					"fieldName=" + resourceField.getName() + "," +
					"referencedResourceClass=" + referencedResourceClass + ")");
			return null;
		}
	}

	private Link getRemoteFieldEnumOptionsLink(
			Class<?> resourceClass,
			ResourceArtifact artifact,
			Field resourceField,
			Set<Class<ReadonlyResourceController>> resourceControllerClasses) {
		Optional<Class<ReadonlyResourceController>> resourceControllerClass = resourceControllerClasses.stream().
				filter(rc -> {
					Class<?> controllerResourceClass = TypeUtil.getArgumentClassFromGenericSuperclass(
							rc,
							MutableResourceController.class,
							0);
					return controllerResourceClass.equals(resourceClass);
				}).findFirst();
		if (resourceControllerClass.isPresent()) {
			if (artifact == null) {
				Class<MutableResourceController> mutableResourceControllerClass = (Class<MutableResourceController>)((Class<?>)resourceControllerClass.get());
				return linkTo(methodOn(mutableResourceControllerClass).fieldEnumOptionsFind(resourceField.getName())).
						withRel(IanaLinkRelations.SELF_VALUE);
			} else {
				return linkTo(methodOn(resourceControllerClass.get()).artifactFieldEnumOptionsFind(
						artifact.type(),
						artifact.code(),
						resourceField.getName())).
						withRel(IanaLinkRelations.SELF_VALUE);
			}
		} else {
			Class<?> referencedResourceClass = TypeUtil.getReferencedResourceClass(resourceField);
			log.error("Couldn't find resource controller class from field (" +
					"resourceClass=" + resourceClass + "," +
					"fieldName=" + resourceField.getName() + "," +
					"referencedResourceClass=" + referencedResourceClass + ")");
			return null;
		}
	}

	private Link getFindLinkWithSelfRel(
			Class<?> resourceControllerClass,
			ResourceArtifact artifact,
			String resourceFieldName) {
		Class<ReadonlyResourceController> readonlyResourceControllerClass = (Class<ReadonlyResourceController>)resourceControllerClass;
		boolean isMutableResourceController = MutableResourceController.class.isAssignableFrom(resourceControllerClass);
		if (artifact == null) {
			if (isMutableResourceController) {
				Class<MutableResourceController> mutableResourceControllerClass = (Class<MutableResourceController>)resourceControllerClass;
				return linkTo(methodOn(mutableResourceControllerClass).fieldOptionsFind(
						resourceFieldName,
						null,
						null,
						null,
						null,
						null)).withRel(IanaLinkRelations.SELF_VALUE);
			} else {
				return null;
			}
		} else if (artifact.type() == ResourceArtifactType.ACTION) {
			if (isMutableResourceController) {
				Class<MutableResourceController> mutableResourceControllerClass = (Class<MutableResourceController>)resourceControllerClass;
				return linkTo(methodOn(mutableResourceControllerClass).artifactActionFieldOptionsFind(
						artifact.code(),
						resourceFieldName,
						null,
						null,
						null,
						null,
						null)).withRel(IanaLinkRelations.SELF_VALUE);
			} else {
				return null;
			}
		} else if (artifact.type() == ResourceArtifactType.REPORT) {
			return linkTo(methodOn(readonlyResourceControllerClass).artifactReportFieldOptionsFind(
					artifact.code(),
					resourceFieldName,
					null,
					null,
					null,
					null,
					null)).withRel(IanaLinkRelations.SELF_VALUE);
		} else if (artifact.type() == ResourceArtifactType.FILTER) {
			return linkTo(methodOn(readonlyResourceControllerClass).artifactFilterFieldOptionsFind(
					artifact.code(),
					resourceFieldName,
					null,
					null,
					null,
					null,
					null)).withRel(IanaLinkRelations.SELF_VALUE);
		} else {
			return null;
		}
	}

	private String getRemoteOptionsPromptField(Field field) {
		String descriptionField = null;
		ResourceField fieldAnnotation = field.getAnnotation(ResourceField.class);
		if (fieldAnnotation != null && !fieldAnnotation.descriptionField().isEmpty()) {
			descriptionField = fieldAnnotation.descriptionField();
		} else {
			Class<? extends Resource<?>> referencedResourceClass = TypeUtil.getReferencedResourceClass(field);
			ResourceConfig configAnnotation = referencedResourceClass.getAnnotation(ResourceConfig.class);
			if (configAnnotation != null && !configAnnotation.descriptionField().isEmpty()) {
				descriptionField = configAnnotation.descriptionField();
			} else {
				descriptionField = "id";
			}
		}
		return descriptionField;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class MutableHolder<T> {
		private T value;
	}

	@Getter
	@AllArgsConstructor
	public static class FieldOption {
		private String id;
		private String description;
	}

}
