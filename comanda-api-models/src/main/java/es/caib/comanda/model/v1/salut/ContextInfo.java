package es.caib.comanda.model.v1.salut;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ContextInfo", description = "Context o 'namespace' funcional exposat per l'aplicació")
public class ContextInfo {
    @Schema(description = "Codi del context", example = "API_INT")
    @NotNull @Size(min = 1)
    private String codi;
    @Schema(description = "Nom descriptiu del context", example = "Api interna")
    @NotNull @Size(min = 1, max = 255)
    private String nom;
    @Schema(description = "Path base del context", example = "/appapi/interna")
    @NotNull @Size(min = 1)
    private String path;
    @Schema(description = "Llista de manuals associats al context")
    @Valid
    private List<Manual> manuals;
    @Schema(description = "URL o especificació OpenAPI del context, si està disponible", example = "https://dev.caib.es/app/internaapi/swagger/index.html")
    private String api;
}
