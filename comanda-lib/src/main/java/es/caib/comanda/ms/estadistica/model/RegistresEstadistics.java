package es.caib.comanda.ms.estadistica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistresEstadistics {
    @NotNull @Valid
    private Temps temps;
    @Valid
    private List<RegistreEstadistic> fets;

    // Custom builder to validate bean constraints on build()
    public static class RegistresEstadisticsBuilder {
        public RegistresEstadistics build() {
            RegistresEstadistics instance = new RegistresEstadistics(temps, fets);
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<RegistresEstadistics>> violations = validator.validate(instance);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            return instance;
        }
    }
}
