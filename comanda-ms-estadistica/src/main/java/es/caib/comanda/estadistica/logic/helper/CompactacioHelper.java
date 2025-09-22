package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.CompactacioEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.FetTipusEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helper responsable d'executar el procés de compactació d'estadístiques.
 * <p>
 * Es divideix en dues fases principals:
 * 1) Compactació per valors de dimensions agrupables (valor -> valorAgrupacio).
 * 2) Compactació temporal (setmanal/mensual) i eliminació per retenció, en funció de la configuració.
 * <p>
 */
@Slf4j
@Component
public class CompactacioHelper {

    @PersistenceContext
    private EntityManager entityManager;

    private final DimensioValorRepository dimensioValorRepository;
    private final FetRepository fetRepository;
    private final IndicadorRepository indicadorRepository;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final EstadisticaHelper estadisticaHelper;

    private static final CompactacioEnum TIPUS_DEFECTE = CompactacioEnum.SUMA;


    public CompactacioHelper(DimensioValorRepository dimensioValorRepository,
                             FetRepository fetRepository,
                             IndicadorRepository indicadorRepository,
                             EstadisticaClientHelper estadisticaClientHelper,
                             EstadisticaHelper estadisticaHelper) {
        this.dimensioValorRepository = dimensioValorRepository;
        this.fetRepository = fetRepository;
        this.indicadorRepository = indicadorRepository;
        this.estadisticaClientHelper = estadisticaClientHelper;
        this.estadisticaHelper = estadisticaHelper;
    }


    @Transactional
    public void compactar(EntornApp entornApp) {
        // Punt d'entrada del procés de compactació per un entorn concret.
        log.info("[Compactacio] Inici del procés de compactació de fets per l'entornApp {}", entornApp.getId());

        MonitorEstadistica monitorEstadistica = initializeMonitor(entornApp);
        try {
            monitorEstadistica.startCompactarAction();
            // Compactació temporal i eliminació per retenció (pendent de configuració EntornApp)
            compactarTemporalIEsborraPerRetencio(entornApp);
            monitorEstadistica.endCompactarAction();
            log.info("[Compactacio] Procés de compactació finalitzat");
        } catch (Exception e) {
            log.error("[Compactacio] Error en el procés de compactació", e);
            monitorEstadistica.endCompactarAction(e);
        }
    }

    private MonitorEstadistica initializeMonitor(EntornApp entornApp) {
        return new MonitorEstadistica(
                entornApp.getId(),
                entornApp.getEstadisticaInfoUrl(),
                entornApp.getEstadisticaUrl(),
                estadisticaClientHelper);
    }

