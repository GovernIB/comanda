package es.caib.comanda.ms.estadistica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "RegistreEstadistic", description = "Registre d'estadística amb dimensions i fets associats")
public class RegistreEstadistic {
    @Schema(description = "Dimensions que qualifiquen el registre (p. ex. àrea, tipus, canal)")
    @Valid
    private List<Dimensio> dimensions;
    @Schema(description = "Fets o mesures quantitatives associades al registre")
    @Valid
    private List<Fet> fets;
}
