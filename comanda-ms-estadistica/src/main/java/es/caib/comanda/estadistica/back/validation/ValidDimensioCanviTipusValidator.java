package es.caib.comanda.estadistica.back.validation;

import es.caib.comanda.estadistica.back.intf.validation.ValidDimensioCanviTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio.ChangeTipusActionForm;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

import static com.hazelcast.org.json.XMLTokener.entity;

@Slf4j
@RequiredArgsConstructor
public class ValidDimensioCanviTipusValidator implements ConstraintValidator<ValidDimensioCanviTipus, ChangeTipusActionForm> {

    private final DimensioRepository dimensioRepository;

    @Override
    public boolean isValid(ChangeTipusActionForm resource, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        if (TipusDimensioEnum.CONSELLERIA.equals(resource.getTipus())) {
            context.buildConstraintViolationWithTemplate("Tipus no valido")
                .addPropertyNode(ChangeTipusActionForm.Fields.tipus)
                .addConstraintViolation();
            isValid = false;
        }

        if (resource.getTipus() != null) {
            List<DimensioEntity> dimensioEntityList = dimensioRepository.findByEntornAppId(resource.getEntornAppId());
            if (dimensioEntityList.stream().anyMatch(c -> c.getTipus() == resource.getTipus())) {
                context.buildConstraintViolationWithTemplate("Tipus ja assignat")
                    .addPropertyNode(ChangeTipusActionForm.Fields.tipus)
                    .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
