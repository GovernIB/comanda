package es.caib.comanda.model.v1.salut;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import es.caib.comanda.model.v1.deserializer.InformacioSistemaDeserializer;
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
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SalutInfo", description = "Estat de salut funcional de l'aplicació i metadades associades")
public class SalutInfo {
    @Schema(description = "Codi identificador de l'aplicació", example = "APP")
    @NotNull @Size(min = 1, max = 16)
    private String codi;
    @Schema(description = "Instant de generació de l'estat de salut", type = "string", format = "date-time")
    @NotNull
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime data;
    @Schema(description = "Estat global de l'aplicació")
    @NotNull @Valid
    @JsonAlias("estat")
    private EstatSalut estatGlobal;
    @Schema(description = "Estat de la base de dades")
    @NotNull @Valid
    @JsonAlias("bd")
    private EstatSalut estatBaseDeDades;
    @Schema(description = "Integracions amb el seu estat")
    @Valid
    private List<IntegracioSalut> integracions;
    @Schema(description = "Informació resum de l'estat del sistema")
    @Valid
    @JsonDeserialize(using = InformacioSistemaDeserializer.class)
    @JsonAlias({"altres", "informacio", "infoSistema", "informacio_sistema"})
    private InformacioSistema informacioSistema;
    @Schema(description = "Missatges informatius o d'alerta")
    @Valid
    private List<MissatgeSalut> missatges;
    @Schema(description = "Versió de l'aplicació", example = "1.4.3")
    @Size(max = 10)
    private String versio;
    @Schema(description = "Subsistemes interns amb el seu estat")
    @Valid
    private List<SubsistemaSalut> subsistemes;

    // Custom builder to validate bean constraints on build()
    public static class SalutInfoBuilder {
        public SalutInfo build() {
            SalutInfo instance = new SalutInfo(codi, data, estatGlobal, estatBaseDeDades, integracions, informacioSistema, missatges, versio, subsistemes);
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<SalutInfo>> violations = validator.validate(instance);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            return instance;
        }
    }

}