    /**
     * Compactació temporal (setmanal i mensual) i eliminació segons retenció configurada.
     *
     */
    protected void compactarTemporalIEsborraPerRetencio(EntornApp entornApp) {
        // Fase 2: Compacta dades antigues a períodes més grossos (setmana/mes) i elimina dades
        // segons política de retenció configurada a l'EntornApp. Tot plegat per reduir volum i millorar rendiment.
        log.info("[Compactacio] Fase 2 - Compactació temporal i esborrat per retenció: inici");
        try {
            if (entornApp.getCompactable() == null || !entornApp.getCompactable()) {
                log.info("[Compactacio] EntornApp {} no compactable.", entornApp.getId());
                return;
            }

            Long entornAppId = entornApp.getId();
            int totalFetsActualitzats = 0;
            int totalFetsEliminats = 0;

            // Calcular els llistars de eliminació i retenció
            var llindars = new LlindarsData(entornApp.getEliminacioMesos(), entornApp.getCompactacioMensualMesos());

            // a) Eliminació per retenció en batch
            // Calcular el llindar de retenció: tot el que sigui anterior a aquest mes menys 'eliminacioMesos'
            if (llindars.getLlindarEliminar() != null) {
                long eliminats = fetRepository.deleteByEntornAppIdAndTempsDataBefore(entornAppId, llindars.getLlindarEliminar());
                totalFetsEliminats += eliminats;
                log.info("[Compactacio] Entorn {} -> fets eliminats per retenció: {}", entornAppId, eliminats);
            }

            // Cache d'indicadors per a l'entorn
            var indicadorsPerEntorn = carregarIndicadorsPerEntornId(entornAppId);

            // b) Compactació mensual
            // Reagrupa les dades anteriors al llindar mensual a l'inici del seu mes, aggregant indicadors segons configuració.
            if (llindars.getLlindarMensual() != null) {
                var llistaMensual = fetRepository.findByEntornAppIdAndTempsDataBefore(entornAppId, llindars.getLlindarMensual())
                        .stream()
                        .filter(fet -> fet.getTipus() != FetTipusEnum.MENSUAL)
                        .collect(Collectors.toList());
                totalFetsActualitzats += compactarPerPeriode(llistaMensual, entornAppId, indicadorsPerEntorn, FetTipusEnum.MENSUAL);
            }

//            // Per ara no es realitza compactació setmanal. Només mensual !!!!!!!!!!
//            // c) Compactació setmanal
//            // Reagrupa les dades anteriors al llindar setmanal a l'inici de setmana (dilluns), evitant solapar amb la mensual.
//            LocalDate llindarSetmanal = entornApp.getCompactacioSetmanalMesos() != null && entornApp.getCompactacioSetmanalMesos() > 0 ? llindars.iniciMesActual.minusMonths(entornApp.getCompactacioSetmanalMesos()) : null;
//            if (llindarSetmanal != null) {
//                List<FetEntity> llistaSetmanal;
//                if (llindarMensual != null) {
//                    llistaSetmanal = fetRepository.findByEntornAppIdAndTempsDataBetween(entornAppId, llindarSetmanal, llindarMensual);
//                } else {
//                    llistaSetmanal = fetRepository.findByEntornAppIdAndTempsDataBefore(entornAppId, llindarSetmanal);
//                }
//                totalFetsActualitzats += compactarPerPeriode(llistaSetmanal, entornAppId, indicadorsPerEntorn, PeriodeTarget.SETMANAL);
//            }
            log.info("[Compactacio] Fase 2 finalitzada. Fets actualitzats/creats: {}, fets eliminats: {}", totalFetsActualitzats, totalFetsEliminats);
        } catch (Exception e) {
            log.warn("[Compactacio] Error durant la fase de compactació temporal/retenció", e);
        } finally {
            log.info("[Compactacio] Fase 2 - Compactació temporal i esborrat per retenció: fi");
        }
    }

    // Carrega els indicadors associats a un entorn (entornAppId) i els indexa en un mapa per codi per a accés eficient.
    // Clau del mapa: codi únic de l’indicador dins l’entorn. Valor: l’IndicadorEntity corresponent.
    // S’utilitza per accelerar compactacions i càlculs d’indicadors agregats sobre conjunts de fets.
    // Si no hi ha indicadors disponibles per a l’entorn, retorna un mapa buit. Pot registrar traça per a diagnòstic.
    private Map<String, IndicadorEntity> carregarIndicadorsPerEntornId(Long entornAppId) {
        // Carrega i indexa per codi la configuració d'indicadors per un entorn concret.
        var map = new HashMap<String, IndicadorEntity>();
        var llista = indicadorRepository.findByEntornAppId(entornAppId);
        for (var ind : llista) map.put(ind.getCodi(), ind);
        return map;
    }


