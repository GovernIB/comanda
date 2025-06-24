package es.caib.comanda.estadistica.back.validation;

import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAbsolutTipus;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeEspecificAny;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeMode;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaWidget;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;

@Slf4j
@NoArgsConstructor
public abstract class ValidWidgetValidator {

    private MessageSource messageSource;
    private Locale locale;

    protected void initializeLocale(MessageSource messageSource) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        this.locale = request.getLocale();
        this.messageSource = messageSource;
    }

    protected boolean validatePeriode(EstadisticaWidget widget, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (PeriodeMode.PRESET.equals(widget.getPeriodeMode())) {
            isValid = validateField(widget.getPresetPeriode() != null, context, "presetPeriode", "És obligatori emplenar aquest camp");
        } else if (PeriodeMode.RELATIU.equals(widget.getPeriodeMode())) {
            isValid = validateField(widget.getRelatiuPuntReferencia() != null, context, "relatiuPuntReferencia", "És obligatori emplenar aquest camp");
            isValid = validateField(widget.getRelatiuCount() != null, context, "relatiuCount", "És obligatori emplenar aquest camp") && isValid;
            isValid = validateField(widget.getRelatiueUnitat() != null, context, "relatiueUnitat", "És obligatori emplenar aquest camp") && isValid;
            isValid = validateField(widget.getRelatiuAlineacio() != null, context, "relatiuAlineacio", "És obligatori emplenar aquest camp") && isValid;
        } else if (PeriodeMode.ABSOLUT.equals(widget.getPeriodeMode())) {
            isValid = validateField(widget.getAbsolutTipus() != null, context, "absolutTipus", "És obligatori emplenar aquest camp");
            if (widget.getAbsolutTipus() != null) {
                if (PeriodeAbsolutTipus.DATE_RANGE.equals(widget.getAbsolutTipus())) {
                    isValid = validateField(widget.getAbsolutDataInici() != null, context, "absolutDataInici", "És obligatori emplenar aquest camp") && isValid;
                    isValid = validateField(widget.getAbsolutDataFi() != null, context, "absolutDataFi", "És obligatori emplenar aquest camp") && isValid;
                } else if (PeriodeAbsolutTipus.SPECIFIC_PERIOD_OF_YEAR.equals(widget.getAbsolutTipus())) {
                    isValid = validateField(widget.getAbsolutAnyReferencia() != null, context, "absolutAnyReferencia", "És obligatori emplenar aquest camp") && isValid;
                    if (widget.getAbsolutAnyReferencia() != null && PeriodeEspecificAny.SPECIFIC_YEAR.equals(widget.getAbsolutAnyReferencia())) {
                        isValid = validateField(widget.getAbsolutAnyValor() != null, context, "absolutAnyValor", "És obligatori emplenar aquest camp") && isValid;
                    }
                    isValid = validateField(widget.getAbsolutPeriodeUnitat() != null, context, "absolutPeriodeUnitat", "És obligatori emplenar aquest camp") && isValid;
                    isValid = validateField(widget.getAbsolutPeriodeInici() != null, context, "absolutPeriodeInici", "És obligatori emplenar aquest camp") && isValid;
                    isValid = validateField(widget.getAbsolutPeriodeFi() != null, context, "absolutPeriodeFi", "És obligatori emplenar aquest camp") && isValid;
                }
            }
        }

        return isValid;
    }

    protected boolean validateField(boolean condition, ConstraintValidatorContext context, String fieldName, String message) {
        if (!condition) {
            addConstraintViolation(context, message, fieldName);
            return false;
        }
        return true;
    }

    protected void addConstraintViolation(ConstraintValidatorContext context, String msgKey, String fieldName) {
        String message;
        try {
            message = messageSource.getMessage(
                    msgKey,
                    null,
                    locale);
        } catch (NoSuchMessageException e) {
            message = msgKey;
        }
        context.buildConstraintViolationWithTemplate(message)
               .addNode(fieldName)
               .addConstraintViolation();
    }
}