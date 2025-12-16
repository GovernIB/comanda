package es.caib.comanda.model.v1.estadistica;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "RegistreEstadistic", description = "Registre d'estadística amb dimensions i fets associats")
public class RegistreEstadistic {

    @Schema(description = "Dimensions que qualifiquen el registre (p. ex. àrea, tipus, canal)",
            example = "[{\"codi\":\"ENTITAT\",\"valor\":\"CAIB\"},{\"codi\":\"ORGAN\",\"valor\":\"A03002345\"},{\"codi\":\"USUARI\",\"valor\":\"u012345\"}]")
    @Valid
    @JsonDeserialize(contentAs = GenericDimensio.class)
    private List<Dimensio> dimensions;
    @Schema(description = "Fets o mesures quantitatives associades al registre",
            example = "[{\"codi\":\"EXP_CREATS\",\"valor\":10},{\"codi\":\"EXP_TANCATS\",\"valor\":2}]")
    @Valid
    @JsonDeserialize(contentAs = GenericFet.class)
    private List<Fet> fets;
}
