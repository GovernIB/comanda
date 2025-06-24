package es.caib.comanda.estadistica.back.validation;

import es.caib.comanda.estadistica.back.intf.validation.ValidSimpleWidget;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
@RequiredArgsConstructor
public class ValidSimpleWidgetValidator extends ValidWidgetValidator implements ConstraintValidator<ValidSimpleWidget, EstadisticaSimpleWidget> {

    private final MessageSource messageSource;

    @Override
    public void initialize(final ValidSimpleWidget constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        super.initializeLocale(messageSource);
    }

    @Override
    public boolean isValid(EstadisticaSimpleWidget widget, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        isValid = validatePeriode(widget, context) && isValid;
        isValid = validateField(!TableColumnsEnum.AVERAGE.equals(widget.getTipusIndicador()) || widget.getPeriodeIndicador() != null, context, "periodeIndicador", "És obligatori emplenar aquest camp") && isValid;

        return isValid;
    }

}
