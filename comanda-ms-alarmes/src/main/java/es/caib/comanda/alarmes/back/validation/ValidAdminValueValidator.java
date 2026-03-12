package es.caib.comanda.alarmes.back.validation;

import es.caib.comanda.alarmes.back.intf.validation.ValidAdminValue;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validador per a comprovar que no existeix un camp boolear {@link Boolean}
 * no pot ser verdader sí l'usuari no és administrador.</br>
 * 
 * @author Limit Tecnologies <limit@limit.es>
 */
public class ValidAdminValueValidator implements ConstraintValidator<ValidAdminValue, Boolean> {

	@Autowired
	private AuthenticationHelper authenticationHelper;

	@Override
	public void initialize(final ValidAdminValue constraintAnnotation) {
	}

	@Override
	public boolean isValid(final Boolean value, final ConstraintValidatorContext context) {
        return !value || authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN);
    }
}