    // Compacta una llista d'entitats de fet per un període determinat, agregant indicadors i eliminant registres redundants.
    // Aquesta operació crea nous registres agregats per períodes (p. ex., setmanals o mensuals), actualitza la informació
    // agregada, i elimina els registres inicials un cop consolidats.
    //
    // fets: llista d'entitats de fet que seran compactades per període
    // entornAppId: identificador de l'entorn aplicatiu al qual pertanyen els fets
    // indicadorsPerEntornMap: mapa de configuracions d'indicadors que defineixen com s'han de calcular els indicadors agregats
    // periodeTipus: el tipus de període d'agregació (setmanal, mensual, etc.)
    // Retorna el nombre total de registres de fet que han estat creats o actualitzats com a part del procés de compactació
    private int compactarPerPeriode(List<FetEntity> fets,
                                    Long entornAppId,
                                    Map<String, IndicadorEntity> indicadorsPerEntornMap,
                                    FetTipusEnum periodeTipus) {
        if (fets == null || fets.isEmpty()) return 0;

        var agrupats = agruparFetsPerPeriode(fets, entornAppId, periodeTipus);

        HashMap<LocalDate, TempsEntity> tempsEntityCache = new HashMap<>();
        List<Long> fetsEliminar = new ArrayList<>();
        int fetsActualitzats = 0;

        for (var fetsPerPeriode : agrupats.getFetsPerPeriode().entrySet()) {
            var data = fetsPerPeriode.getKey().getData();
            var dimensions = fetsPerPeriode.getKey().getDimensions();
            var llistaDeFets = fetsPerPeriode.getValue();

            Map<String, Double> indicadorsResult = calcularIndicadorsAgregats(llistaDeFets, indicadorsPerEntornMap);

            var desti = new FetEntity();
            if (!tempsEntityCache.containsKey(data)) {
                tempsEntityCache.put(data, estadisticaHelper.createOrGetTempsEntity(data));
            }
            desti.setTemps(tempsEntityCache.get(data));
            desti.setDimensionsJson(dimensions);
            desti.setIndicadorsJson(indicadorsResult);
            desti.setTipus(periodeTipus);
            desti.setNumDies(agrupats.comptarDiesAmbDades(data));
            desti.setEntornAppId(entornAppId);
            fetRepository.save(desti);
            fetsActualitzats++;

            for (var fet : llistaDeFets) fetsEliminar.add(fet.getId());
        }

        // Eliminar els fets compactats
        int batchSize = 750;
        for (int start = 0; start < fetsEliminar.size(); start += batchSize) {
            fetRepository.deleteAllByIdInBatch(fetsEliminar.subList(
                    start,
                    Math.min(start + batchSize, fetsEliminar.size())
            ));
        }
        int fetsEliminats = fetsEliminar.size();

        log.info("[Compactacio] Compactació {}: fets fusionats/actualitzats: {}, fets eliminats: {}", periodeTipus, fetsActualitzats, fetsEliminats);
        return fetsActualitzats;
    }

    // Nova extracció: agrupació per període (setmana/mes) per facilitar proves i reutilització.
    protected ResultatAgrupacio agruparFetsPerPeriode(List<FetEntity> fets,
                                                      Long entornAppId,
                                                      FetTipusEnum periodeTipus) {
        var resultat = new ResultatAgrupacio();
        if (fets == null || fets.isEmpty()) return resultat;

        for (var fet : fets) {
            var dims = new HashMap<>(fet.getDimensionsJson() == null ? Map.of() : fet.getDimensionsJson());
            var data = fet.getTemps().getData();
            LocalDate novaData;
            if (periodeTipus == FetTipusEnum.MENSUAL) {
                novaData = data.withDayOfMonth(1);
            } else {
                // Dilluns com a inici de setmana (ISO)
                DayOfWeek first = DayOfWeek.MONDAY;
                DayOfWeek dow = data.getDayOfWeek();
                int diff = dow.getValue() - first.getValue();
                novaData = data.minusDays(diff);
            }
            var clau = new ClauDimensio(entornAppId, novaData, Collections.unmodifiableMap(dims));
            resultat.fetsPerPeriode.computeIfAbsent(clau, k -> new ArrayList<>()).add(fet);

            // Registram els dies amb dades
            boolean[] dies = resultat.diesdePeriodeAmbFets.computeIfAbsent(novaData, k -> new boolean[periodeTipus == FetTipusEnum.MENSUAL ? data.lengthOfMonth() : 7]);
            int index = periodeTipus == FetTipusEnum.MENSUAL ? data.getDayOfMonth() - 1 : data.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            dies[index] = true;
        }
        return resultat;
    }

