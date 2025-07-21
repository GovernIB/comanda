package es.caib.comanda.permisos.back.intf.validation;

import es.caib.comanda.permisos.back.validation.ValidPermisPrincipalValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy= ValidPermisPrincipalValidator.class)
public @interface ValidPermisPrincipal {

    String message() default "Error en la validació del permís.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
