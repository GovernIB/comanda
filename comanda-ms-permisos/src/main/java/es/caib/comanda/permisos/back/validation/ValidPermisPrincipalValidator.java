package es.caib.comanda.permisos.back.validation;

import es.caib.comanda.permisos.back.intf.validation.ValidPermisPrincipal;
import es.caib.comanda.permisos.logic.intf.model.Permis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
public class ValidPermisPrincipalValidator implements ConstraintValidator<ValidPermisPrincipal, Permis> {

    private final MessageSource messageSource;
    private Locale locale;

    @Override
    public void initialize(final ValidPermisPrincipal constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        this.locale = request.getLocale();
    }

    @Override
    public boolean isValid(Permis permis, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();
        String message = "És obligatori emplenar el camp usuari o el camp grup";

        if (permis.getUsuari() == null && permis.getGrup() == null) {
            addConstraintViolation(context, message, "usuari");
            addConstraintViolation(context, message, "grup");
            return false;
        }

        return isValid;
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
