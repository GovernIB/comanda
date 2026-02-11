package es.caib.comanda.model.v1.estadistica;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import es.caib.comanda.model.v1.deserializer.OffsetDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "RegistresEstadistics", description = "Registres estadístics d'un instant de temps amb les seves mesures")
public class RegistresEstadistics {
    @Schema(description = "Dia al que pertanyen els registres", type = "string", format = "date-time", example = "2025-11-25T00:00:00.000Z")
    @NotNull
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime temps;
    @Schema(description = "Llista de registres o fets estadístics recollits en l'instant indicat")
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
