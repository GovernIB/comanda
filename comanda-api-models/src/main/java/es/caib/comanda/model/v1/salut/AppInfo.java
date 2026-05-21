package es.caib.comanda.model.v1.salut;

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
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AppInfo", description = "Informació bàsica de l'aplicació consultada per COMANDA")
public class AppInfo {
    @Schema(description = "Codi identificador de l'aplicació", example = "APP")
    @NotNull @Size(min = 1, max = 16)
    private String codi;

    @Schema(description = "Nom complet de l'aplicació", example = "APLICACIO")
    @NotNull @Size(min = 1, max = 100)
    private String nom;

    @Schema(description = "Versió desplegada de l'aplicació", example = "2.1.0")
    @NotNull @Size(min = 1, max = 10)
    private String versio;

    @Schema(description = "Data de compilació o de la informació reportada", type = "string", format = "date-time")
    @NotNull
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime data;

    @Schema(description = "Revisió o identificador de commit de la build", example = "a1b2c3d")
    @Size(max = 64)
    private String revisio;

    @Schema(description = "Versió de JDK amb la qual s'executa l'aplicació", example = "Temurin-17.0.9")
    @Size(max = 32)
    private String jdkVersion;

    @Schema(description = "Versió de JBoss/WildFly amb la qual s'executa l'aplicació", example = "JBoss EAP 7.2")
    private String versioJboss;

    @Schema(description = "Llista d'integracions exposades per l'aplicació")
    @Valid
    private List<IntegracioInfo> integracions;

    @Schema(description = "Llista de subsistemes interns amb el seu estat")
    @Valid
    private List<SubsistemaInfo> subsistemes;

    @Schema(description = "Contextos o endpoints base exposats per l'aplicació")
    @Valid
    private List<ContextInfo> contexts;

    @AssertTrue(message = "No poden existir integracions amb codis duplicats")
    private boolean isIntegracionsCodiUnique() {
        if (integracions == null) {
            return true;
        }
        Set<String> uniqueValues = new HashSet<String>();
        for (IntegracioInfo integracio : integracions) {
            if (integracio.getCodi() != null && !uniqueValues.add(integracio.getCodi())) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "No poden existir subsistemes amb codis duplicats")
    private boolean isSubsistemesCodiUnique() {
        if (subsistemes == null) {
            return true;
        }
        Set<String> uniqueValues = new HashSet<String>();
        for (SubsistemaInfo subsistema : subsistemes) {
            if (subsistema.getCodi() != null && !uniqueValues.add(subsistema.getCodi())) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "No poden existir contextos amb codis duplicats")
    private boolean isContextsCodiUnique() {
        if (contexts == null) {
            return true;
        }
        Set<String> uniqueValues = new HashSet<String>();
        for (ContextInfo context : contexts) {
            if (context.getCodi() != null && !uniqueValues.add(context.getCodi())) {
                return false;
            }
        }
        return true;
    }

    // Custom builder to validate bean constraints on build()
    public static class AppInfoBuilder {
        public AppInfo build() {
            AppInfo instance = new AppInfo(codi, nom, versio, data, revisio, jdkVersion, versioJboss, integracions, subsistemes, contexts);
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<AppInfo>> violations = validator.validate(instance);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            return instance;
        }
    }
}
