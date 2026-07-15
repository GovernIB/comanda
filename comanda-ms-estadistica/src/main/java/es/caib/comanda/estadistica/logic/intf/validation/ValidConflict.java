package es.caib.comanda.estadistica.logic.intf.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy= ValidConflictValidator.class)
public @interface ValidConflict {

    String message() default "{es.caib.comanda.estadistica.logic.intf.validation.ValidConflict}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
