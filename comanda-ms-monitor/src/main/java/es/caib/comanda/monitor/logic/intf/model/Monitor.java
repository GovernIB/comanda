package es.caib.comanda.monitor.logic.intf.model;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Classe que representa un monitor per a la gestió de dades estadístiques i logs dins del sistema.
 * Inclou informació relacionada amb els esdeveniments registrats, com operacions realitzades, estat, temps de resposta,
 * detalls d'errors, i altra informació rellevant associada al context del sistema o aplicació.
 *
 * Aquesta entitat hereta de BaseResource.
 *
 * Atributs principals:
 * - entornAppId: Identificador de l'entorn d'aplicació.
 * - modul: Mòdul del sistema al qual pertany.
 * - tipus: Tipus d'acció realitzada.
 * - data: Data i hora en què s'ha registrat l'esdeveniment.
 * - url: URL associada a l'operació.
 * - operacio: Descripció de l'operació realitzada.
 * - tempsResposta: Temps de resposta associat a l'acció.
 * - estat: Estat resultant de l'operació (OK, ERROR, WARN).
 * - codiUsuari: Identificador de l'usuari involucrat en l'operació.
 * - errorDescripcio: Descripció d'errors en cas que s'hagin produït.
 * - excepcioMessage: Missatge associat a l'excepció generada (si escau).
 * - excepcioStacktrace: Stack trace de l'excepció en cas d'error.
 *
 * Validacions:
 * - Determinats camps són obligatoris, com entornAppId, modul, tipus, data, operacio i estat.
 * - S'utilitzen restriccions de longitud màxima per als camps cadena (ex. url, operacio, etc.).
 *
 * Aquesta classe és utilitzada principalment pel controlador MonitorController per gestionar operacions de consulta d'indicadors.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
    quickFilterFields = { "operacio", "codiUsuari" },
    artifacts = {@ResourceArtifact(type = ResourceArtifactType.FILTER, code = Monitor.FILTER_MONITOR, formClass = Monitor.FrontFilter.class),}
)
public class Monitor extends BaseResource<Long> {

    public static final String FILTER_MONITOR = "FILTER";

    @NotNull
    private Long entornAppId;
    @NotNull
    private ModulEnum modul;
    @NotNull
    protected AccioTipusEnum tipus;
    @NotNull
    private LocalDateTime data;
    @Size(max = 255)
    private String url;
    @NotNull
    @Size(max = 255)
    private String operacio;
    private Long tempsResposta;
    @NotNull
    private EstatEnum estat;
    @Size(max = 64)
    private String codiUsuari;
    @Size(max = 1024)
    private String errorDescripcio;
    @Size(max = 1024)
    private String excepcioMessage;
    @Size(max = 4000)
    private String excepcioStacktrace;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class FrontFilter implements Serializable {
        private String codi;
        private LocalDate dataDesde;
        private LocalDate dataFins;
        private AccioTipusEnum tipus;
        private EstatEnum estat;
        private String descripcio;
    }

}
