package es.caib.comanda.avisos.logic.helper;

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
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Slf4j
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonitorAvisos {

    private static final String AVIS_DADES_ACCIO = "Obtenir dades d'un avís";
    private static final String AVIS_DADES_ERROR = "S'ha produït un error obtenint les dades de l'avís'";

    private AvisClientHelper avisClientHelper;

    private Long startTime;
    private Monitor monitor;
    private boolean finishedAction;

    public MonitorAvisos(
            Long entornAppId,
            String tascaDadesUrl,
            AvisClientHelper avisClientHelper) {
        String usuariCodi = getAuthenticatedUserCode();
        this.monitor = createMonitor(entornAppId, tascaDadesUrl, AVIS_DADES_ACCIO, usuariCodi);
        this.avisClientHelper = avisClientHelper;
    }

    private Monitor createMonitor(Long entornAppId, String url, String operacio, String codiUsuari) {
        return Monitor.builder()
                .entornAppId(entornAppId)
                .modul(ModulEnum.SALUT)
                .tipus(AccioTipusEnum.SORTIDA)
                .url(url)
                .operacio(operacio)
                .codiUsuari(codiUsuari)
                .build();
    }


    public void startAction() {
        monitor.setData(LocalDateTime.now());
        this.startTime = System.currentTimeMillis();
    }

    public void endAction() {
        monitor.setEstat(EstatEnum.OK);
        monitor.setTempsResposta(System.currentTimeMillis() - this.startTime);
        this.finishedAction = true;
        avisClientHelper.monitorCreate(monitor);
    }

    public void endAction(Throwable t, String errorDescripcio) {
        monitor.setEstat(EstatEnum.ERROR);
        monitor.setTempsResposta(System.currentTimeMillis() - this.startTime);
        monitor.setErrorDescripcio(Strings.isBlank(errorDescripcio) ? AVIS_DADES_ERROR : errorDescripcio);
        monitor.setExcepcioMessage(ExceptionUtils.getMessage(t));
        monitor.setExcepcioStacktrace(ExceptionUtils.getStackTrace(t));
        avisClientHelper.monitorCreate(monitor);
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