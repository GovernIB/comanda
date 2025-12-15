package es.caib.comanda.model.v1.estadistica;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DimensioDesc", description = "Descripció d'una dimensió disponible (les dimenstions són els camps pels quals es pot filtrar la informació estadística")
public class DimensioDesc {
    @Schema(description = "Codi de la dimensió", example = "ENT")
    @NotNull @Size(min = 1, max = 32)
    private String codi;
    @Schema(description = "Nom de la dimensió", example = "Entitat")
    @NotNull @Size(min = 1, max = 64)
    private String nom;
    @Schema(description = "Descripció funcional de la dimensió", example = "Entitat de la que s'ha generat la informació estadística")
    private String descripcio;
    @Schema(description = "Llista dels possibles valors que pot tenir assignada la dimensió", example = "[CAIB, TEST]")
    private List<String> valors;
}
