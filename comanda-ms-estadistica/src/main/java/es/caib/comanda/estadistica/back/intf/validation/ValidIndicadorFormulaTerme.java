package es.caib.comanda.estadistica.back.intf.validation;

import es.caib.comanda.estadistica.back.validation.ValidIndicadorFormulaTermeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidIndicadorFormulaTermeValidator.class)
public @interface ValidIndicadorFormulaTerme {

    String message() default "Error en la validació del terme de fórmula d'indicador.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
