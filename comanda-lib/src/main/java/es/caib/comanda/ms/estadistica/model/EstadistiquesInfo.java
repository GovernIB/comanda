package es.caib.comanda.ms.estadistica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(name = "EstadistiquesInfo", description = "Catàleg de dimensions i indicadors d'estadística disponibles per a una APP")
public class EstadistiquesInfo {
    @Schema(description = "Codi identificador de l'aplicació", example = "NOTIB")
    @NotNull @Size(min = 1)
    private String codi;

    @Schema(description = "Versió de l'aplicació", example = "1.4.3")
    private String versio;

    @Schema(description = "Data de generació de la informació", type = "string", format = "date-time")
    private Date data;

    @Schema(description = "Dimensions estadístiques disponibles")
    @NotNull @Valid
    private List<DimensioDesc> dimensions;

    @Schema(description = "Indicadors estadístics disponibles")
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