    // Calcula els nous valors d'indicadors per a un grup segons la configuració de compactació de cada indicador.
    // Suporta SUMA, MÀXIMA, MÍNIMA i MITJANA (amb comptador especial opcional).
    private Map<String, Double> calcularIndicadorsAgregats(List<FetEntity> fets, Map<String, IndicadorEntity> indicadorsPerCodi) {

        Map<String, Double> resultats = new HashMap<>();
        Map<String, Double> sumes = new HashMap<>();
        Map<String, Integer> comptatges = new HashMap<>();
        Map<String, Double> comptadorsPerMitjana = new HashMap<>();

        for (var fet : fets) {
            var indicadorsDelFet = fet.getIndicadorsJson();
            if (indicadorsDelFet == null) continue;

            for (var indicadorFet : indicadorsDelFet.entrySet()) {
                String codi = indicadorFet.getKey();
                Double valor = indicadorFet.getValue();
                if (valor == null) continue;

                var indicadorAgrupacio = indicadorsPerCodi.get(codi);
                var tipus = obtenirTipus(indicadorAgrupacio);

                acumulaSegonsTipus(
                        codi,
                        valor,
                        tipus,
                        resultats,
                        sumes,
                        comptatges,
                        comptadorsPerMitjana,
                        indicadorsDelFet,
                        indicadorAgrupacio
                );
            }
        }

        for (var e : sumes.entrySet()) {
            String codi = e.getKey();
            double suma = e.getValue();
            CompactacioEnum tipus = obtenirTipus(indicadorsPerCodi.get(codi));

            if (tipus == CompactacioEnum.SUMA) {
                resultats.put(codi, suma);
            } else if (tipus == CompactacioEnum.MITJANA) {
                resultats.put(codi, calculaValorFinalMitjana(codi, suma, comptadorsPerMitjana, comptatges));
            }
        }
        return resultats;
    }

    private void acumulaSegonsTipus(
            String codi,
            Double valor,
            CompactacioEnum tipus,
            Map<String, Double> resultats,
            Map<String, Double> sumes,
            Map<String, Integer> comptatges,
            Map<String, Double> comptadorsPerMitjana,
            Map<String, Double> indicadorsDelFet,
            IndicadorEntity indicadorAgrupacio) {

        switch (tipus) {
            case SUMA:
                sumes.merge(codi, valor, Double::sum);
                break;
            case MAXIMA:
                resultats.merge(codi, valor, java.lang.Math::max);
                break;
            case MINIMA:
                resultats.merge(codi, valor, java.lang.Math::min);
                break;
            case MITJANA:
                sumes.merge(codi, valor, Double::sum);
                comptatges.merge(codi, 1, Integer::sum);
                if (indicadorAgrupacio != null && indicadorAgrupacio.getIndicadorComptadorPerMitjana() != null) {
                    String comptadorMitjanaCodi = indicadorAgrupacio.getIndicadorComptadorPerMitjana().getCodi();
                    Double comptadorMitjanaValor = indicadorsDelFet.get(comptadorMitjanaCodi);
                    if (comptadorMitjanaValor != null) {
                        comptadorsPerMitjana.merge(codi, comptadorMitjanaValor, Double::sum);
                    }
                }
                break;
        }
    }

    private double calculaValorFinalMitjana(
            String codi,
            double suma,
            Map<String, Double> comptadorsPerMitjana,
            Map<String, Integer> comptatges) {

        Double comptadorPerMitjana = comptadorsPerMitjana.get(codi);
        if (comptadorPerMitjana != null && comptadorPerMitjana > 0) {
            return suma / comptadorPerMitjana;
        }
        int n = comptatges.getOrDefault(codi, 0);
        return n > 0 ? (suma / n) : 0d;
    }

