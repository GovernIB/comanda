package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SalutInfo {
    @NotNull @Size(min = 1, max = 16)
    private String codi;
    @NotNull
    private Date data;
    @NotNull @Valid
    private EstatSalut estat;
    @NotNull @Valid
    private EstatSalut bd;
    @Valid
    private List<IntegracioSalut> integracions;
    @Valid
    private List<DetallSalut> altres;
    @Valid
    private List<MissatgeSalut> missatges;
    @Size(max = 10)
    private String versio;
    @Valid
    private List<SubsistemaSalut> subsistemes;

    // Custom builder to validate bean constraints on build()
    public static class SalutInfoBuilder {
        public SalutInfo build() {
            SalutInfo instance = new SalutInfo(codi, data, estat, bd, integracions, altres, missatges, versio, subsistemes);
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<SalutInfo>> violations = validator.validate(instance);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            return instance;
        }
    }
}
