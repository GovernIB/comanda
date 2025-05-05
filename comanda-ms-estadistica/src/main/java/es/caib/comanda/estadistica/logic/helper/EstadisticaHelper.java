package es.caib.comanda.estadistica.logic.helper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import es.caib.comanda.client.model.EntornApp;
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
import es.caib.comanda.ms.estadistica.model.Dimensio;
import es.caib.comanda.ms.estadistica.model.DimensioDesc;
import es.caib.comanda.ms.estadistica.model.EstadistiquesInfo;
import es.caib.comanda.ms.estadistica.model.Fet;
import es.caib.comanda.ms.estadistica.model.GenericDimensio;
import es.caib.comanda.ms.estadistica.model.GenericFet;
import es.caib.comanda.ms.estadistica.model.IndicadorDesc;
import es.caib.comanda.ms.estadistica.model.RegistreEstadistic;
import es.caib.comanda.ms.estadistica.model.RegistresEstadistics;
import es.caib.comanda.ms.estadistica.model.Temps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstadisticaHelper {

    private final DimensioRepository dimensioRepository;
    private final DimensioValorRepository dimensioValorRepository;
    private final IndicadorRepository indicadorRepository;
    private final TempsRepository tempsRepository;
    private final FetRepository fetRepository;

    @Transactional
    public void getEstadisticaInfo(EntornApp entornApp, String estadisticaInfoUrl, String estadisticaUrl) {
        log.debug("Consultant informació estadística de l'app {}, entorn {}",
                entornApp.getApp().getNom(),
                entornApp.getEntorn().getNom());
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(0, getConverter());

        try {
            EstadistiquesInfo estadistiquesInfo = restTemplate.getForObject(estadisticaInfoUrl, EstadistiquesInfo.class);
            crearIndicadorsIDimensions(estadistiquesInfo, entornApp.getId());
            RegistresEstadistics registresEstadistics = restTemplate.getForObject(estadisticaUrl, RegistresEstadistics.class);
            crearEstadistiques(registresEstadistics, entornApp.getId());
        } catch (RestClientException ex) {
            log.warn("No s'ha pogut obtenir informació estadistica de l'app {}, entorn {}: {}",
                    entornApp.getApp().getId(),
                    entornApp.getEntorn().getId(),
                    ex.getLocalizedMessage());
        }
    }

    private static MappingJackson2HttpMessageConverter getConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(Dimensio.class, GenericDimensio.class);
        module.addAbstractTypeMapping(Fet.class, GenericFet.class);
        objectMapper.registerModule(module);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        return converter;
    }

    private void crearIndicadorsIDimensions(EstadistiquesInfo estadistiquesInfo, Long entornAppId) {
        crearIndicadors(estadistiquesInfo.getIndicadors(), entornAppId);
        crearDimensions(estadistiquesInfo.getDimensions(), entornAppId);
    }


    private void crearIndicadors(List<IndicadorDesc> indicadors, Long entornAppId) {
        if (indicadors != null) {
            indicadors.forEach(i -> {
                if (Strings.isBlank(i.getNom()))
                    return;
                
                IndicadorEntity indicador = indicadorRepository.findByNomAndEntornAppId(i.getNom(), entornAppId)
                        .orElse(new IndicadorEntity());

                indicador.setEntornAppId(entornAppId);
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

    private void crearDimensions(List<DimensioDesc> dimensions, Long entornAppId) {
        if (dimensions != null) {
            dimensions.forEach(d -> {
                if (Strings.isBlank(d.getNom()))
                    return;
                
                DimensioEntity dimensio = dimensioRepository.findByNomAndEntornAppId(d.getNom(), entornAppId)
                        .orElse(new DimensioEntity());

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


    private void crearEstadistiques(RegistresEstadistics registresEstadistics, Long entornAppId) {
        TempsEntity temps = crearTemps(registresEstadistics.getTemps());
        crearFets(registresEstadistics.getFets(), temps, entornAppId);
    }

    private TempsEntity crearTemps(Temps temps) {
        if (temps == null)
            return null;

        LocalDate data = LocalDate.from(temps.getData().toInstant().atZone(ZoneId.systemDefault()));
        TempsEntity tempsEntity = tempsRepository.findByData(data).orElse(tempsRepository.save(new TempsEntity(data)));
        return tempsEntity;
    }

//    private void crearFets(List<RegistreEstadistic> registresEstadistics, TempsEntity temps, Long entornAppId) {
//        if (registresEstadistics == null)
//            return;
//
//        List<IndicadorEntity> indicadors = indicadorRepository.findByEntornAppId(entornAppId);
//        fetRepository.deleteAllByTempsAndIndicator_EntornAppId(temps, entornAppId);
//        fetRepository.flush();
//
//        System.out.println("> Crear fets: " + (registresEstadistics != null ? registresEstadistics.size() : 0) + " registres)");
//
//        AtomicInteger indexReg = new AtomicInteger(1);
//        registresEstadistics.forEach(re -> {
//            System.out.println(">>> Registre estadístic: " + indexReg.getAndIncrement() + "(" + (re.getFets() != null ? re.getFets().size() : 0) + " fets)");
//            if (re.getFets() == null || re.getDimensions() == null)
//                return;
//
//            AtomicInteger indexFet = new AtomicInteger(1);
//            Set<DimensioValorEntity> dimensionsValor = getDimensionsValor(re.getDimensions(), entornAppId);
//            re.getFets().forEach(f -> {
//                System.out.println(">>>>> Fet estadístic: " + indexFet.getAndIncrement());
//                FetEntity fet = new FetEntity();
//                fet.setTemps(temps);
//                fet.setIndicator(getIndicador(indicadors, f.getNom()));
//                fet.setDimensioValors(dimensionsValor);
//                fet.setValor(f.getValor());
//                fetRepository.save(fet);
//            });
//            fetRepository.flush();
//        });
//    }

    private void crearFets(List<RegistreEstadistic> registresEstadistics, TempsEntity temps, Long entornAppId) {
        if (registresEstadistics == null || registresEstadistics.isEmpty()) {
            return;
        }

        // Carregar dades necessàries en memòria
        List<IndicadorEntity> indicadors = indicadorRepository.findByEntornAppId(entornAppId);
        Map<String, IndicadorEntity> indicadorsMap = indicadors.stream()
                .collect(Collectors.toMap(IndicadorEntity::getNom, i -> i));

        // Esborrar registres anteriors
        fetRepository.deleteAllByTempsAndIndicator_EntornAppId(temps, entornAppId);

        System.out.println("> Crear fets: " + (registresEstadistics != null ? registresEstadistics.size() : 0) + " registres)");

        // Preparar llista per batch insert
        List<FetEntity> fetsPerGuardar = new ArrayList<>();
        int batchSize = 500; // Ajusta segons necessitats

        AtomicInteger indexReg = new AtomicInteger(1);

        for (RegistreEstadistic re : registresEstadistics) {
            System.out.println(">>> Registre estadístic: " + indexReg.getAndIncrement() + "(" + (re.getFets() != null ? re.getFets().size() : 0) + " fets)");
            if (re.getFets() == null || re.getDimensions() == null) {
                continue;
            }

            AtomicInteger indexFet = new AtomicInteger(1);

            Set<DimensioValorEntity> dimensionsValor = getDimensionsValor(re.getDimensions(), entornAppId);

            for (Fet f : re.getFets()) {
                System.out.println(">>>>> Fet estadístic: " + indexFet.getAndIncrement());
                FetEntity fet = new FetEntity();
                fet.setTemps(temps);
                fet.setIndicator(indicadorsMap.get(f.getNom()));
                fet.setDimensioValors(dimensionsValor);
                fet.setValor(f.getValor());
                fetsPerGuardar.add(fet);

                // Quan arribem al batch size, guardem el lot
                if (fetsPerGuardar.size() >= batchSize) {
                    fetRepository.saveAll(fetsPerGuardar);
                    fetsPerGuardar.clear();
                }
            }
        }

        // Guardar els fets restants
        if (!fetsPerGuardar.isEmpty()) {
            fetRepository.saveAll(fetsPerGuardar);
        }
    }


//    private Set<DimensioValorEntity> getDimensionsValor(List<Dimensio> dimensions, Long entornAppId) {
//        Set<DimensioValorEntity> dimensionsValor = new HashSet<>();
//
//        dimensions.forEach(d -> {
//            DimensioEntity dimensio = dimensioRepository.findByNomAndEntornAppId(d.getNom(), entornAppId).orElseThrow();
//            DimensioValorEntity dimensioValor = dimensioValorRepository.findByDimensioAndValor(dimensio, ("".equals(d.getValor()) ? null : d.getValor())).orElseThrow();
//            dimensionsValor.add(dimensioValor);
//        });
//
//        return dimensionsValor;
//    }

    private Map<String, Map<String, DimensioValorEntity>> dimensioValorCache;

    private Set<DimensioValorEntity> getDimensionsValor(List<Dimensio> dimensions, Long entornAppId) {
        // Inicialitzar cache si és necessari
        if (dimensioValorCache == null) {
            dimensioValorCache = new HashMap<>();
            List<DimensioEntity> dimensioList = dimensioRepository.findByEntornAppId(entornAppId);
            for (DimensioEntity dimensio : dimensioList) {
                Map<String, DimensioValorEntity> valorMap = dimensioValorRepository
                        .findByDimensio(dimensio)
                        .stream()
                        .collect(Collectors.toMap(
                                DimensioValorEntity::getValor,
                                d -> d,
                                (v1, v2) -> v1
                        ));
                dimensioValorCache.put(dimensio.getNom(), valorMap);
            }
        }

        Set<DimensioValorEntity> dimensionsValor = new HashSet<>();
        for (Dimensio d : dimensions) {
            String valor = "".equals(d.getValor()) ? null : d.getValor();
            DimensioValorEntity dimensioValor = dimensioValorCache
                    .getOrDefault(d.getNom(), Collections.emptyMap())
                    .get(valor);
            if (dimensioValor != null) {
                dimensionsValor.add(dimensioValor);
            }
        }
        return dimensionsValor;
    }


    private IndicadorEntity getIndicador(List<IndicadorEntity> indicadors, String nom) {
        return indicadors.stream()
                .filter(i -> i.getNom().equals(nom))
                .findFirst()
                .orElseThrow();
    }

}
