package es.caib.comanda.estadistica.logic.helper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.Fet;
import es.caib.comanda.estadistica.logic.intf.model.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.Temps;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.persist.entity.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.FetEntity;
import es.caib.comanda.estadistica.persist.entity.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.TempsEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.TempsRepository;
import es.caib.comanda.ms.estadistica.model.DimensioDesc;
import es.caib.comanda.ms.estadistica.model.EstadistiquesInfo;
import es.caib.comanda.ms.estadistica.model.GenericDimensio;
import es.caib.comanda.ms.estadistica.model.GenericFet;
import es.caib.comanda.ms.estadistica.model.IndicadorDesc;
import es.caib.comanda.ms.estadistica.model.RegistreEstadistic;
import es.caib.comanda.ms.estadistica.model.RegistresEstadistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final DimensioRepository dimensioRepository;
    private final DimensioValorRepository dimensioValorRepository;
    private final IndicadorRepository indicadorRepository;
    private final TempsRepository tempsRepository;
    private final FetRepository fetRepository;

    private final EstadisticaClientHelper estadisticaClientHelper;

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
        RestTemplate restTemplate = initializeRestTemplate();
        String estadisticaUrl = buildEstadisticaUrl(entornApp, dies);

        MonitorEstadistica monitorEstadistica = initializeMonitor(entornApp, estadisticaUrl);

        try {
            processEstadisticaInfo(entornApp, restTemplate, monitorEstadistica);
            processEstadisticaDades(entornApp, estadisticaUrl, restTemplate, monitorEstadistica, dies != null);
        } catch (RestClientException ex) {
            handleEstadisticaException(entornApp, monitorEstadistica, ex);
        }
    }

    private RestTemplate initializeRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(0, getConverter());
        return restTemplate;
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
        monitorEstadistica.startInfoAction();
        EstadistiquesInfo estadistiquesInfo = restTemplate.getForObject(entornApp.getEstadisticaInfoUrl(), EstadistiquesInfo.class);
        monitorEstadistica.endInfoAction();
        // Guardar la inforció de l'estructura de les dades estadístiques
        crearIndicadorsIDimensions(estadistiquesInfo, entornApp.getId());
    }

    // Obtenir les dades estadístiques
    private void processEstadisticaDades(EntornApp entornApp, String estadisticaUrl, RestTemplate restTemplate, MonitorEstadistica monitorEstadistica, boolean multiplesDies) throws RestClientException {
        monitorEstadistica.startDadesAction();
        if (multiplesDies) {
            List<RegistresEstadistics> registresEstadistics = restTemplate.exchange(
                    estadisticaUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RegistresEstadistics>>() {}).getBody();
            monitorEstadistica.endDadesAction();
            // Guardar les dades estadístiques
            registresEstadistics.forEach(r -> crearEstadistiques(r, entornApp.getId()));
        } else {
            RegistresEstadistics registresEstadistics = restTemplate.getForObject(estadisticaUrl, RegistresEstadistics.class);
            monitorEstadistica.endDadesAction();
            // Guardar les dades estadístiques
            crearEstadistiques(registresEstadistics, entornApp.getId());
        }
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
     * Configura i retorna un {@link MappingJackson2HttpMessageConverter} personalitzat amb un {@link ObjectMapper}
     * per gestionar la serialització i deserialització de dades JSON. El mapejador afegeix configuracions específiques com la desactivació
     * d'errors en subtipus i en objectes buits, i registra un mòdul que mapeja tipus abstractes a implementacions concretes.
     *
     * @return una instància de {@link MappingJackson2HttpMessageConverter} configurada amb ajustos personalitzats.
     */
    private static MappingJackson2HttpMessageConverter getConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(es.caib.comanda.ms.estadistica.model.Dimensio.class, GenericDimensio.class);
        module.addAbstractTypeMapping(es.caib.comanda.ms.estadistica.model.Fet.class, GenericFet.class);
        objectMapper.registerModule(module);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        return converter;
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
        if (dimensions != null) {
            dimensions.forEach(d -> {
                if (Strings.isBlank(d.getNom()))
                    return;

                DimensioEntity dimensio = dimensioRepository.findByCodiAndEntornAppId(d.getCodi(), entornAppId)
                        .orElseGet(() -> new DimensioEntity());

                dimensio.setCodi(d.getCodi());
                dimensio.setNom(d.getNom());
                dimensio.setEntornAppId(entornAppId);
                if (!Strings.isBlank(d.getDescripcio())) {
                    dimensio.setDescripcio(d.getDescripcio());
                }
                DimensioEntity dimensioSaved = dimensioRepository.save(dimensio);

                if (d.getValors() != null) {
                    d.getValors().forEach(v -> {
                        if (!dimensioValorRepository.findByDimensioAndValor(dimensioSaved, ("".equals(v) ? null : v)).isPresent()) {
                            DimensioValorEntity valor = new DimensioValorEntity();
                            valor.setDimensio(dimensioSaved);
                            valor.setValor(v);
                            dimensioValorRepository.save(valor);
                        }
                    });
                }
            });
        }
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
    private TempsEntity crearTemps(es.caib.comanda.ms.estadistica.model.Temps temps) {
        if (temps == null)
            return null;

        LocalDate data = LocalDate.from(temps.getData().toInstant().atZone(ZoneId.systemDefault()));
        TempsEntity tempsEntity = tempsRepository.findByData(data);
        if (tempsEntity == null)
            tempsEntity = tempsRepository.save(new TempsEntity(data));
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
            for (es.caib.comanda.ms.estadistica.model.Dimensio d : re.getDimensions()) {
                dimensionsMap.put(d.getCodi(), d.getValor());
            }

            // Crear mapa per indicadors
            Map<String, Double> indicadorsValuesMap = new HashMap<>();
            for (es.caib.comanda.ms.estadistica.model.Fet f : re.getFets()) {
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


    // CONSULTA ESTADISTIQUES
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    /**
     * Recupera una llista de fets estadístics corresponents a un entorn d'aplicació específic i a un període de dates especificat.
     * Els fets estadístics inclouen informació agregada relativa a dimensions, indicadors i temps associats.
     *
     * @param entornAppId Identificador de l'entorn d'aplicació pel qual es volen recuperar els fets estadístics.
     * @param dataInici Data d'inici del període pel qual es volen recuperar els fets estadístics.
     * @param dataFi Data de finalització del període pel qual es volen recuperar els fets estadístics.
     * @return Una llista d'objectes {@link Fet} que representen els fets estadístics associats al període i entorn especificats.
     */
    @Transactional(readOnly = true)
    public List<Fet> getEstadistiquesPeriode(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi) {

        // Get statistics directly from the database using JSON_VALUE and JSON_TABLE
        List<FetEntity> fets = fetRepository.findByEntornAppIdAndTempsDataBetween(
                entornAppId,
                dataInici,
                dataFi);
//                nivellAgrupacio.name());

        // Convert to DTOs
        return toFets(fets);
    }

    /**
     * Recupera una llista de fets estadístics corresponents a un entorn d'aplicació específic i a un període de dates especificat,
     * tenint en compte un conjunt de dimensions filtrades. Si no es proporcionen filtres de dimensions, es fa una crida a la funcionalitat
     * estàndard que no té en compte dimensions (mètode {@link #getEstadistiquesPeriode}).
     *
     * @param entornAppId Identificador de l'entorn d'aplicació pel qual es volen recuperar els fets estadístics.
     * @param dataInici Data d'inici del període pel qual es volen recuperar els fets estadístics.
     * @param dataFi Data de finalització del període pel qual es volen recuperar els fets estadístics.
     * @param dimensionsFiltre Un mapa que conté les dimensions a filtrar, on la clau és el nom de la dimensió i el valor és
     *                         una llista de valors que s'han de considerar per aquesta dimensió.
     * @return Una llista d'objectes {@link Fet} que representen els fets estadístics associats al període, l'entorn i
     *         les dimensions especificades en els filtres.
     */
    @Transactional(readOnly = true)
    public List<Fet> getEstadistiquesPeriodeAmbDimensions(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre) {

        // If no dimensions filter is provided, use the standard method
        if (dimensionsFiltre == null || dimensionsFiltre.isEmpty()) {
            return getEstadistiquesPeriode(entornAppId, dataInici, dataFi);
        }

        // Get statistics directly from the database using JSON_VALUE and JSON_TABLE
        List<FetEntity> fets = fetRepository.findByEntornAppIdAndTempsDataBetweenAndDimensions(
                entornAppId,
                dataInici,
                dataFi,
                dimensionsFiltre);
//                nivellAgrupacio.name());

        // Convert to DTOs
        return toFets(fets);
    }

    /**
     * Obté un valor agregat d'un indicador específic basat en l'entornAppId, un rang de dates específic, 
     * valors dimensionals i un tipus d'agregació.
     * Aquesta consulta aplica l'agregació directament a la base de dades, optimitzant el rendiment.
     *
     * @param entornAppId Identificador de l'entorn d'aplicació pel qual es vol fer la cerca.
     * @param dataInici Data inicial del rang de dates per a la cerca.
     * @param dataFi Data final del rang de dates per a la cerca.
     * @param dimensionsFiltre Un mapa que conté les dimensions a filtrar, on la clau és el nom de la dimensió i el valor és
     *                         una llista de valors que s'han de considerar per aquesta dimensió.
     * @param indicadorCodi El codi de l'indicador sobre el qual s'aplicarà l'agregació.
     * @param agregacio El tipus d'agregació a aplicar (COUNT, SUM, AVERAGE, etc.).
     * @return El valor agregat calculat directament a la base de dades.
     */
    @Transactional(readOnly = true)
    public Object getValorAgregatPeriodeAmbDimensions(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            String indicadorCodi,
            TableColumnsEnum agregacio,
            PeriodeUnitat unitatAgregacio) {

        return fetRepository.getAggregatedValue(
                entornAppId,
                dataInici,
                dataFi,
                dimensionsFiltre,
                indicadorCodi,
                agregacio,
                unitatAgregacio);
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
