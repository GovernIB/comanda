package es.caib.comanda.ms.broker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Avis", description = "Representa un avís o notificació publicada a COMANDA")
public class Avis implements Serializable {

    @Schema(description = "Codi de l'aplicació que publica l'avís", example = "PFI")
    private String appCodi;

    @Schema(description = "Codi de l'entorn de l'aplicació", example = "DEV")
    private String entornCodi;

    @Schema(description = "Identificador únic de l'avís", example = "AV-2025-0001")
    private String identificador;

    @Schema(description = "Tipus d'avís")
    private AvisTipus tipus;

    @Schema(description = "Títol de l'avís", example = "Interrupció programada")
    private String nom;

    @Schema(description = "Descripció de l'avís", example = "Aturada de manteniment el diumenge a les 8:00")
    private String descripcio;

    @Schema(description = "Data d'inici de vigència", example = "2025-11-21T08:00:00Z", format = "date-time")
    private Date dataInici;

    @Schema(description = "Data de fi de vigència", example = "2025-11-21T10:00:00Z", format = "date-time")
    private Date dataFi;

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
}
