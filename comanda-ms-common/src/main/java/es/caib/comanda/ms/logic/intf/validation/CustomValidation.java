package es.caib.comanda.ms.logic.intf.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Validació personalitzada.
 *
 * @author Límit Tecnologies
 * @see CustomValidator
 */
@Documented
@Constraint(validatedBy = CustomValidationValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CustomValidation.List.class)
public @interface CustomValidation {

	Class<? extends CustomValidator<?>> customValidatorType();
	boolean springBean() default false;
	String[] targetFields() default {};

	String message() default "{es.limit.base.boot.validation.constraints.CustomValidation}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	/**
	 * Defines several CustomValidation annotations on the same element.
	 *
	 * @see CustomValidation
	 */
	@Documented
	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@interface List {
		CustomValidation[] value();
	}

}
