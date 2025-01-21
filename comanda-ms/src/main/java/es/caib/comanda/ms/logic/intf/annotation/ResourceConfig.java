package es.caib.comanda.ms.logic.intf.annotation;

import es.caib.comanda.ms.logic.intf.model.Resource;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotació per a configurar un recurs de l'API REST.
 * 
 * @author Limit Tecnologies
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ResourceConfig {

	@AliasFor("name")
	String value() default "";
	@AliasFor("value")
	String name() default "";
	public String resourceDescription() default "";
	public String resourceDescriptionI18n() default "";
	public String descriptionField() default "";
	public ResourceSort[] defaultSortFields() default {};
	public String[] quickFilterFields() default {};
	public Class<? extends Resource> parentEntity() default Resource.class;

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface ResourceSort {
		public String field() default "";
		public org.springframework.data.domain.Sort.Direction direction() default org.springframework.data.domain.Sort.Direction.ASC;
	}

}
