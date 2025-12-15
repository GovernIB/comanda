package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet.FetObtenirResponse;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.model.v1.estadistica.DimensioDesc;
import es.caib.comanda.model.v1.estadistica.Dimensio;
import es.caib.comanda.model.v1.estadistica.EstadistiquesInfo;
import es.caib.comanda.model.v1.estadistica.IndicadorDesc;
import es.caib.comanda.model.v1.estadistica.RegistreEstadistic;
import es.caib.comanda.model.v1.estadistica.RegistresEstadistics;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.TempsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Lògica comuna per a obtenir i consultar informació estadística de les apps.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EstadisticaHelper {

    @Lazy
    private final EstadisticaHelper self = this;

    private final DimensioRepository dimensioRepository;
    private final DimensioValorRepository dimensioValorRepository;
    private final IndicadorRepository indicadorRepository;
    private final TempsRepository tempsRepository;
    private final FetRepository fetRepository;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final RestTemplate restTemplate;

    private static final ConcurrentHashMap<Long, Object> LOCKS = new ConcurrentHashMap<>();

    // OBTENCIÓ i DESAT D'ESTADISTIQUES
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Obté i processa informació estadística d'una aplicació a partir dels URLs proporcionades.
     * La informació inclou indicadors, dimensions i registres estadístics que es creen i es guarden en el sistema a partir de les dades rebudes.
     *
     * @param entornApp               Objecte que representa l'aplicació i l'entorn per als quals es recupera informació estadística.
     */
    @Transactional
    public void getEstadisticaInfoDades(EntornApp entornApp) {
        getEstadisticaInfoDades(entornApp, null);
    }

    @Transactional
    public void getEstadisticaInfoDades(EntornApp entornApp, Integer dies) {
        log.debug("Obtenint informació i dades estadístiques de l'app {}, entorn {}",
                entornApp.getApp().getNom(),
                entornApp.getEntorn().getNom());
        String estadisticaUrl = buildEstadisticaUrl(entornApp, dies);

        MonitorEstadistica monitorEstadistica = initializeMonitor(entornApp, estadisticaUrl);

        try {
            processEstadisticaInfo(entornApp, restTemplate, monitorEstadistica);
            processEstadisticaDades(entornApp, estadisticaUrl, restTemplate, monitorEstadistica, dies != null);
        } catch (RestClientException ex) {
            handleEstadisticaException(entornApp, monitorEstadistica, ex);
        }
    }

    /**
     * Obté i processa informació estadística d'una aplicació a partir d'una URL específica.
     * La informació inclou indicadors, dimensions i registres estadístics que es creen i es guarden en el sistema a partir de les dades rebudes.
     *
     * @param entornApp      Objecte que representa l'aplicació i l'entorn per als quals es recupera informació estadística.
     * @param estadisticaUrl URL específica per obtenir les dades estadístiques.
     * @param multiplesDies  Indica si s'espera rebre múltiples dies de dades estadístiques.
     */
    @Transactional
    public FetObtenirResponse getEstadisticaInfoDadesAmbUrl(EntornApp entornApp, String estadisticaUrl, boolean multiplesDies) {
        log.debug("Obtenint informació i dades estadístiques de l'app {}, entorn {} amb URL específica: {}",
                entornApp.getApp().getNom(),
                entornApp.getEntorn().getNom(),
                estadisticaUrl);

        MonitorEstadistica monitorEstadistica = initializeMonitor(entornApp, estadisticaUrl);

        try {
            processEstadisticaInfo(entornApp, restTemplate, monitorEstadistica);
            var result = processEstadisticaDades(entornApp, estadisticaUrl, restTemplate, monitorEstadistica, multiplesDies);
            return FetObtenirResponse.builder().success(true).diesAmbDades(result).build();
        } catch (RestClientException ex) {
            handleEstadisticaException(entornApp, monitorEstadistica, ex);
            return FetObtenirResponse.builder().success(false).message(ex.getLocalizedMessage()).build();
        }
    }

    private String buildEstadisticaUrl(EntornApp entornApp, Integer dies) {
        return dies != null ? entornApp.getEstadisticaUrl() + "/" + dies : entornApp.getEstadisticaUrl();
    }

    private MonitorEstadistica initializeMonitor(EntornApp entornApp, String estadisticaUrl) {
        return new MonitorEstadistica(
                entornApp.getId(),
                entornApp.getEstadisticaInfoUrl(),
                estadisticaUrl,
                estadisticaClientHelper);
    }


    // Obtenir informació estadística de l'app i dimensions
    private void processEstadisticaInfo(EntornApp entornApp, RestTemplate restTemplate, MonitorEstadistica monitorEstadistica) throws RestClientException {
        Object lock = LOCKS.computeIfAbsent(entornApp.getId(), k -> new Object());
        synchronized (lock) {
            monitorEstadistica.startInfoAction();
            EstadistiquesInfo estadistiquesInfo = restTemplate.getForObject(entornApp.getEstadisticaInfoUrl(), EstadistiquesInfo.class);
            monitorEstadistica.endInfoAction();
            // Guardar la inforció de l'estructura de les dades estadístiques
            crearIndicadorsIDimensions(estadistiquesInfo, entornApp.getId());
        }
    }

    // Obtenir les dades estadístiques
    private Map<String, Boolean> processEstadisticaDades(EntornApp entornApp, String estadisticaUrl, RestTemplate restTemplate, MonitorEstadistica monitorEstadistica, boolean multiplesDies) throws RestClientException {
        Object lock = LOCKS.computeIfAbsent(entornApp.getId(), k -> new Object());
        Map<String, Boolean> result = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        synchronized (lock) {
            monitorEstadistica.startDadesAction();
            if (multiplesDies) {
                List<RegistresEstadistics> registresEstadistics = restTemplate.exchange(
                        estadisticaUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<RegistresEstadistics>>() {
                        }).getBody();
                monitorEstadistica.endDadesAction();
                // Guardar les dades estadístiques
                registresEstadistics.forEach(r -> {
                    crearEstadistiques(r, entornApp.getId());
                    result.put(sdf.format(r.getTemps().getData()), getRegistreEstadisticMessage(r));
                });
            } else {
                RegistresEstadistics registresEstadistics = restTemplate.getForObject(estadisticaUrl, RegistresEstadistics.class);
                monitorEstadistica.endDadesAction();
                // Guardar les dades estadístiques
                crearEstadistiques(registresEstadistics, entornApp.getId());
                result.put(sdf.format(registresEstadistics.getTemps().getData()), getRegistreEstadisticMessage(registresEstadistics));
            }
        }
        return result;
    }

    private Boolean getRegistreEstadisticMessage(RegistresEstadistics registresEstadistics) {
        return !(registresEstadistics.getFets() == null || registresEstadistics.getFets().isEmpty());
    }

    private void handleEstadisticaException(EntornApp entornApp, MonitorEstadistica monitorEstadistica, RestClientException ex) {
        String warnMsg = monitorEstadistica.isFinishedInfoAction()
                ? "No s'han pogut obtenir dades estadístiques "
                : "No s'ha pogut obtenir informació estadística ";
        log.warn(warnMsg + "de l'app {}, entorn {}: {}",
                entornApp.getApp().getNom(),
                entornApp.getEntorn().getNom(),
                ex.getLocalizedMessage());
        if (!monitorEstadistica.isFinishedInfoAction()) {
            monitorEstadistica.endInfoAction(ex);
        } else if (!monitorEstadistica.isFinishedDadesAction()) {
            monitorEstadistica.endDadesAction(ex);
        }
    }


    /**
     * Crea indicadors i dimensions associats a un entorn d'aplicació especificat a partir de la informació proporcionada
     * en un objecte EstadistiquesInfo.
     *
     * @param estadistiquesInfo Objecte que conté la llista d'indicadors i dimensions
     *                          que s'han de crear.
     * @param entornAppId Identificador de l'entorn d'aplicació amb el qual s'associaran
     *                    els indicadors i dimensions.
     */
    private void crearIndicadorsIDimensions(EstadistiquesInfo estadistiquesInfo, Long entornAppId) {
        crearIndicadors(estadistiquesInfo.getIndicadors(), entornAppId);
        crearDimensions(estadistiquesInfo.getDimensions(), entornAppId);
    }


    /**
     * Crea i actualitza els indicadors associats a un entorn d'aplicació especificat.
     * Si un indicador amb el mateix nom ja existeix per a l'entorn, s'actualitzen les seves propietats. Si no existeix, se'n crea un de nou.
     *
     * @param indicadors Llista d'objectes IndicadorDesc que contenen la informació dels indicadors a crear o actualitzar.
     * @param entornAppId Identificador de l'entorn d'aplicació amb el qual s'associen els indicadors.
     */
    private void crearIndicadors(List<IndicadorDesc> indicadors, Long entornAppId) {
        if (indicadors != null) {
            indicadors.forEach(i -> {
                if (Strings.isBlank(i.getNom()))
                    return;

                IndicadorEntity indicador = indicadorRepository.findByCodiAndEntornAppId(i.getCodi(), entornAppId)
                        .orElseGet(() -> new IndicadorEntity());

                indicador.setEntornAppId(entornAppId);
                indicador.setCodi(i.getCodi());
                indicador.setNom(i.getNom());
                if (!Strings.isBlank(i.getDescripcio())) {
                    indicador.setDescripcio(i.getDescripcio());
                }
                if (i.getFormat() != null) {
                    indicador.setFormat(i.getFormat());
                }
                indicadorRepository.save(indicador);
            });
        }
    }

    /**
     * Crea o actualitza les dimensions associades a un entorn d'aplicació especificat.
     * Processa cada dimensió proporcionada, cercant si ja existeix pel nom i l'identificador de l'entorn. Si existeix, s'actualitza;
     * si no, es crea una nova dimensió. També gestiona els valors associats a cada dimensió.
     *
     * @param dimensions Llista d'objectes DimensioDesc que contenen la informació de les dimensions i els seus valors associats.
     * @param entornAppId Identificador de l'entorn d'aplicació amb el qual s'associen les dimensions.
     */
    private void crearDimensions(List<DimensioDesc> dimensions, Long entornAppId) {
        if (dimensions == null) return;

        for (DimensioDesc d : dimensions) {
            if (Strings.isBlank(d.getNom())) continue;

            DimensioEntity dimensio = dimensioRepository.findByCodiAndEntornAppId(d.getCodi(), entornAppId)
                    .orElseGet(DimensioEntity::new);

            dimensio.setCodi(d.getCodi());
            dimensio.setNom(d.getNom());
            dimensio.setEntornAppId(entornAppId);
            if (!Strings.isBlank(d.getDescripcio())) {
                dimensio.setDescripcio(d.getDescripcio());
            }
            DimensioEntity dimensioSaved = dimensioRepository.save(dimensio);

            if (d.getValors() != null && !d.getValors().isEmpty()) {
                Set<String> uniqueValues = new HashSet<>(d.getValors());
                Set<String> existingValues = new HashSet<>();

                List<List<String>> valueBatches = splitIntoBatches(new ArrayList<>(uniqueValues), 900);
                for (List<String> batch : valueBatches) {
                    List<DimensioValorEntity> existing = findExistingDimensioValors(dimensioSaved, batch);
                    existingValues.addAll(existing.stream()
                            .map(DimensioValorEntity::getValor)
                            .collect(Collectors.toSet()));
                }

                createMissingDimensioValors(dimensioSaved, uniqueValues, existingValues);
            }
        }
    }

    private void createMissingDimensioValors(DimensioEntity dimensio, Set<String> values, Set<String> existingValues) {
        List<DimensioValorEntity> newValues = values.stream()
                .filter(v -> !existingValues.contains(v))
                .map(v -> {
                    DimensioValorEntity valor = new DimensioValorEntity();
                    valor.setDimensio(dimensio);
                    valor.setValor("".equals(v) ? null : v);
                    return valor;
                })
                .collect(Collectors.toList());

        if (!newValues.isEmpty()) {
            dimensioValorRepository.saveAll(newValues);
        }
    }

    private List<DimensioValorEntity> findExistingDimensioValors(DimensioEntity dimensio, List<String> values) {
        return dimensioValorRepository.findByDimensioAndValorIn(dimensio, values);
    }

    private <T> List<List<T>> splitIntoBatches(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    /**
     * Crea les estadístiques associades a un entorn d'aplicació específic.
     * Aquesta operació inclou la creació d'una entitat de temps i els fets estadístics corresponents basats en els registres proporcionats.
     *
     * @param registresEstadistics Objecte que conté els registres estadístics, incloent-hi informació temporal i els fets a processar.
     * @param entornAppId Identificador de l'entorn d'aplicació amb el qual s'associen les estadístiques creades.
     */
    private void crearEstadistiques(RegistresEstadistics registresEstadistics, Long entornAppId) {
        TempsEntity temps = crearTemps(registresEstadistics.getTemps());
        crearFets(registresEstadistics.getFets(), temps, entornAppId);
    }

    /**
     * Crea o recupera una entitat de tipus TempsEntity a partir de la informació proporcionada en un objecte Temps.
     * Si ja existeix una entitat TempsEntity amb la mateixa data, es recupera; en cas contrari, se'n crea una de nova.
     *
     * @param temps l'objecte Temps que conté la informació temporal necessària per crear o recuperar un TempsEntity.
     * @return l'entitat TempsEntity corresponent a la data proporcionada, o null si l'objecte Temps és null.
     */
    private static final ConcurrentHashMap<LocalDate, Object> TIME_LOCKS = new ConcurrentHashMap<>();

    private TempsEntity crearTemps(es.caib.comanda.model.v1.estadistica.Temps temps) {
        if (temps == null)
            return null;

        LocalDate data = LocalDate.from(temps.getData().toInstant().atZone(ZoneId.systemDefault()));
        Object lock = TIME_LOCKS.computeIfAbsent(data, k -> new Object());

        synchronized (lock) {
            return self.createOrGetTempsEntity(data);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TempsEntity createOrGetTempsEntity(LocalDate data) {
        TempsEntity tempsEntity = tempsRepository.findByData(data);
        if (tempsEntity == null) {
            tempsEntity = tempsRepository.save(new TempsEntity(data));
        }
        return tempsEntity;
    }

    /**
     * Crea registres de tipus "Fets" associats a un entorn d'aplicació específic basats en els registres estadístics i el temps proporcionats.
     *
     * @param registresEstadistics Llista de registres estadístics que contenen dimensions i fets per processar. Si la llista és buida o nul·la,
     *                             no es farà cap operació.
     * @param temps Entitat que representa el moment temporal associat als fets que es crearan.
     * @param entornAppId Identificador de l'entorn d'aplicació amb el qual s'associen els registres de "Fets" creats.
     */
    private void crearFets(List<RegistreEstadistic> registresEstadistics, TempsEntity temps, Long entornAppId) {
        if (registresEstadistics == null || registresEstadistics.isEmpty()) {
            return;
        }

        // Esborrar registres anteriors
        fetRepository.deleteAllByTempsAndEntornAppId(temps, entornAppId);

        System.out.println("> Crear fets: " + (registresEstadistics != null ? registresEstadistics.size() : 0) + " registres)");

        // Preparar llista per batch insert
        List<FetEntity> fetsPerGuardar = new ArrayList<>();
        int batchSize = 500; // Ajusta segons necessitats

        int indexReg = 1;

        for (RegistreEstadistic re : registresEstadistics) {
            System.out.println(">>> Registre estadístic: " + indexReg++ + "(" + (re.getFets() != null ? re.getFets().size() : 0) + " fets)");
            if (re.getFets() == null || re.getDimensions() == null) {
                continue;
            }

            // Crear mapa per dimensions
            Map<String, String> dimensionsMap = new HashMap<>();
            for (Dimensio d : re.getDimensions()) {
                dimensionsMap.put(d.getCodi(), d.getValor());
            }

            // Crear mapa per indicadors
            Map<String, Double> indicadorsValuesMap = new HashMap<>();
            for (es.caib.comanda.model.v1.estadistica.Fet f : re.getFets()) {
                indicadorsValuesMap.put(f.getCodi(), f.getValor());
            }

            // Crear una única entitat FetEntity per cada combinació de dimensions i temps
            FetEntity fet = new FetEntity();
            fet.setEntornAppId(entornAppId);
            fet.setTemps(temps);
            fet.setDimensionsJson(dimensionsMap);
            fet.setIndicadorsJson(indicadorsValuesMap);
            fetsPerGuardar.add(fet);

            // Quan arribem al batch size, guardem el lot
            if (fetsPerGuardar.size() >= batchSize) {
                fetRepository.saveAll(fetsPerGuardar);
                fetsPerGuardar.clear();
            }
        }

        // Guardar els fets restants
        if (!fetsPerGuardar.isEmpty()) {
            fetRepository.saveAll(fetsPerGuardar);
        }
    }



    protected Fet toFet(FetEntity fetEntity) {
        return Fet.builder()
                .entornAppId(fetEntity.getEntornAppId())
                .temps(Temps.builder().data(fetEntity.getTemps().getData()).build())
                .dimensionsJson(fetEntity.getDimensionsJson())
                .indicadorsJson(fetEntity.getIndicadorsJson())
                .build();
    }
    protected List<Fet> toFets(List<FetEntity> fetEntities) {
        return fetEntities.stream().
                map(this::toFet).
                collect(Collectors.toList());
    }

}
