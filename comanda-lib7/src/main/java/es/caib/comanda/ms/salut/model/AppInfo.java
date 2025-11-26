package es.caib.comanda.ms.salut.model;

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
public class AppInfo {
    @NotNull @Size(min = 1)
    private String codi;
    @NotNull @Size(min = 1)
    private String nom;
    @NotNull @Size(min = 1)
    private String versio;
    @NotNull
    private Date data;
    private String revisio;
    private String jdkVersion;
    @Valid
    private List<IntegracioInfo> integracions;
    @Valid
    private List<SubsistemaInfo> subsistemes;
    @Valid
    private List<ContextInfo> contexts;

    // Custom builder to validate bean constraints on build()
    public static class AppInfoBuilder {
        public AppInfo build() {
            AppInfo instance = new AppInfo(codi, nom, versio, data, revisio, jdkVersion, integracions, subsistemes, contexts);
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<AppInfo>> violations = validator.validate(instance);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            return instance;
        }
    }
}
