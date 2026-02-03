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
public class FitxerContingut extends FitxerInfo {

    @Schema(description = "Contingut del fitxer en format binari comprimit en zip",
            format = "byte",
            implementation = String.class)
    private byte[] contingut;

    @Schema(description = "Tipus MIME del fitxer", example = "application/pdf")
    private String mimeType;

}
