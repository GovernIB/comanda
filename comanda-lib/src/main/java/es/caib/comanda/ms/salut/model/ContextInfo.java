package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ContextInfo", description = "Context o 'namespace' funcional exposat per l'aplicació")
public class ContextInfo {
    @Schema(description = "Codi del context", example = "public")
    @NotNull @Size(min = 1)
    private String codi;
    @Schema(description = "Nom descriptiu del context", example = "Context públic")
    @NotNull @Size(min = 1)
    private String nom;
    @Schema(description = "Path base del context", example = "/app/public")
    @NotNull @Size(min = 1)
    private String path;
    @Schema(description = "Llista de manuals associats al context")
    @Valid
    private List<Manual> manuals;
    @Schema(description = "URL o especificació OpenAPI del context, si està disponible", example = "https://dev.caib.es/app/public/openapi.json")
    private String api;
}