    private CompactacioEnum obtenirTipus(IndicadorEntity indicador) {
        CompactacioEnum tipus = (indicador != null) ? indicador.getTipusCompactacio() : null;
        return (tipus != null) ? tipus : TIPUS_DEFECTE;
    }


    // Classes auxiliars
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Getter
    static class LlindarsData {
        // Agrupa valors de data rellevants per calcular llindars d'operacions de compactació/retenció.
        private final LocalDate avui;
        private final LocalDate iniciMesActual;
        private final LocalDate llindarEliminar;
        private final LocalDate llindarMensual;

        public LlindarsData(Integer eliminacioMesos, Integer compactacioMesos) {
            this.avui = LocalDate.now();
            this.iniciMesActual = avui.withDayOfMonth(1);
            this.llindarEliminar = eliminacioMesos != null && eliminacioMesos > 0 ? this.iniciMesActual.minusMonths(eliminacioMesos) : null;
            this.llindarMensual = compactacioMesos != null && compactacioMesos > 0 ? this.iniciMesActual.minusMonths(compactacioMesos) : null;
        }
    }

    @Getter
    static class ClauDimensio {
        private final Long entornAppId;
        private final LocalDate data;
        private final Map<String, String> dimensions;

        ClauDimensio(Long entornAppId, LocalDate data, Map<String, String> dimensions){
            this.entornAppId = entornAppId;
            this.data = data;
            this.dimensions = dimensions;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClauDimensio c = (ClauDimensio) o;
            return Objects.equals(entornAppId, c.entornAppId) && Objects.equals(data, c.data) && Objects.equals(dimensions, c.dimensions);
        }

        @Override public int hashCode() { return Objects.hash(entornAppId, data, dimensions); }
    }

    @Getter
    static class ResultatAgrupacio {
        final LinkedHashMap<ClauDimensio, List<FetEntity>> fetsPerPeriode;
        final Map<LocalDate, boolean[]> diesdePeriodeAmbFets;

        public ResultatAgrupacio() {
            this.fetsPerPeriode = new LinkedHashMap<>();
            this.diesdePeriodeAmbFets = new HashMap<>();
        }

