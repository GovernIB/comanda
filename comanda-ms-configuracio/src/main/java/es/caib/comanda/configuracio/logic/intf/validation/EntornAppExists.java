package es.caib.comanda.configuracio.logic.intf.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Anotació pel validador {@link EntornAppExistsValidator}
 * 
 * @author Limit Tecnologies <limit@limit.es>
 */
@Documented
@Constraint(validatedBy = EntornAppExistsValidator.class)
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntornAppExists {

	String message() default "{es.caib.comanda.configuracio.logic.intf.validation.EntornAppExists}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
