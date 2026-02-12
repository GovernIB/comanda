package es.caib.comanda.model.v1.avis;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import es.caib.comanda.model.v1.deserializer.OffsetDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Avis", description = "Representa un avís o notificació publicada a COMANDA")
public class Avis implements Serializable {

    @NotNull
    @Schema(description = "Codi de l'aplicació que publica l'avís", example = "PFI")
    private String appCodi;

    @NotNull
    @Schema(description = "Codi de l'entorn de l'aplicació", example = "DEV")
    private String entornCodi;

    @NotNull
    @Schema(description = "Identificador únic de l'avís", example = "AV-2025-0001")
    private String identificador;

    @NotNull
    @Schema(description = "Tipus d'avís")
    private AvisTipus tipus;

    @NotNull
    @Schema(description = "Títol de l'avís", example = "Interrupció programada")
    private String nom;

    @Schema(description = "Descripció de l'avís", example = "Aturada de manteniment el diumenge a les 8:00")
    private String descripcio;

    @Schema(description = "Data d'inici de vigència", example = "2025-11-21T08:00:00Z", format = "date-time")
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime dataInici;

    @Schema(description = "Data de fi de vigència", example = "2025-11-21T10:00:00Z", format = "date-time")
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime dataFi;

    @Schema(description = "URL de redirecció associada a l'avís", example = "https://dev.caib.es/app/avis/AV-2025-0001", format = "uri")
    private URL redireccio;

    @Schema(description = "Usuari responsable", example = "usr1234")
    private String responsable;

    @Schema(description = "Grup responsable", example = "SUPORT")
    private String grup;

    @Schema(description = "Usuaris amb permís de visualització", example = "[\"usr1\", \"usr2\"]")
    private List<String> usuarisAmbPermis;

    @Schema(description = "Grups amb permís de visualització", example = "[\"USUARIS\"]")
    private List<String> grupsAmbPermis;

    @Schema(hidden = true)
    private Boolean esborrar;
}
