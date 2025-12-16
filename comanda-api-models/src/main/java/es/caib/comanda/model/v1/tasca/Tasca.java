package es.caib.comanda.model.v1.tasca;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Tasca", description = "Representa una tasca publicada a COMANDA perquè sigui processada asíncronament")
public class Tasca implements Serializable {

    @Schema(description = "Codi de l'aplicació que publica la tasca", example = "PORTAFIB")
    private String appCodi;

    @Schema(description = "Codi de l'entorn de l'aplicació", example = "DEV")
    private String entornCodi;

    @Schema(description = "Identificador únic de la tasca en l'àmbit de l'APP", example = "TAS-2025-0001")
    private String identificador;

    @Schema(description = "Tipus funcional de la tasca", example = "GENERAR_INFORME")
    private String tipus;

    @Schema(description = "Nom curt de la tasca", example = "Generar informe mensual")
    private String nom;

    @Schema(description = "Descripció detallada de la tasca", example = "Generar l'informe mensual de consums per unitat")
    private String descripcio;

    @Schema(description = "Estat de processament de la tasca")
    private TascaEstat estat;

    @Schema(description = "Descripció de l'estat actual", example = "En cua")
    private String estatDescripcio;

    @Schema(description = "Número d'expedient relacionat (si aplica)", example = "EXP-12345/2025")
    private String numeroExpedient;

    @Schema(description = "Prioritat de la tasca")
    private Prioritat prioritat;

    @Schema(description = "Data d'inici prevista o real", example = "2025-11-20T09:30:00Z", format = "date-time")
    private Date dataInici;

    @Schema(description = "Data de finalització", example = "2025-11-20T10:00:00Z", format = "date-time")
    private Date dataFi;

    @Schema(description = "Data de caducitat límit", example = "2025-12-01T00:00:00Z", format = "date-time")
    private Date dataCaducitat;

    @Schema(description = "URL de redirecció per accedir a la tasca", example = "https://dev.caib.es/app/tasques/TAS-2025-0001", format = "uri")
    private URL redireccio;

    @Schema(description = "Usuari responsable", example = "usr1234")
    private String responsable;

    @Schema(description = "Grup responsable", example = "GESTORS")
    private String grup;

    @Schema(description = "Llista d'usuaris amb permís", example = "[\"usr1\", \"usr2\"]")
    private List<String> usuarisAmbPermis;

    @Schema(description = "Llista de grups amb permís", example = "[\"GESTORS\", \"SUPORT\"]")
    private List<String> grupsAmbPermis;

}
