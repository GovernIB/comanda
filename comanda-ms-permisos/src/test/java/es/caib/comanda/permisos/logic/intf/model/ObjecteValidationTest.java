package es.caib.comanda.permisos.logic.intf.model;

import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ObjecteValidationTest {

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Test
    void objecte_detectaElsCampsObligatorisDelModel() {
        // Comprova que el model Objecte marca com a obligatoris els camps bàsics de negoci.
        Objecte objecte = new Objecte();

        var violations = validator.validate(objecte);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("tipus", "nom", "identificador");
    }
}
