package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(name = "SalutInfo", description = "Estat de salut funcional de l'aplicació i metadades associades")
public class SalutInfo {
    @Schema(description = "Codi identificador de l'aplicació", example = "NOTIB")
    @NotNull @Size(min = 1)
    private String codi;
    @Schema(description = "Instant de generació de l'estat de salut", type = "string", format = "date-time")
    @NotNull
    private Date data;
    @Schema(description = "Estat global de l'aplicació")
    @NotNull @Valid
    private EstatSalut estat;
    @Schema(description = "Estat de la base de dades")
    @NotNull @Valid
    private EstatSalut bd;
    @Schema(description = "Integracions amb el seu estat")
    @Valid
    private List<IntegracioSalut> integracions;
    @Schema(description = "Altres detalls rellevants de salut")
    @Valid
    private List<DetallSalut> altres;
    @Schema(description = "Missatges informatius o d'alerta")
    @Valid
    private List<MissatgeSalut> missatges;
    @Schema(description = "Versió de l'aplicació", example = "1.4.3")
    private String versio;
    @Schema(description = "Subsistemes interns amb el seu estat")
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
