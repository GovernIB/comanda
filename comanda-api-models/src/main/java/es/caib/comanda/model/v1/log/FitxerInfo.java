package es.caib.comanda.model.v1.log;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import es.caib.comanda.model.v1.deserializer.OffsetDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

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
    @Schema(description = "Data de creació del fitxer en format dd/MM/yyyy HH:mm:ss", example = "2025-11-21T10:00:00Z", format = "date-time")
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime dataCreacio;
    @Schema(description = "Data de modificació del fitxer en format dd/MM/yyyy HH:mm:ss", example = "2025-11-21T10:00:00Z", format = "date-time")
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime dataModificacio;
}
