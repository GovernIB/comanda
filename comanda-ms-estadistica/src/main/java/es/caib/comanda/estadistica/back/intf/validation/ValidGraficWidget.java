package es.caib.comanda.estadistica.back.intf.validation;

import es.caib.comanda.estadistica.back.validation.ValidGraficWidgetValidator;

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
@Constraint(validatedBy= ValidGraficWidgetValidator.class)
public @interface ValidGraficWidget {

    String message() default "Error en la validació del widget gràfic.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
