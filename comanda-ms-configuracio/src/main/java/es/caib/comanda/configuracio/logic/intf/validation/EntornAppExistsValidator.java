package es.caib.comanda.configuracio.logic.intf.validation;

import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * Validador per a comprovar que no existeix una entitat {@link EntornApp}
 * amb la mateixa combinació d'entorn i aplicació.</br>
 *
 * Evita errors de constraint única a nivell de base de dades.
 * 
 * @author Limit Tecnologies <limit@limit.es>
 */
public class EntornAppExistsValidator implements ConstraintValidator<EntornAppExists, EntornApp> {

	@Autowired
	private EntornAppRepository entornAppRepository;

	@Override
	public void initialize(final EntornAppExists constraintAnnotation) {
	}

	@Override
	public boolean isValid(final EntornApp entornApp, final ConstraintValidatorContext context) {
		boolean valid = Objects.isNull(entornApp) || Objects.isNull(entornApp.getEntorn()) || Objects.isNull(entornApp.getApp());
		if (!valid) { // Si l'objecte i els camps entorn i app no són nuls, es verifica si la combinació és única
			Long idEntornApp = entornAppRepository.getIdByEntornIdAndAppId(entornApp.getEntorn().getId(), entornApp.getApp().getId());
			valid = Objects.isNull(idEntornApp); // És vàlid si no existeix cap altra entitat amb aquesta combinació
			if (!valid && Objects.nonNull(entornApp.getId())) { // O si existeix, però és el mateix registre (edició)
				valid = Objects.equals(idEntornApp, entornApp.getId());
			}
		}
		if (!valid) {// Si no és vàlid, s'afegeixen errors als camps implicats
			context.
				buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).
				addPropertyNode(EntornApp.Fields.app).
				addConstraintViolation().
				disableDefaultConstraintViolation();
			context.
				buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).
				addPropertyNode(EntornApp.Fields.entorn).
				addConstraintViolation().
				disableDefaultConstraintViolation();
		}
		return valid;
	}
}