package es.caib.comanda.permisos.logic.intf.model;

import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;

class PermisValidationTest {

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Test
    void permis_detectaQueElsPermisosSonObligatoris() {
        // Verifica que el model Permis obliga a informar la col·lecció de permisos.
        Permis permis = new Permis();
        permis.setEntornAppId(10L);

        var violations = validator.validateProperty(permis, "permisos");

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("permisos");
    }

    @Test
    void permis_detectaQueLObjecteEsObligatori() {
        // Comprova que el model Permis obliga a informar la referència a objecte.
        Permis permis = new Permis();
        permis.setEntornAppId(10L);

        var violations = validator.validateProperty(permis, "objecte");

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("objecte");
    }
}
