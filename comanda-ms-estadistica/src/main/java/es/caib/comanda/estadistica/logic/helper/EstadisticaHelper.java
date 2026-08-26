package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPluginDir3;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.EntitatValorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet.FetObtenirResponse;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.*;
import es.caib.comanda.estadistica.persist.repository.*;
import es.caib.comanda.model.v1.estadistica.*;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    private final EntitatRepository entitatRepository;
    @Value("${" + BaseConfig.PROP_STATS_AUTH_USER + ":}")
    private String statsAuthUser;
    @Value("${" + BaseConfig.PROP_STATS_AUTH_PASSWORD + ":}")
    private String statsAuthPassword;

    @Lazy
    private final EstadisticaHelper self = this;

    private final DimensioRepository dimensioRepository;
    private final DimensioValorRepository dimensioValorRepository;
    private final IndicadorRepository indicadorRepository;
    private final TempsRepository tempsRepository;
    private final FetRepository fetRepository;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final RestTemplate restTemplate;
    private final Environment environment;
    private final UnitatsOrganitzativesPluginDir3 unitatsOrganitzativesPluginDir3;
    private final EntitatResolverHelper entitatResolverHelper;
    private final UnitatOrganitzativaHelper unitatOrganitzativaHelper;

    private static final ConcurrentHashMap<Long, Object> LOCKS = new ConcurrentHashMap<>();

    // OBTENCIÓ i DESAT D'ESTADISTIQUES
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Obté i processa informació estadística d'una aplicació a partir dels URLs proporcionades.
     * La informació inclou indicadors, dimensions i registres estadístics que es creen i es guarden en el sistema a partir de les dades rebudes.
     *
     * @param entornApp Objecte que representa l'aplicació i l'entorn per als quals es recupera informació estadística.
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
    public FetObtenirResponse getEstadisticaInfoDadesAmbUrl(EntornApp entornApp,
                                                            String estadisticaUrl,
                                                            boolean multiplesDies) {
        log.debug("Obtenint informació i dades estadístiques de l'app {}, entorn {} amb URL específica: {}",
            entornApp.getApp().getNom(),
            entornApp.getEntorn().getNom(),
            estadisticaUrl);

        MonitorEstadistica monitorEstadistica = initializeMonitor(entornApp, estadisticaUrl);

        try {
            processEstadisticaInfo(entornApp, restTemplate, monitorEstadistica);
            return processEstadisticaDades(entornApp, estadisticaUrl, restTemplate, monitorEstadistica, multiplesDies);
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
    private void processEstadisticaInfo(EntornApp entornApp,
                                        RestTemplate restTemplate,
                                        MonitorEstadistica monitorEstadistica) throws RestClientException {
        Object lock = LOCKS.computeIfAbsent(entornApp.getId(), k -> new Object());
        synchronized (lock) {
            monitorEstadistica.startInfoAction();
            HttpEntity<Void> httpEntity = buildAuthEntityIfNeeded(entornApp);
            EstadistiquesInfo estadistiquesInfo;
            if (httpEntity != null) {
                estadistiquesInfo = restTemplate.exchange(
                    entornApp.getEstadisticaInfoUrl(),
                    org.springframework.http.HttpMethod.GET,
                    httpEntity,
                    EstadistiquesInfo.class).getBody();
            } else {
                estadistiquesInfo = restTemplate.getForObject(entornApp.getEstadisticaInfoUrl(), EstadistiquesInfo.class);
            }
            monitorEstadistica.endInfoAction();
            // Guardar la inforció de l'estructura de les dades estadístiques
            crearIndicadorsIDimensions(estadistiquesInfo, entornApp.getId());
        }
    }

    // Obtenir les dades estadístiques
    private FetObtenirResponse processEstadisticaDades(EntornApp entornApp,
                                                       String estadisticaUrl,
                                                       RestTemplate restTemplate,
                                                       MonitorEstadistica monitorEstadistica,
                                                       boolean multiplesDies) throws RestClientException {
        Object lock = LOCKS.computeIfAbsent(entornApp.getId(), k -> new Object());
        Map<String, Boolean> diesAmbDates = new HashMap<>();
        Map<String, String> diesAmbErrors = new HashMap<>();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        synchronized (lock) {
            monitorEstadistica.startDadesAction();
            HttpEntity<Void> httpEntity = buildAuthEntityIfNeeded(entornApp);
            if (multiplesDies) {
                List<RegistresEstadistics> registresEstadistics = restTemplate.exchange(
                    estadisticaUrl,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<List<RegistresEstadistics>>() {
                    }).getBody();
                monitorEstadistica.endDadesAction();
                // Guardar les dades estadístiques
                registresEstadistics.forEach(r -> {
                    String savedBD = crearEstadistiques(r, entornApp.getId());
                    if (Objects.isNull(savedBD)) {
                        diesAmbDates.put(r.getTemps().format(formatter), getRegistreEstadisticMessage(r));
                    } else {
                        diesAmbErrors.put(r.getTemps().format(formatter), savedBD);
                    }
                });
            } else {
                RegistresEstadistics registresEstadistics;
                if (httpEntity != null) {
                    registresEstadistics = restTemplate.exchange(
                        estadisticaUrl,
                        HttpMethod.GET,
                        httpEntity,
                        RegistresEstadistics.class).getBody();
                } else {
                    registresEstadistics = restTemplate.getForObject(estadisticaUrl, RegistresEstadistics.class);
                }
                monitorEstadistica.endDadesAction();
                // Guardar les dades estadístiques
                String savedBD = crearEstadistiques(registresEstadistics, entornApp.getId());
                if (Objects.isNull(savedBD)) {
                    diesAmbDates.put(registresEstadistics.getTemps().format(formatter), getRegistreEstadisticMessage(registresEstadistics));
                } else {
                    diesAmbErrors.put(registresEstadistics.getTemps().format(formatter), savedBD);
                }
            }
        }
        FetObtenirResponse result = new FetObtenirResponse();
        result.setDiesAmbDades(diesAmbDates);
        result.setDiesAmbErrors(diesAmbErrors);
        result.setSuccess(diesAmbErrors.isEmpty());
        if (Boolean.FALSE.equals(result.getSuccess())) {
            result.setMessage(I18nUtil.getInstance().getI18nMessage(
                "es.caib.comanda.estadistica.logic.helper.EstadisticaHelper.processEstadisticaDades.dies.amb.errors.message",
                diesAmbErrors.size()));
        }
        return result;
    }

    /**
     * Construeix HttpEntity amb Basic Auth. Lògica de {@code AuthHeaderUtil} (microservei configuracio)
     **/
    private HttpEntity<Void> buildAuthEntityIfNeeded(EntornApp entornApp) {
        if (!entornApp.isEstadisticaAuth()) {
            return null;
        }
        String nomUsuari = buildValorStatsAuth(statsAuthUser, entornApp.getNomUsuariAuth(), entornApp.isParametreAuth());
        if (nomUsuari == null || nomUsuari.isBlank()) {
            return null;
        }
        String contrasenyaUsuari = Optional.ofNullable(buildValorStatsAuth(statsAuthPassword, entornApp.getContrasenyaAuth(), entornApp.isParametreAuth())).orElse("");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", basicAuthHeader(nomUsuari, contrasenyaUsuari));
        return new HttpEntity<>(headers);
    }

    private String buildValorStatsAuth(String valorStatic, String valorEntornApp, boolean parametreAuth) {
        if (valorEntornApp != null) {
            if (!parametreAuth) {
                return valorEntornApp;
            }
            return environment.getProperty(valorEntornApp);
        }
        return valorStatic;
    }

    private String basicAuthHeader(String user, String password) {
        String token = java.util.Base64.getEncoder().encodeToString((user + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private Boolean getRegistreEstadisticMessage(RegistresEstadistics registresEstadistics) {
        return !(registresEstadistics.getFets() == null || registresEstadistics.getFets().isEmpty());
    }

    private void handleEstadisticaException(EntornApp entornApp,
                                            MonitorEstadistica monitorEstadistica,
                                            RestClientException ex) {
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
     * @param entornAppId       Identificador de l'entorn d'aplicació amb el qual s'associaran
     *                          els indicadors i dimensions.
     */
    private void crearIndicadorsIDimensions(EstadistiquesInfo estadistiquesInfo, Long entornAppId) {
        crearIndicadors(estadistiquesInfo.getIndicadors(), entornAppId);
        crearDimensions(estadistiquesInfo.getDimensions(), entornAppId);
        crearEntitats(estadistiquesInfo.getEntitats(), entornAppId);
    }

    private void crearEntitats(List<EntitatDesc> entitats, Long entornAppId) {
        if (entitats == null || entitats.isEmpty())
            return;

        entitats.forEach(e -> {
            try {
                crearEntitat(e, entornAppId);
            } catch (Exception ex) {
                log.warn("No s'ha pogut crear/actualitzar l'Entitat rebuda a la informació estadística (codi={}, codiDir3={}): {}",
                    e.getCodi(), e.getCodiDir3(), ex.getMessage());
            }
        });
    }

    private void crearEntitat(EntitatDesc e, Long entornAppId) {
        EntitatEntity entitat = trobarEntitatExistent(e).orElseGet(EntitatEntity::new);
        boolean afegeixCodiDir3 = Strings.isBlank(entitat.getCodiDir3()) && !Strings.isBlank(e.getCodiDir3());

        if (!Strings.isBlank(e.getCodiDir3())) {
            entitat.setCodiDir3(e.getCodiDir3());
        }
        if (!Strings.isBlank(e.getNom())) {
            entitat.setNom(e.getNom());
        }
        if (!Strings.isBlank(e.getCif())) {
            entitat.setCif(e.getCif());
        }
        if (entitat.getCodi() == null && !Strings.isBlank(e.getCodi())) {
            entitat.setCodi(e.getCodi());
        }
        EntitatEntity saved = entitatRepository.save(entitat);

        if (afegeixCodiDir3) {
            unitatOrganitzativaHelper.refreshFromEntitatCodiDir3(saved.getCodiDir3());
        }

        lligarDimensioEntitat(e, entornAppId, saved);
    }

    /**
     * Cerca una Entitat ja existent que correspongui a la mateixa entitat real descrita per {@code e}, mirant
     * tots els seus camps identificadors coneguts (no només codiDir3). Cal fer-ho així perquè {@code crearDimensions}
     * ja pot haver creat abans un "esquelet" d'Entitat amb un únic camp (segons entitatValorTipus, vegeu
     * {@link EntitatResolverHelper#resolveOrCreateEntitat}) per al mateix valor - si només es cerqués per codiDir3,
     * es crearia una fila duplicada per a la mateixa entitat.
     */
    private Optional<EntitatEntity> trobarEntitatExistent(EntitatDesc e) {
        if (!Strings.isBlank(e.getCodiDir3())) {
            Optional<EntitatEntity> trobada = entitatRepository.findByCodiDir3(e.getCodiDir3());
            if (trobada.isPresent()) return trobada;
        }
        if (!Strings.isBlank(e.getCodi())) {
            Optional<EntitatEntity> trobada = entitatRepository.findByCodi(e.getCodi());
            if (trobada.isPresent()) return trobada;
        }
        if (!Strings.isBlank(e.getCif())) {
            Optional<EntitatEntity> trobada = entitatRepository.findByCif(e.getCif());
            if (trobada.isPresent()) return trobada;
        }
        if (!Strings.isBlank(e.getNom())) {
            return entitatRepository.findFirstByNom(e.getNom());
        }
        return Optional.empty();
    }

    private void lligarDimensioEntitat(EntitatDesc e, Long entornAppId, EntitatEntity entitat) {
        // Enllaçar entitats amb dimensió tipus entitat
        DimensioEntity dimensioEntitat = dimensioRepository.findByEntornAppIdAndTipus(entornAppId, TipusDimensioEnum.ENTITAT).orElse(null);
        if (dimensioEntitat == null || dimensioEntitat.getValors() == null)
            return;

        EntitatValorTipus tipus = dimensioEntitat.getEntitatValorTipus();
        if (tipus == null) {
            tipus = EntitatValorTipus.CODI;
        }
        String valorEsperat;
        switch (tipus) {
            case CODI_DIR3:
                valorEsperat = e.getCodiDir3();
                break;
            case CIF:
                valorEsperat = e.getCif();
                break;
            case NOM:
                valorEsperat = e.getNom();
                break;
            case MANUAL:
                // Sense automatisme: el mapeig d'aquests valors s'ha de fer manualment.
                return;
            case CODI:
            default:
                valorEsperat = e.getCodi();
                break;
        }
        if (valorEsperat == null) {
            return;
        }
        for (DimensioValorEntity v : dimensioEntitat.getValors()) {
            if (valorEsperat.equals(v.getValor())) {
                v.setEntitatMapejada(entitat);
            }
        }
    }


    /**
     * Crea i actualitza els indicadors associats a un entorn d'aplicació especificat.
     * Si un indicador amb el mateix nom ja existeix per a l'entorn, s'actualitzen les seves propietats. Si no existeix, se'n crea un de nou.
     *
     * @param indicadors  Llista d'objectes IndicadorDesc que contenen la informació dels indicadors a crear o actualitzar.
     * @param entornAppId Identificador de l'entorn d'aplicació amb el qual s'associen els indicadors.
     */
    private void crearIndicadors(List<IndicadorDesc> indicadors, Long entornAppId) {
        if (indicadors != null) {
            indicadors.forEach(i -> {
                if (Strings.isBlank(i.getNom()))
                    return;

                IndicadorEntity indicador = indicadorRepository.findByCodiAndEntornAppId(i.getCodi(), entornAppId)
                    .orElseGet(() -> new IndicadorEntity());

                // Un indicador de tipus FORMULA es gestiona només des de la pantalla d'Indicadors: si una app
                // publica per error un codi que hi coincideix, no se n'ha de sobreescriure cap camp.
                if (indicador.getTipus() == IndicadorTipus.FORMULA) {
                    log.warn("S'ha ignorat la publicació de l'indicador amb codi={} i entornAppId={} perquè ja existeix com a indicador de fórmula", i.getCodi(), entornAppId);
                    return;
                }

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
     * @param dimensions  Llista d'objectes DimensioDesc que contenen la informació de les dimensions i els seus valors associats.
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

            if (TipusDimensioEnum.ORGAN_GESTOR.equals(dimensioSaved.getTipus())) {
                if (dimensions.stream().noneMatch(dim -> Objects.equals(dim.getCodi(), "CONS"))) {
                    try {
                        DimensioDesc dDesc = new DimensioDesc();
                        dDesc.setCodi("CONS");
                        dDesc.setNom("Conselleria");
                        dDesc.setValors(dimensioSaved.getValors().stream()
                            .map(DimensioValorEntity::getValor)
                            .map(unitatsOrganitzativesPluginDir3::getConselleria)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList())
                        );

                        DimensioEntity dEntity = dimensioRepository.findByCodiAndEntornAppId(dDesc.getCodi(), entornAppId)
                            .orElseGet(DimensioEntity::new);
                        dEntity.setCodi(dDesc.getCodi());
                        dEntity.setNom(dDesc.getNom());
                        dEntity.setEntornAppId(entornAppId);
                        dEntity.setTipus(TipusDimensioEnum.CONSELLERIA);
                        dimensioRepository.save(dEntity);

                        this.crearDimensions(List.of(dDesc), entornAppId);
                    } catch (Exception ignore) {
                    }
                }
            }

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
        List<String> missingValues = values.stream()
            .filter(v -> !existingValues.contains(v))
            .collect(Collectors.toList());

        List<DimensioValorEntity> newValues = missingValues.stream()
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

        if (TipusDimensioEnum.ENTITAT.equals(dimensio.getTipus())) {
            for (String valor : missingValues) {
                if (Strings.isBlank(valor)) continue;
                try {
                    entitatResolverHelper.resolveOrCreateEntitat(dimensio, valor);
                } catch (Exception e) {
                    log.warn("No s'ha pogut crear/resoldre automàticament l'Entitat pel valor '{}' de la dimensió {}: {}",
                        valor, dimensio.getCodi(), e.getMessage());
                }
            }
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
     * @param entornAppId          Identificador de l'entorn d'aplicació amb el qual s'associen les estadístiques creades.
     */
    private String crearEstadistiques(RegistresEstadistics registresEstadistics, Long entornAppId) {
        try {
            TempsEntity temps = crearTemps(registresEstadistics.getTemps());
            crearFets(registresEstadistics.getFets(), temps, entornAppId);
            return null;
        } catch (Exception ex) {
            log.error("Error al crear estadistiques", ex);
            return Objects.nonNull(ex.getMessage()) ? ex.getMessage() : "";
        }
    }

    /**
     * Crea o recupera una entitat de tipus TempsEntity a partir de la informació proporcionada en un objecte Temps.
     * Si ja existeix una entitat TempsEntity amb la mateixa data, es recupera; en cas contrari, se'n crea una de nova.
     *
     * @param temps l'objecte Temps que conté la informació temporal necessària per crear o recuperar un TempsEntity.
     * @return l'entitat TempsEntity corresponent a la data proporcionada, o null si l'objecte Temps és null.
     */
    private static final ConcurrentHashMap<LocalDate, Object> TIME_LOCKS = new ConcurrentHashMap<>();

    private TempsEntity crearTemps(OffsetDateTime temps) {
        if (temps == null)
            return null;

        LocalDate data = LocalDate.from(temps.toInstant().atZone(ZoneId.systemDefault()));
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
     * @param temps                Entitat que representa el moment temporal associat als fets que es crearan.
     * @param entornAppId          Identificador de l'entorn d'aplicació amb el qual s'associen els registres de "Fets" creats.
     */
    private void crearFets(List<RegistreEstadistic> registresEstadistics, TempsEntity temps, Long entornAppId) {
        if (registresEstadistics == null || registresEstadistics.isEmpty()) {
            return;
        }

        // Esborrar registres anteriors
        fetRepository.deleteAllByTempsAndEntornAppId(temps, entornAppId);

        log.debug("> Crear fets: {} registres)", registresEstadistics.size());

        // Preparar llista per batch insert
        List<FetEntity> fetsPerGuardar = new ArrayList<>();
        int batchSize = 500; // Ajusta segons necessitats

        int indexReg = 1;

        for (RegistreEstadistic re : registresEstadistics) {
            log.debug(">>> Registre estadístic: {}({} fets)", indexReg++, re.getFets() != null ? re.getFets().size() : 0);
            if (re.getFets() == null || re.getDimensions() == null) {
                continue;
            }

            // Crear mapa complet de dimensions (sense enriquir encara amb CONS)
            Map<String, String> dimensionsMap = new HashMap<>();
            for (Dimensio d : re.getDimensions()) {
                dimensionsMap.put(d.getCodi(), d.getValor());
            }

            // Enriquir amb la conselleria (CONS) de l'òrgan gestor, ara que ja tenim totes les dimensions del fet
            // (incloent-hi el valor de la dimensió ENTITAT si n'hi ha - independentment de l'ordre en què arribin).
            for (Dimensio d : re.getDimensions()) {
                DimensioEntity dimensioEntity = dimensioRepository.findByCodiAndEntornAppId(d.getCodi(), entornAppId).orElse(null);
                if (dimensioEntity != null && TipusDimensioEnum.ORGAN_GESTOR.equals(dimensioEntity.getTipus())) {
                    String conselleria = entitatResolverHelper.resolveConselleria(entornAppId, d.getValor(), dimensionsMap);
                    if (conselleria != null) {
                        dimensionsMap.put("CONS", conselleria);
                    }
                }
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
