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
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EstadistiquesInfo {
    @NotNull @Size(min = 1)
    private String codi;
    private String versio;
    private Date data;
    @NotNull @Valid
    private List<DimensioDesc> dimensions;
    @NotNull @Valid
    private List<IndicadorDesc> indicadors;

    // Custom builder to validate bean constraints on build()
    public static class EstadistiquesInfoBuilder {
        public EstadistiquesInfo build() {
            EstadistiquesInfo instance = new EstadistiquesInfo(codi, versio, data, dimensions, indicadors);
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<EstadistiquesInfo>> violations = validator.validate(instance);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            return instance;
        }
    }
}
