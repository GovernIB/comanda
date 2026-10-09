package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet.FetObtenirResponse;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Indicador.SincronitzarCatalegResponse;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.ProcesBaixaPrioritat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Servei per gestionar els processos de recuperació de dades estadístiques en segon pla
 * de baixa prioritat de manera menys invasiva.
 *
 * Fa el seguiment dels processos mitjançant una estructura thread-safe local en memòria.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class ProcesBaixaPrioritatService {

    private final EstadisticaClientHelper estadisticaClientHelper;
    private final EstadisticaHelper estadisticaHelper;
    private final TaskExecutor estadisticaWorkerExecutor;

    public static final long PAUSA_MS_PER_DEFECTE = 500L;
    private long pausaMsPerDefecte = PAUSA_MS_PER_DEFECTE;

    private static final int MAX_PROCESSOS_HISTORIC = 100;
    private final Map<String, ProcesBaixaPrioritat> processos = new ConcurrentHashMap<>();

    private static final DateTimeFormatter FORMATTER_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ProcesBaixaPrioritatService(
            EstadisticaClientHelper estadisticaClientHelper,
            EstadisticaHelper estadisticaHelper,
            @Qualifier("estadisticaWorkerExecutor") TaskExecutor estadisticaWorkerExecutor) {
        this.estadisticaClientHelper = estadisticaClientHelper;
        this.estadisticaHelper = estadisticaHelper;
        this.estadisticaWorkerExecutor = estadisticaWorkerExecutor;
    }

    /**
     * Inicia un procés de recuperació de baixa prioritat en segon pla per al període especificat.
     *
     * @param entornAppId identificador de l'entorn d'aplicació
     * @param dataInici data d'inici
     * @param dataFi data de finalització (opcional, si és nul·la s'utilitza dataInici)
     * @param pausaMs temps de pausa en mil·lisegons entre dies (opcional, si és nul s'utilitza el valor per defecte)
     * @return instància de ProcesBaixaPrioritat creada
     */
    public ProcesBaixaPrioritat iniciarProces(Long entornAppId, LocalDate dataInici, LocalDate dataFi, Long pausaMs) {
        if (entornAppId == null || dataInici == null) {
            throw new IllegalArgumentException("entornAppId i dataInici són obligatoris");
        }

        LocalDate fi = (dataFi != null) ? dataFi : dataInici;
        if (fi.isBefore(dataInici)) {
            throw new IllegalArgumentException("dataFi no pot ser anterior a dataInici");
        }

        EntornApp entornApp = estadisticaClientHelper.entornAppFindById(entornAppId);
        if (entornApp == null) {
            throw new IllegalArgumentException("L'entornApp " + entornAppId + " no existeix");
        }
        if (!entornApp.isActiva()) {
            throw new IllegalStateException("L'entornApp " + entornAppId + " no està actiu");
        }
        if (entornApp.getEstadisticaUrl() == null || entornApp.getEstadisticaUrl().trim().isEmpty()) {
            throw new IllegalStateException("L'entornApp " + entornAppId + " no té URL d'estadística configurada");
        }

        // Generem les dates en ordre descendent (les més recents primer) per omplir el calendari del final cap al principi
        List<LocalDate> dates = new ArrayList<>();
        LocalDate curr = fi;
        while (!curr.isBefore(dataInici)) {
            dates.add(curr);
            curr = curr.minusDays(1);
        }

        String appNom = entornApp.getApp() != null ? entornApp.getApp().getNom() : "";
        String entornNom = entornApp.getEntorn() != null ? entornApp.getEntorn().getNom() : "";
        String entornAppNom = appNom + (appNom.isEmpty() || entornNom.isEmpty() ? "" : " - ") + entornNom;
        if (entornAppNom.isEmpty()) {
            entornAppNom = "Entorn " + entornAppId;
        }

        long pausaEfectiva = (pausaMs != null && pausaMs >= 0) ? pausaMs : this.pausaMsPerDefecte;

        ProcesBaixaPrioritat proces = ProcesBaixaPrioritat.builder()
                .id(UUID.randomUUID().toString())
                .entornAppId(entornAppId)
                .entornAppNom(entornAppNom)
                .dataInici(dataInici)
                .dataFi(fi)
                .pausaMs(pausaEfectiva)
                .estat(ProcesBaixaPrioritat.ESTAT_PENDENT)
                .dataIniciExecucio(LocalDateTime.now())
                .totalDies(dates.size())
                .diesProcessats(0)
                .diesAmbDades(0)
                .diesAmbErrors(0)
                .percentatge(0)
                .build();

        limitarCapacitat();
        processos.put(proces.getId(), proces);

        log.info("S'ha encolet el procés de baixa prioritat {} per a entornAppId {} ({} dies: {} a {}, pausa: {} ms)",
                proces.getId(), entornAppId, dates.size(), dataInici, fi, pausaEfectiva);

        try {
            estadisticaWorkerExecutor.execute(() -> executarProces(proces, dates));
        } catch (Exception e) {
            log.error("Error al programar el procés de baixa prioritat {} al worker", proces.getId(), e);
            proces.setEstat(ProcesBaixaPrioritat.ESTAT_ERROR);
            proces.setMissatge("Error en programar l'execució: " + e.getMessage());
            proces.setDataFiExecucio(LocalDateTime.now());
        }

        return proces;
    }

    public ProcesBaixaPrioritat iniciarProces(Long entornAppId, LocalDate dataInici, LocalDate dataFi) {
        return iniciarProces(entornAppId, dataInici, dataFi, null);
    }

    /**
     * Executa el procés dia a dia en segon pla amb una pausa entre dies per minimitzar l'impacte.
     */
    void executarProces(ProcesBaixaPrioritat proces, List<LocalDate> dates) {
        if (proces.isCancelled()) {
            log.info("El procés {} ja està cancel·lat abans d'iniciar", proces.getId());
            proces.setEstat(ProcesBaixaPrioritat.ESTAT_CANCELAT);
            proces.setMissatge("Procés cancel·lat per l'usuari");
            return;
        }

        proces.setRunningThread(Thread.currentThread());
        proces.setEstat(ProcesBaixaPrioritat.ESTAT_EN_EXECUCIO);
        log.info("Iniciant execució de baixa prioritat per al procés {}", proces.getId());

        try {
            EntornApp entornApp = estadisticaClientHelper.entornAppFindById(proces.getEntornAppId());
            if (entornApp == null) {
                throw new IllegalStateException("L'entornApp " + proces.getEntornAppId() + " no existeix");
            }
            if (!entornApp.isActiva()) {
                throw new IllegalStateException("L'entornApp " + proces.getEntornAppId() + " no està actiu");
            }
            if (entornApp.getEstadisticaUrl() == null || entornApp.getEstadisticaUrl().trim().isEmpty()) {
                throw new IllegalStateException("L'entornApp " + proces.getEntornAppId() + " no té URL d'estadística configurada");
            }

            // Sincronització de catàleg un sol cop abans del bucle per evitar consultes redundants
            if (entornApp.getEstadisticaInfoUrl() != null && !entornApp.getEstadisticaInfoUrl().trim().isEmpty()) {
                try {
                    log.info("Procés {}: sincronitzant catàleg d'estadística abans d'iniciar la recuperació diària", proces.getId());
                    SincronitzarCatalegResponse catRes = estadisticaHelper.sincronitzarEstadisticaInfo(entornApp);
                    if (catRes != null && !catRes.isSuccess()) {
                        log.warn("Procés {}: error en sincronitzar el catàleg abans de les dades: {}", proces.getId(), catRes.getMessage());
                    }
                } catch (Exception ex) {
                    log.warn("Procés {}: error en sincronitzar el catàleg abans de les dades: {}", proces.getId(), ex.getMessage());
                }
            }

            if (proces.isCancelled() || Thread.currentThread().isInterrupted()) {
                log.info("El procés {} ha estat cancel·lat per l'usuari abans d'iniciar el bucle de dies", proces.getId());
                proces.setEstat(ProcesBaixaPrioritat.ESTAT_CANCELAT);
                proces.setMissatge("Procés cancel·lat per l'usuari");
                if (proces.getDataFiExecucio() == null) {
                    proces.setDataFiExecucio(LocalDateTime.now());
                }
                return;
            }

            for (int i = 0; i < dates.size(); i++) {
                LocalDate data = dates.get(i);
                if (proces.isCancelled() || Thread.currentThread().isInterrupted()) {
                    log.info("El procés {} ha estat cancel·lat per l'usuari", proces.getId());
                    proces.setEstat(ProcesBaixaPrioritat.ESTAT_CANCELAT);
                    proces.setMissatge("Procés cancel·lat per l'usuari");
                    if (proces.getDataFiExecucio() == null) {
                        proces.setDataFiExecucio(LocalDateTime.now());
                    }
                    return;
                }

                proces.setDiaActual(data);
                String dataFormatada = data.format(FORMATTER_DD_MM_YYYY);
                String estadisticaUrl = entornApp.getEstadisticaUrl() + "/of/" + dataFormatada;

                try {
                    log.debug("Procés {}: obtenint dia {}", proces.getId(), dataFormatada);
                    FetObtenirResponse response = estadisticaHelper.getEstadisticaDadesAmbUrl(entornApp, estadisticaUrl, false);
                    if (response != null && Boolean.TRUE.equals(response.getSuccess())) {
                        String dataIso = data.format(FORMATTER_YYYY_MM_DD);
                        boolean hasData = response.getDiesAmbDades() != null && Boolean.TRUE.equals(response.getDiesAmbDades().get(dataIso));
                        if (hasData) {
                            proces.setDiesAmbDades(proces.getDiesAmbDades() + 1);
                        }
                    } else {
                        proces.setDiesAmbErrors(proces.getDiesAmbErrors() + 1);
                    }
                } catch (Exception ex) {
                    log.warn("Procés {}: error en obtenir dades del dia {}: {}", proces.getId(), dataFormatada, ex.getMessage());
                    proces.setDiesAmbErrors(proces.getDiesAmbErrors() + 1);
                }

                proces.setDiesProcessats(proces.getDiesProcessats() + 1);
                if (proces.getTotalDies() > 0) {
                    proces.setPercentatge((proces.getDiesProcessats() * 100) / proces.getTotalDies());
                }

                // Pausa entre peticions per no saturar el sistema (només entre dies, no després del darrer)
                if (i < dates.size() - 1 && !proces.isCancelled()) {
                    ferPausa(proces.getPausaMs());
                }
            }

            if (proces.isCancelled() || Thread.currentThread().isInterrupted()) {
                proces.setEstat(ProcesBaixaPrioritat.ESTAT_CANCELAT);
                proces.setMissatge("Procés cancel·lat per l'usuari");
                if (proces.getDataFiExecucio() == null) {
                    proces.setDataFiExecucio(LocalDateTime.now());
                }
                return;
            }

            if (proces.getDiesAmbErrors() == proces.getTotalDies() && proces.getTotalDies() > 0) {
                proces.setEstat(ProcesBaixaPrioritat.ESTAT_ERROR);
                proces.setMissatge("S'han produït errors en la recuperació de tots els dies");
            } else if (proces.getDiesAmbErrors() > 0) {
                proces.setEstat(ProcesBaixaPrioritat.ESTAT_FINALITZAT);
                proces.setMissatge("Procés finalitzat amb " + proces.getDiesAmbErrors() + " dies amb error");
            } else {
                proces.setEstat(ProcesBaixaPrioritat.ESTAT_FINALITZAT);
                proces.setMissatge("Procés finalitzat correctament");
            }
        } catch (Exception e) {
            log.error("Error en l'execució del procés de baixa prioritat {}", proces.getId(), e);
            if (!proces.isCancelled()) {
                proces.setEstat(ProcesBaixaPrioritat.ESTAT_ERROR);
                proces.setMissatge(e.getMessage());
            }
        } finally {
            proces.setRunningThread(null);
            proces.setDiaActual(null);
            if (!ProcesBaixaPrioritat.ESTAT_CANCELAT.equals(proces.getEstat())) {
                proces.setDataFiExecucio(LocalDateTime.now());
            }
            log.info("Procés de baixa prioritat {} finalitzat amb estat {}", proces.getId(), proces.getEstat());
        }
    }

    private void ferPausa(Long pausa) {
        long ms = (pausa != null && pausa >= 0) ? pausa : this.pausaMsPerDefecte;
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Retorna la llista de processos creats, opcionalment filtrats per entornAppId.
     * Ordenats per data d'inici d'execució descendent.
     */
    public List<ProcesBaixaPrioritat> getProcessos(Long entornAppId) {
        return processos.values().stream()
                .filter(p -> entornAppId == null || entornAppId.equals(p.getEntornAppId()))
                .sorted(Comparator.comparing(ProcesBaixaPrioritat::getDataIniciExecucio, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Obté un procés pel seu identificador únic.
     */
    public Optional<ProcesBaixaPrioritat> getProces(String id) {
        return Optional.ofNullable(processos.get(id));
    }

    /**
     * Cancel·la un procés en curs o pendent.
     */
    public boolean cancelarProces(String id) {
        ProcesBaixaPrioritat proces = processos.get(id);
        if (proces != null && (ProcesBaixaPrioritat.ESTAT_EN_EXECUCIO.equals(proces.getEstat())
                || ProcesBaixaPrioritat.ESTAT_PENDENT.equals(proces.getEstat()))) {
            proces.setCancelled(true);
            proces.setEstat(ProcesBaixaPrioritat.ESTAT_CANCELAT);
            proces.setMissatge("Procés cancel·lat per l'usuari");
            proces.setDataFiExecucio(LocalDateTime.now());
            proces.setDiaActual(null);
            Thread t = proces.getRunningThread();
            if (t != null) {
                t.interrupt();
            }
            return true;
        }
        return false;
    }

    /**
     * Limita la quantitat de processos emmagatzemats en memòria per evitar fuites de recursos.
     */
    private void limitarCapacitat() {
        if (processos.size() >= MAX_PROCESSOS_HISTORIC) {
            List<String> candidatsPerEliminar = processos.values().stream()
                    .filter(p -> ProcesBaixaPrioritat.ESTAT_FINALITZAT.equals(p.getEstat())
                            || ProcesBaixaPrioritat.ESTAT_ERROR.equals(p.getEstat())
                            || ProcesBaixaPrioritat.ESTAT_CANCELAT.equals(p.getEstat()))
                    .sorted(Comparator.comparing(ProcesBaixaPrioritat::getDataIniciExecucio, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .map(ProcesBaixaPrioritat::getId)
                    .collect(Collectors.toList());

            int aEliminar = (processos.size() - MAX_PROCESSOS_HISTORIC) + 1;
            for (int i = 0; i < Math.min(aEliminar, candidatsPerEliminar.size()); i++) {
                processos.remove(candidatsPerEliminar.get(i));
            }
        }
    }

    public void setPausaMs(long pausaMs) {
        this.pausaMsPerDefecte = pausaMs;
    }
}
