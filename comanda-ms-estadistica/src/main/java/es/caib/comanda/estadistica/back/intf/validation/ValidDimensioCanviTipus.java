package es.caib.comanda.estadistica.back.intf.validation;

import es.caib.comanda.estadistica.back.validation.ValidDimensioCanviTipusValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy= ValidDimensioCanviTipusValidator.class)
public @interface ValidDimensioCanviTipus {

    String message() default "Error en la validació del widget taula.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
