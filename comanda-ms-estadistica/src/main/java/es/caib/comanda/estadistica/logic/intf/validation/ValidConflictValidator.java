package es.caib.comanda.estadistica.logic.intf.validation;

import es.caib.comanda.estadistica.logic.helper.DashboardImportHelper;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OverwriteEnum;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport;
import es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.Conflict;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validador per a comprovar la validesa de la resolució de conflictes en la importació de dashboards.
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
                                I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.intf.validation.ValidConflict.overwrite.invalid"))
                        .addNode(Conflict.Fields.overwrite)
                        .addConstraintViolation();
            }
        }

        if (OverwriteEnum.CREAR_AMB_ALTRE_NOM.equals(conflict.getOverwrite()) && conflict.getNouNom() != null) {
            if (dashboardImportHelper.existsElementByNom(conflict.getNouNom(), conflict.getAppId(), conflict.getTipo())) {
                valid = false;
                context.buildConstraintViolationWithTemplate(
                                I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.intf.validation.ValidConflict.nouNom.exists"))
                        .addNode(Conflict.Fields.nouNom)
                        .addConstraintViolation();
            }
        }

        return valid;
    }
}
