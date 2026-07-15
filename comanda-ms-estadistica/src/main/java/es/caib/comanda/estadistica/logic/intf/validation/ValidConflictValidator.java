package es.caib.comanda.estadistica.logic.intf.validation;

import es.caib.comanda.estadistica.logic.helper.DashboardImportHelper;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OverwriteEnum;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport;
import es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.Conflict;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validador per a comprovar que no existeix un camp boolear {@link Boolean}
 * no pot ser verdader sí l'usuari no és administrador.</br>
 * 
 * @author Limit Tecnologies <limit@limit.es>
 */
public class ValidConflictValidator implements ConstraintValidator<ValidConflict, Conflict> {

    @Autowired
    private DashboardImportHelper dashboardImportHelper;

	@Override
	public void initialize(final ValidConflict constraintAnnotation) {
	}

	@Override
	public boolean isValid(final Conflict conflict, final ConstraintValidatorContext context) {
        boolean valid = true;

        if (DashboardExport.class.getSimpleName().equals(conflict.getTipo())){
            if (OverwriteEnum.EMPRAR_EXISTENT.equals(conflict.getOverwrite())) {
                valid = false;
                context.buildConstraintViolationWithTemplate(
                                "Aquest no es un valor valid per a aquest element")
                        .addNode(Conflict.Fields.overwrite)
                        .addConstraintViolation();
            }
        }

        if (OverwriteEnum.CREAR_AMB_ALTRE_NOM.equals(conflict.getOverwrite()) && conflict.getNouNom() != null) {
            if (dashboardImportHelper.existsElementByNom(conflict.getNouNom(), conflict.getAppId(), conflict.getTipo())) {
                valid = false;
                context.buildConstraintViolationWithTemplate(
                                "Ja existeix un element amb aquest nom")
                        .addNode(Conflict.Fields.nouNom)
                        .addConstraintViolation();
            }
        }

        return valid;
    }
}