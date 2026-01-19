package es.caib.comanda.model.v1.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FitxerInfo {

    @Schema(description = "Nom del fitxer", example = "document.pdf")
    private String nom;
    @Schema(description = "Mida del fitxer en bytes", example = "1024")
    private long mida;
    @Schema(description = "Tipus MIME del fitxer", example = "application/pdf")
    private String mimeType;
    @Schema(description = "Data de creació del fitxer en format dd/MM/yyyy HH:mm:ss", example = "15/01/2024 00:00:00")
    private String dataCreacio;
    @Schema(description = "Data de modificació del fitxer en format dd/MM/yyyy HH:mm:ss", example = "15/01/2024 23:59:59")
    private String dataModificacio;
}