        public int comptarDiesAmbDades(LocalDate data) {
            boolean[] dies = diesdePeriodeAmbFets.get(data);
            if (dies == null) return 0;
            int count = 0;
            for (boolean diaAmbDades : dies) {
                if (diaAmbDades) count++;
            }
            return count;
        }
    }



//    /**
//     * Compactar fets que tinguin dimensions amb valors marcats com a agrupables, substituint
//     * el valor per l'indicat a valorAgrupacio i fusionant registres.
//     *
//     * Per ara no s'utilitza ja que és molt costós
//     */
//    protected void compactarPerDimensioAgrupable(EntornApp entornApp) {
//        // Fase 1: Normalitza valors de dimensions que estiguin marcats com a "agrupables"
//        // i fusiona els fets que, un cop normalitzats, passen a tenir la mateixa clau (entorn, data, dimensions).
//        log.info("[Compactacio] Fase 1 - Compactació per dimensions agrupables: inici");
//        try {
//            // Obtenim els reemplaços per dimensions agrupables.
//            var reemplaCosPerDimensio = construirMapaReemplac(entornApp);
//            if (reemplaCosPerDimensio.isEmpty()) {
//                log.info("[Compactacio] No hi ha dimensions-valor agrupables configurades. Res a fer.");
//                return;
//            }
//
//            // Estratègia de rendiment: si el volum és gran, processam per lots evitant carregar-ho tot en memòria
//            long total = 0L;
//            try {
//                total = fetRepository.countByEntornAppId(entornApp.getId());
//            } catch (Exception ignore) {
//                // alguns mocks/tests no tenen aquest mètode configurat: caurem al camí original
//            }
//
//            final int PAGE_SIZE = 5000; // mida de lot conservadora
//            final int FLUSH_INTERVAL = 1000;
//            int fusionats = 0;
//            int eliminats = 0;
//
//            if (total > PAGE_SIZE) {
//                log.info("[Compactacio] Processament paginat per entorn {}. Total fets: {}. Mida pàgina: {}", entornApp.getId(), total, PAGE_SIZE);
//                int page = 0;
//                while (true) {
//                    var pageable = PageRequest.of(page, PAGE_SIZE);
//                    var pageData = fetRepository.findByEntornAppId(entornApp.getId(), pageable);
//                    if (pageData == null || pageData.isEmpty()) break;
//
//                    var agregats = agruparFetsAmbDimensionsNormalitzades(entornApp.getId(), pageData.getContent(), reemplaCosPerDimensio);
//                    if (!agregats.isEmpty()) {
//                        var indicadorsPerEntorn = carregarIndicadorsPerEntorn(agregats);
//                        int[] counters = fusionarIEliminarBatch(agregats, indicadorsPerEntorn);
//                        fusionats += counters[0];
//                        eliminats += counters[1];
//                    }
//
//                    // Gestionam memòria del context de persistència
//                    if (((page + 1) * PAGE_SIZE) % FLUSH_INTERVAL == 0) {
//                        try {
//                            entityManager.flush(); entityManager.clear();
//                        } catch (Exception ignored) {}
//                    }
//
//                    page++;
//                    if (!pageData.hasNext()) break;
//                }
//                log.info("[Compactacio] Fase 1 finalitzada (paginat). Fets fusionats/actualitzats: {}, fets eliminats: {}", fusionats, eliminats);
//                return;
//            }
//
//            // Camí original (volum petit o tests)
//            var totsFets = carregarFetsEntorn(entornApp);
//            var agregats = agruparFetsAmbDimensionsNormalitzades(entornApp.getId(), totsFets, reemplaCosPerDimensio);
//            if (agregats.isEmpty()) {
//                log.info("[Compactacio] No s'han detectat fets a reagrupar per dimensions agrupables.");
//                return;
//            }
//
//            var indicadorsPerEntorn = carregarIndicadorsPerEntorn(agregats);
//
//            int[] counters = fusionarIEliminarBatch(agregats, indicadorsPerEntorn);
//            log.info("[Compactacio] Fase 1 finalitzada. Fets fusionats/actualitzats: {}, fets eliminats: {}", counters[0], counters[1]);
//        } catch (Exception e) {
//            log.warn("[Compactacio] Error durant la compactació per dimensions agrupables", e);
//        } finally {
//            try { entityManager.flush(); entityManager.clear(); } catch (Exception ignored) {}
//            log.info("[Compactacio] Fase 1 - Compactació per dimensions agrupables: fi");
//        }
//    }
//
//    private Map<String, Map<String, String>> construirMapaReemplac(EntornApp entornApp) {
//        // Construeix un mapa de reemplaç per cada dimensió: codiDimensio -> (valorOriginal -> valorAgrupacio)
//        // Es filtra per l'entorn proporcionat per a limitar l'abast de la compactació.
//        var totsDimVal = dimensioValorRepository.findByDimensioEntornAppIdAndAgrupableTrueAndValorAgrupacioIsNotNull(entornApp.getId());
//        var reemplaCosPerDimensio = new HashMap<String, Map<String, String>>();
//        totsDimVal.stream()
//                .filter(dv -> dv.getDimensio() != null && dv.getDimensio().getCodi() != null)
//                .forEach(dv -> {
//                    reemplaCosPerDimensio.computeIfAbsent(dv.getDimensio().getCodi(), k -> new HashMap<>())
//                            .put(dv.getValor(), dv.getValorAgrupacio());
//                });
//        return reemplaCosPerDimensio;
//    }
//
//    // Visibilitat ampliada a protected per facilitar proves dirigides a l'agrupació per dimensions.
//    protected LinkedHashMap<ClauDimensio, List<FetEntity>> agruparFetsAmbDimensionsNormalitzades(Long entornAppId,
//                                                                                               List<FetEntity> totsFets,
//                                                                                               Map<String, Map<String, String>> reemplaCosPerDimensio) {
//        // Recorre tots els fets, aplica els reemplaços de dimensions i agrupa
//        // només aquells que canvien després de la normalització.
//        var agregats = new LinkedHashMap<ClauDimensio, List<FetEntity>>();
//        for (var fet : totsFets) {
//            var dims = new HashMap<>(fet.getDimensionsJson() == null ? Map.of() : fet.getDimensionsJson());
//            boolean canvi = false;
//            for (var entry : reemplaCosPerDimensio.entrySet()) {
//                String dimCodi = entry.getKey();
//                var mapValors = entry.getValue();
//                if (dims.containsKey(dimCodi)) {
//                    String valor = dims.get(dimCodi);
//                    String nouValor = mapValors.get(valor);
//                    if (nouValor != null && !nouValor.equals(valor)) {
//                        dims.put(dimCodi, nouValor);
//                        canvi = true;
//                    }
//                }
//            }
//            if (canvi) {
//                var clau = new ClauDimensio(fet.getEntornAppId(), fet.getTemps().getData(), Collections.unmodifiableMap(dims));
//                agregats.computeIfAbsent(clau, k -> new ArrayList<>()).add(fet);
//            }
//        }
//        return agregats;
//    }
//
//    private Map<Long, Map<String, IndicadorEntity>> carregarIndicadorsPerEntorn(Map<ClauDimensio, List<FetEntity>> agregats) {
//        // Per rendiment, cachem configuració d'indicadors per entornAppId i codi.
//        // Això evita consultes repetides i accelera el càlcul d'agregacions.
//        var indicadorsPerEntorn = new HashMap<Long, Map<String, IndicadorEntity>>();
//        for (var clau : agregats.keySet()) {
//            indicadorsPerEntorn.computeIfAbsent(clau.entornAppId, id -> {
//                var llista = indicadorRepository.findByEntornAppId(id);
//                var map = new HashMap<String, IndicadorEntity>();
//                for (var ind : llista) map.put(ind.getCodi(), ind);
//                return map;
//            });
//        }
//        return indicadorsPerEntorn;
//    }
//
//    private int[] fusionarIEliminarBatch(Map<ClauDimensio, List<FetEntity>> agregats,
//                                         Map<Long, Map<String, IndicadorEntity>> indicadorsPerEntorn) {
//        // Per a cada grup amb dimensions normalitzades, reutilitza el primer registre com a destí,
//        // actualitza indicadors i elimina la resta en batch per eficiència.
//        int fetsFusioCreatsOuActualitzats = 0;
//        int fetsEliminats = 0;
//        for (var entry : agregats.entrySet()) {
//            var clau = entry.getKey();
//            var llista = entry.getValue();
//            var indicadorsResult = calcularIndicadorsAgregats(llista, indicadorsPerEntorn.getOrDefault(clau.entornAppId, Map.of()));
//            var primer = llista.get(0);
//            var desti = primer;
//            desti.setDimensionsJson(new HashMap<>(clau.dims));
//            desti.getTemps().setData(clau.data);
//            desti.setIndicadorsJson(indicadorsResult);
//            fetRepository.save(desti);
//            fetsFusioCreatsOuActualitzats++;
//            if (llista.size() > 1) {
//                var aEliminar = llista.subList(1, llista.size());
//                fetRepository.deleteAllInBatch(aEliminar);
//                fetsEliminats += aEliminar.size();
//            }
//        }
//        return new int[]{fetsFusioCreatsOuActualitzats, fetsEliminats};
//    }
//
//    private List<FetEntity> carregarFetsEntorn(EntornApp entornApp) {
//        // Carrega només els fets de l'entorn especificat.
//        // D'aquesta manera evitam processar dades d'altres entorns.
//        var totsFets = fetRepository.findByEntornAppId(entornApp.getId());
//        log.info("[Compactacio] Entorn {} -> fets carregats per revisar agrupació: {}", entornApp.getId(), totsFets.size());
//        return totsFets;
//    }

}
