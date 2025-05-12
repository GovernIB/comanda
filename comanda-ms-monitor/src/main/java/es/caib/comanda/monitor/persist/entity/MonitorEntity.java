package es.caib.comanda.monitor.persist.entity;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.management.monitor.Monitor;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Representa l'entitat MonitorEntity que modela les dades d'auditoria i monitorització dins
 * l'aplicació. Defineix les propietats i comportament relacionats amb registres de monitoratge.
 * Està construïda a partir de BaseEntity.
 *
 * Les propietats d'aquesta entitat permeten descriure informació rellevant sobre l'activitat
 * registrada, com el codi de mòdul, tipus d'acció, estat, errors i temps de resposta.
 *
 * Aquesta entitat està configurada per ser persistida a la base de dades en una taula específica
 * definida a partir del prefix general de configuració.
 *
 * Funcionalitats principals inclouen:
 * - Tractament i limitació de longituds per a descripcions d'errors i traça d'excepcions.
 * - Suport per a enumeracions com ModulEnum, AccioTipusEnum i EstatEnum.
 * - Capacitat per a registrar operacions específiques, usuari i detalls temporals.
 *
 * Aquest component permet la configuració personalitzada via constructors o patrons Builder,
 * afavorint la seguretat i integritat de les dades emmagatzemades.
 *
 * @author Límit Tecnologies
 */
@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = BaseConfig.DB_PREFIX + "monitor")
public class MonitorEntity extends BaseEntity<Monitor> {

    private static int ERROR_DESC_MAX_LENGTH = 1024;
    private static int ERROR_STACK_MAX_LENGTH = 4000;

    @Column(name = "entorn_app_id")
    private Long entornAppId;

    @Column(name = "codi", length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    private ModulEnum modul;

    @Column(name = "tipus", nullable = false)
    @Enumerated(EnumType.STRING)
    protected AccioTipusEnum tipus;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "operacio", length = 255, nullable = false)
    private String operacio;

    @Column(name = "temps_resposta")
    private Long tempsResposta;

    @Builder.Default
    @Column(name = "estat", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstatEnum estat = EstatEnum.OK;

    @Column(name = "codi_usuari", length = 64)
    private String codiUsuari;

    @Column(name = "error_descripcio", length = 1024)
    private String errorDescripcio;

    @Column(name = "excepcio_msg", length = 1024)
    private String excepcioMessage;

    @Column(name = "excepcio_stacktrace", length = 4000)
    private String excepcioStacktrace;



    public void setErrorDescripcio(String errorDescripcio) {
        this.errorDescripcio = StringUtils.abbreviate(errorDescripcio, ERROR_DESC_MAX_LENGTH);
    }
    public void setExcepcioMessage(String excepcioMessage) {
        this.excepcioMessage = StringUtils.abbreviate(excepcioMessage, ERROR_DESC_MAX_LENGTH);
    }

    public void setExcepcioStacktrace(String excepcioStacktrace) {
        this.excepcioStacktrace = StringUtils.abbreviate(excepcioStacktrace, ERROR_STACK_MAX_LENGTH);
    }

    // Custom builder setters
    public static class MonitorEntityBuilder {
        public MonitorEntity.MonitorEntityBuilder errorDescripcio(String errorDescripcio){
            this.errorDescripcio = StringUtils.abbreviate(errorDescripcio, ERROR_DESC_MAX_LENGTH);
            return this;
        }

        public MonitorEntity.MonitorEntityBuilder excepcioMessage(String excepcioMessage){
            this.excepcioMessage = StringUtils.abbreviate(excepcioMessage, ERROR_DESC_MAX_LENGTH);
            return this;
        }

        public MonitorEntity.MonitorEntityBuilder excepcioStacktrace(String excepcioStacktrace){
            this.excepcioStacktrace = StringUtils.abbreviate(excepcioStacktrace, ERROR_DESC_MAX_LENGTH*2);
            return this;
        }
    }
}