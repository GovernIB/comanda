package es.caib.comanda.alarmes.back.intf.validation;

import es.caib.comanda.alarmes.back.validation.ValidAdminValueValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy= ValidAdminValueValidator.class)
public @interface ValidAdminValue {

    String message() default "{es.caib.comanda.alarmes.back.intf.validation.ValidAdminValue}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
