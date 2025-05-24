package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Slf4j
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonitorEstadistica {

    private static final String ESTADISTICA_INFO_ACCIO = "Obtenir dades estadístiques";
    private static final String ESTADISTICA_DADES_ACCIO = "Obtenir informació de l'estructura de les estadístiques";
    private static final String ESTADISTICA_INFO_ERROR = "S'ha produït un error obtenint la informació de l'estructura de les estadístiques";
    private static final String ESTADISTICA_DADES_ERROR = "S'ha produït un error obtenint les dades estadístiques";

    private EstadisticaClientHelper estadisticaClientHelper;

    private Long startInfoTime;
    private Long startDadesTime;
    private Monitor monitorInfo;
    private Monitor monitorDades;
    private boolean finishedInfoAction;
    private boolean finishedDadesAction;

    public MonitorEstadistica(
            Long entornAppId,
            String estadisticaInfoUrl,
            String estadisticaDadesUrl,
            EstadisticaClientHelper estadisticaClientHelper) {
        String usuariCodi = getAuthenticatedUserCode();
        this.monitorInfo = createMonitor(entornAppId, estadisticaInfoUrl, ESTADISTICA_INFO_ACCIO, usuariCodi);
        this.monitorDades = createMonitor(entornAppId, estadisticaDadesUrl, ESTADISTICA_DADES_ACCIO, usuariCodi);
        this.estadisticaClientHelper = estadisticaClientHelper;
    }

    private Monitor createMonitor(Long entornAppId, String url, String operacio, String codiUsuari) {
        return Monitor.builder()
                .entornAppId(entornAppId)
                .modul(ModulEnum.ESTADISTICA)
                .tipus(AccioTipusEnum.SORTIDA)
                .url(url)
                .operacio(operacio)
                .codiUsuari(codiUsuari)
                .build();
    }


    public void startInfoAction() {
        monitorInfo.setData(LocalDateTime.now());
        this.startInfoTime = System.currentTimeMillis();
    }

    public void startDadesAction() {
        monitorDades.setData(LocalDateTime.now());
        this.startDadesTime = System.currentTimeMillis();
    }

    public void endInfoAction() {
        monitorInfo.setEstat(EstatEnum.OK);
        monitorInfo.setTempsResposta(System.currentTimeMillis() - this.startInfoTime);
        this.finishedInfoAction = true;
        estadisticaClientHelper.monitorCreate(monitorInfo);
    }

    public void endInfoAction(Throwable t) {
        monitorInfo.setEstat(EstatEnum.ERROR);
        monitorInfo.setTempsResposta(System.currentTimeMillis() - this.startInfoTime);
        monitorInfo.setErrorDescripcio(ESTADISTICA_INFO_ERROR);
        monitorInfo.setExcepcioMessage(ExceptionUtils.getMessage(t));
        monitorInfo.setExcepcioStacktrace(ExceptionUtils.getStackTrace(t));
        estadisticaClientHelper.monitorCreate(monitorInfo);
    }

    public void endDadesAction() {
        monitorDades.setEstat(EstatEnum.OK);
        monitorDades.setTempsResposta(System.currentTimeMillis() - this.startDadesTime);
        this.finishedDadesAction = true;
        estadisticaClientHelper.monitorCreate(monitorDades);
    }

    public void endDadesAction(Throwable t) {
        monitorDades.setEstat(EstatEnum.ERROR);
        monitorDades.setTempsResposta(System.currentTimeMillis() - this.startDadesTime);
        monitorDades.setErrorDescripcio(ESTADISTICA_DADES_ERROR);
        monitorDades.setExcepcioMessage(ExceptionUtils.getMessage(t));
        monitorDades.setExcepcioStacktrace(ExceptionUtils.getStackTrace(t));
        estadisticaClientHelper.monitorCreate(monitorDades);
    }


    /**
     * Obté el codi de l'usuari autenticat o retorna "SCHEDULER" si no hi ha cap usuari autenticat o és anònim
     *
     * @return String amb el codi d'usuari o "SCHEDULER"
     */
    private String getAuthenticatedUserCode() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return "SCHEDULER";
            }
            String userName = authentication.getName();
            return userName != null && !userName.isEmpty() ? userName : "SCHEDULER";
        } catch (Exception e) {
            return "SCHEDULER";
        }
    }

}