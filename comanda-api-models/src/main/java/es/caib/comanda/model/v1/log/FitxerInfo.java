package es.caib.comanda.model.v1.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FitxerInfo implements Serializable {

    @Schema(description = "Nom del fitxer", example = "document.pdf")
    private String nom;
    @Schema(description = "Mida del fitxer en bytes", example = "1024")
    private long mida;
    @Schema(description = "Tipus MIME del fitxer", example = "application/pdf")
    private String mimeType;
    @Schema(description = "Data de creació del fitxer", example = "2024-01-15T10:30:00")
    private String dataCreacio;
    @Schema(description = "Data de modificació del fitxer", example = "2024-01-20T14:45:00")
    private String dataModificacio;
}
