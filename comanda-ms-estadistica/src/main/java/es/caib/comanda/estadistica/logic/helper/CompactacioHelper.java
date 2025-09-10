package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.CompactacioEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.FetTipusEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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

    public CompactacioHelper(DimensioValorRepository dimensioValorRepository,
                             FetRepository fetRepository,
                             IndicadorRepository indicadorRepository,
                             EstadisticaClientHelper estadisticaClientHelper) {
        this.dimensioValorRepository = dimensioValorRepository;
        this.fetRepository = fetRepository;
        this.indicadorRepository = indicadorRepository;
        this.estadisticaClientHelper = estadisticaClientHelper;
    }

    private MonitorEstadistica initializeMonitor(EntornApp entornApp) {
        return new MonitorEstadistica(
                entornApp.getId(),
                entornApp.getEstadisticaInfoUrl(),
                entornApp.getEstadisticaUrl(),
                estadisticaClientHelper);
    }

    @Transactional
    public void compactar(EntornApp entornApp) {
        // Punt d'entrada del procés de compactació per un entorn concret.
        // Aquest mètode orquestra les dues fases: 1) compactació per dimensions agrupables i 2) compactació temporal + retenció.
        log.info("[Compactacio] Inici del procés de compactació de fets per l'entornApp {}", entornApp.getId());

        MonitorEstadistica monitorEstadistica = initializeMonitor(entornApp);
        try {
            monitorEstadistica.startCompactarAction();
            // 1) Compactació per valors de dimensions agrupables
//            compactarPerDimensioAgrupable(entornApp);

            // 2) Compactació temporal i eliminació per retenció (pendent de configuració EntornApp)
            compactarTemporalIEsborraPerRetencio(entornApp);
            monitorEstadistica.endCompactarAction();
            log.info("[Compactacio] Procés de compactació finalitzat");
        } catch (Exception e) {
            log.error("[Compactacio] Error en el procés de compactació", e);
            monitorEstadistica.endCompactarAction(e);
        }
    }

    /**
     * Compactar fets que tinguin dimensions amb valors marcats com a agrupables, substituint
     * el valor per l'indicat a valorAgrupacio i fusionant registres.
     *
     */
    protected void compactarPerDimensioAgrupable(EntornApp entornApp) {
        // Fase 1: Normalitza valors de dimensions que estiguin marcats com a "agrupables"
        // i fusiona els fets que, un cop normalitzats, passen a tenir la mateixa clau (entorn, data, dimensions).
        log.info("[Compactacio] Fase 1 - Compactació per dimensions agrupables: inici");
        try {
            // Obtenim els reemplaços per dimensions agrupables.
            var reemplaCosPerDimensio = construirMapaReemplac(entornApp);
            if (reemplaCosPerDimensio.isEmpty()) {
                log.info("[Compactacio] No hi ha dimensions-valor agrupables configurades. Res a fer.");
                return;
            }

            // Estratègia de rendiment: si el volum és gran, processam per lots evitant carregar-ho tot en memòria
            long total = 0L;
            try {
                total = fetRepository.countByEntornAppId(entornApp.getId());
            } catch (Exception ignore) {
                // alguns mocks/tests no tenen aquest mètode configurat: caurem al camí original
            }

            final int PAGE_SIZE = 5000; // mida de lot conservadora
            final int FLUSH_INTERVAL = 1000;
            int fusionats = 0;
            int eliminats = 0;

            if (total > PAGE_SIZE) {
                log.info("[Compactacio] Processament paginat per entorn {}. Total fets: {}. Mida pàgina: {}", entornApp.getId(), total, PAGE_SIZE);
                int page = 0;
                while (true) {
                    var pageable = PageRequest.of(page, PAGE_SIZE);
                    var pageData = fetRepository.findByEntornAppId(entornApp.getId(), pageable);
                    if (pageData == null || pageData.isEmpty()) break;

                    var agregats = agruparFetsAmbDimensionsNormalitzades(entornApp.getId(), pageData.getContent(), reemplaCosPerDimensio);
                    if (!agregats.isEmpty()) {
                        var indicadorsPerEntorn = carregarIndicadorsPerEntorn(agregats);
                        int[] counters = fusionarIEliminarBatch(agregats, indicadorsPerEntorn);
                        fusionats += counters[0];
                        eliminats += counters[1];
                    }

                    // Gestionam memòria del context de persistència
                    if (((page + 1) * PAGE_SIZE) % FLUSH_INTERVAL == 0) {
                        try {
                            entityManager.flush(); entityManager.clear();
                        } catch (Exception ignored) {}
                    }

                    page++;
                    if (!pageData.hasNext()) break;
                }
                log.info("[Compactacio] Fase 1 finalitzada (paginat). Fets fusionats/actualitzats: {}, fets eliminats: {}", fusionats, eliminats);
                return;
            }

            // Camí original (volum petit o tests)
            var totsFets = carregarFetsEntorn(entornApp);
            var agregats = agruparFetsAmbDimensionsNormalitzades(entornApp.getId(), totsFets, reemplaCosPerDimensio);
            if (agregats.isEmpty()) {
                log.info("[Compactacio] No s'han detectat fets a reagrupar per dimensions agrupables.");
                return;
            }

            var indicadorsPerEntorn = carregarIndicadorsPerEntorn(agregats);

            int[] counters = fusionarIEliminarBatch(agregats, indicadorsPerEntorn);
            log.info("[Compactacio] Fase 1 finalitzada. Fets fusionats/actualitzats: {}, fets eliminats: {}", counters[0], counters[1]);
        } catch (Exception e) {
            log.warn("[Compactacio] Error durant la compactació per dimensions agrupables", e);
        } finally {
            try { entityManager.flush(); entityManager.clear(); } catch (Exception ignored) {}
            log.info("[Compactacio] Fase 1 - Compactació per dimensions agrupables: fi");
        }
    }

    private Map<String, Map<String, String>> construirMapaReemplac(EntornApp entornApp) {
        // Construeix un mapa de reemplaç per cada dimensió: codiDimensio -> (valorOriginal -> valorAgrupacio)
        // Es filtra per l'entorn proporcionat per a limitar l'abast de la compactació.
        var totsDimVal = dimensioValorRepository.findByDimensioEntornAppIdAndAgrupableTrueAndValorAgrupacioIsNotNull(entornApp.getId());
        var reemplaCosPerDimensio = new HashMap<String, Map<String, String>>();
        totsDimVal.stream()
                .filter(dv -> dv.getDimensio() != null && dv.getDimensio().getCodi() != null)
                .forEach(dv -> {
                    reemplaCosPerDimensio.computeIfAbsent(dv.getDimensio().getCodi(), k -> new HashMap<>())
                            .put(dv.getValor(), dv.getValorAgrupacio());
                });
        return reemplaCosPerDimensio;
    }

    private List<FetEntity> carregarFetsEntorn(EntornApp entornApp) {
        // Carrega només els fets de l'entorn especificat.
        // D'aquesta manera evitam processar dades d'altres entorns.
        var totsFets = fetRepository.findByEntornAppId(entornApp.getId());
        log.info("[Compactacio] Entorn {} -> fets carregats per revisar agrupació: {}", entornApp.getId(), totsFets.size());
        return totsFets;
    }

    private static class ClauDimensio {
        final Long entornAppId;
        final LocalDate data;
        final Map<String, String> dims;
        ClauDimensio(Long entornAppId, LocalDate data, Map<String, String> dims){
            this.entornAppId=entornAppId;
            this.data=data;
            this.dims=dims;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClauDimensio c = (ClauDimensio) o;
            return Objects.equals(entornAppId, c.entornAppId) && Objects.equals(data, c.data) && Objects.equals(dims, c.dims);
        }
        @Override public int hashCode() { return Objects.hash(entornAppId, data, dims); }
    }

    // Visibilitat ampliada a protected per facilitar proves dirigides a l'agrupació per dimensions.
    protected LinkedHashMap<ClauDimensio, List<FetEntity>> agruparFetsAmbDimensionsNormalitzades(Long entornAppId,
                                                                                               List<FetEntity> totsFets,
                                                                                               Map<String, Map<String, String>> reemplaCosPerDimensio) {
        // Recorre tots els fets, aplica els reemplaços de dimensions i agrupa
        // només aquells que canvien després de la normalització.
        var agregats = new LinkedHashMap<ClauDimensio, List<FetEntity>>();
        for (var fet : totsFets) {
            var dims = new HashMap<>(fet.getDimensionsJson() == null ? Map.of() : fet.getDimensionsJson());
            boolean canvi = false;
            for (var entry : reemplaCosPerDimensio.entrySet()) {
                String dimCodi = entry.getKey();
                var mapValors = entry.getValue();
                if (dims.containsKey(dimCodi)) {
                    String valor = dims.get(dimCodi);
                    String nouValor = mapValors.get(valor);
                    if (nouValor != null && !nouValor.equals(valor)) {
                        dims.put(dimCodi, nouValor);
                        canvi = true;
                    }
                }
            }
            if (canvi) {
                var clau = new ClauDimensio(fet.getEntornAppId(), fet.getTemps().getData(), Collections.unmodifiableMap(dims));
                agregats.computeIfAbsent(clau, k -> new ArrayList<>()).add(fet);
            }
        }
        return agregats;
    }

    private Map<Long, Map<String, IndicadorEntity>> carregarIndicadorsPerEntorn(Map<ClauDimensio, List<FetEntity>> agregats) {
        // Per rendiment, cachem configuració d'indicadors per entornAppId i codi.
        // Això evita consultes repetides i accelera el càlcul d'agregacions.
        var indicadorsPerEntorn = new HashMap<Long, Map<String, IndicadorEntity>>();
        for (var clau : agregats.keySet()) {
            indicadorsPerEntorn.computeIfAbsent(clau.entornAppId, id -> {
                var llista = indicadorRepository.findByEntornAppId(id);
                var map = new HashMap<String, IndicadorEntity>();
                for (var ind : llista) map.put(ind.getCodi(), ind);
                return map;
            });
        }
        return indicadorsPerEntorn;
    }

    private int[] fusionarIEliminarBatch(Map<ClauDimensio, List<FetEntity>> agregats,
                                         Map<Long, Map<String, IndicadorEntity>> indicadorsPerEntorn) {
        // Per a cada grup amb dimensions normalitzades, reutilitza el primer registre com a destí,
        // actualitza indicadors i elimina la resta en batch per eficiència.
        int fetsFusioCreatsOuActualitzats = 0;
        int fetsEliminats = 0;
        for (var entry : agregats.entrySet()) {
            var clau = entry.getKey();
            var llista = entry.getValue();
            var indicadorsResult = calcularIndicadorsAgregats(llista, indicadorsPerEntorn.getOrDefault(clau.entornAppId, Map.of()));
            var primer = llista.get(0);
            var desti = primer;
            desti.setDimensionsJson(new HashMap<>(clau.dims));
            desti.getTemps().setData(clau.data);
            desti.setIndicadorsJson(indicadorsResult);
            fetRepository.save(desti);
            fetsFusioCreatsOuActualitzats++;
            if (llista.size() > 1) {
                var aEliminar = llista.subList(1, llista.size());
                fetRepository.deleteAllInBatch(aEliminar);
                fetsEliminats += aEliminar.size();
            }
        }
        return new int[]{fetsFusioCreatsOuActualitzats, fetsEliminats};
    }

    private Map<String, Double> calcularIndicadorsAgregats(List<FetEntity> llista, Map<String, IndicadorEntity> cfg) {
        // Calcula els nous valors d'indicadors per a un grup segons la configuració de compactació de cada indicador.
        // Suporta SUMA, MÀXIMA, MÍNIMA i MITJANA (amb comptador especial opcional).
        Map<String, Double> indicadorsResult = new HashMap<>();
        Map<String, Double> sumatori = new HashMap<>();
        Map<String, Integer> comptatge = new HashMap<>();
        Map<String, Double> comptadorPerMitjana = new HashMap<>();
        for (var fet : llista) {
            var indMap = fet.getIndicadorsJson();
            if (indMap == null) continue;
            for (var e : indMap.entrySet()) {
                String codi = e.getKey();
                Double valor = e.getValue();
                if (valor == null) continue;
                var indCfg = cfg.get(codi);
                var tipus = indCfg != null && indCfg.getTipusCompactacio() != null ? indCfg.getTipusCompactacio() : CompactacioEnum.SUMA;
                switch (tipus) {
                    case SUMA:
                        sumatori.merge(codi, valor, Double::sum);
                        break;
                    case MAXIMA:
                        indicadorsResult.merge(codi, valor, java.lang.Math::max);
                        break;
                    case MINIMA:
                        indicadorsResult.merge(codi, valor, java.lang.Math::min);
                        break;
                    case MITJANA:
                        sumatori.merge(codi, valor, Double::sum);
                        comptatge.merge(codi, 1, Integer::sum);
                        if (indCfg != null && indCfg.getIndicadorComptadorPerMitjana() != null) {
                            String compteCodi = indCfg.getIndicadorComptadorPerMitjana().getCodi();
                            Double compteValor = indMap.get(compteCodi);
                            if (compteValor != null) {
                                comptadorPerMitjana.merge(codi, compteValor, Double::sum);
                            }
                        }
                        break;
                }
            }
        }
        for (var e : sumatori.entrySet()) {
            String codi = e.getKey();
            double suma = e.getValue();
            var indCfg = cfg.get(codi);
            var tipus = indCfg != null && indCfg.getTipusCompactacio() != null ? indCfg.getTipusCompactacio() : CompactacioEnum.SUMA;
            if (tipus == CompactacioEnum.SUMA) {
                indicadorsResult.put(codi, suma);
            } else if (tipus == CompactacioEnum.MITJANA) {
                Double divisorEspecial = comptadorPerMitjana.get(codi);
                if (divisorEspecial != null && divisorEspecial > 0) {
                    indicadorsResult.put(codi, suma / divisorEspecial);
                } else {
                    int n = comptatge.getOrDefault(codi, 0);
                    indicadorsResult.put(codi, n > 0 ? (suma / n) : 0d);
                }
            }
        }
        return indicadorsResult;
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
            var llindars = calcularLlindars();
            Long entornAppId = entornApp.getId();
            int totalFetsActualitzats = 0;
            int totalFetsEliminats = 0;

            // 1.a) Eliminació per retenció en batch
            // Calcular el llindar de retenció: tot el que sigui anterior a aquest mes menys 'eliminacioMesos'
            LocalDate llindarEliminar = entornApp.getEliminacioMesos() != null && entornApp.getEliminacioMesos() > 0 ? llindars.iniciMesActual.minusMonths(entornApp.getEliminacioMesos()) : null;
            if (llindarEliminar != null) {
                long eliminats = fetRepository.deleteByEntornAppIdAndTempsDataBefore(entornAppId, llindarEliminar);
                totalFetsEliminats += eliminats;
                log.info("[Compactacio] Entorn {} -> fets eliminats per retenció: {}", entornAppId, eliminats);
            }

            // Cache d'indicadors per a l'entorn
            var indicadorsPerEntorn = carregarIndicadorsPerEntornId(entornAppId);

            // 1.b) Compactació mensual
            // Reagrupa les dades anteriors al llindar mensual a l'inici del seu mes, aggregant indicadors segons configuració.
            LocalDate llindarMensual = entornApp.getCompactacioMensualMesos() != null && entornApp.getCompactacioMensualMesos() > 0 ? llindars.iniciMesActual.minusMonths(entornApp.getCompactacioMensualMesos()) : null;
            if (llindarMensual != null) {
                var llistaMensual = fetRepository.findByEntornAppIdAndTempsDataBefore(entornAppId, llindarMensual)
                        .stream().filter(fet -> fet.getTipus() != FetTipusEnum.MENSUAL).collect(Collectors.toList());
                totalFetsActualitzats += compactarPerPeriode(llistaMensual, entornAppId, indicadorsPerEntorn, PeriodeTarget.MENSUAL);
            }

//            // 1.c) Compactació setmanal
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

    private static class LlindarsData {
        // Agrupa valors de data rellevants per calcular llindars d'operacions de compactació/retenció.
        final LocalDate avui = LocalDate.now();
        final LocalDate iniciMesActual = avui.withDayOfMonth(1);
    }

    private LlindarsData calcularLlindars() {
        // Càlcul centralitzat dels llindars per facilitar testabilitat i reutilització.
        return new LlindarsData();
    }

    private Map<String, IndicadorEntity> carregarIndicadorsPerEntornId(Long entornAppId) {
        // Carrega i indexa per codi la configuració d'indicadors per un entorn concret.
        var map = new HashMap<String, IndicadorEntity>();
        var llista = indicadorRepository.findByEntornAppId(entornAppId);
        for (var ind : llista) map.put(ind.getCodi(), ind);
        return map;
    }

    private enum PeriodeTarget {SETMANAL, MENSUAL}

    // Nova extracció: agrupació per període (setmana/mes) per facilitar proves i reutilització.
    protected LinkedHashMap<ClauDimensio, List<FetEntity>> agruparFetsPerPeriode(List<FetEntity> fets,
                                                                                  Long entornAppId,
                                                                                  PeriodeTarget target) {
        var agrupats = new LinkedHashMap<ClauDimensio, List<FetEntity>>();
        if (fets == null || fets.isEmpty()) return agrupats;

        for (var fet : fets) {
            var dims = new HashMap<>(fet.getDimensionsJson() == null ? Map.of() : fet.getDimensionsJson());
            var data = fet.getTemps().getData();
            LocalDate novaData;
            if (target == PeriodeTarget.MENSUAL) {
                novaData = data.withDayOfMonth(1);
            } else {
                // Dilluns com a inici de setmana (ISO)
                DayOfWeek first = DayOfWeek.MONDAY;
                DayOfWeek dow = data.getDayOfWeek();
                int diff = dow.getValue() - first.getValue();
                novaData = data.minusDays(diff);
            }
            var clau = new ClauDimensio(entornAppId, novaData, Collections.unmodifiableMap(dims));
            agrupats.computeIfAbsent(clau, k -> new ArrayList<>()).add(fet);
        }
        return agrupats;
    }

    private int compactarPerPeriode(List<FetEntity> fets,
                                    Long entornAppId,
                                    Map<String, IndicadorEntity> indCfgMap,
                                    PeriodeTarget target) {
        if (fets == null || fets.isEmpty()) return 0;

        var agrupats = agruparFetsPerPeriode(fets, entornAppId, target);

        int fetsActualitzats = 0;
        int fetsEliminats = 0;

        for (var entry : agrupats.entrySet()) {
            var clau = entry.getKey();
            var llista = entry.getValue();

            Map<String, Double> indicadorsResult = calcularIndicadorsAgregats(llista, indCfgMap);

            var primer = llista.get(0);
            var desti = primer;
            desti.getTemps().setData(clau.data);
            desti.setIndicadorsJson(indicadorsResult);
            desti.setTipus(FetTipusEnum.MENSUAL);
            long nombreFetsMateixMes = fets.stream()
                    .filter(fet -> fet.getTemps().getData().getMonthValue() == desti.getTemps().getData().getMonthValue())
                    .count();
            desti.setNumDies(Math.toIntExact(nombreFetsMateixMes));
            // dimensions es mantenen tal qual
            fetRepository.save(desti);
            fetsActualitzats++;

            if (llista.size() > 1) {
                var aEliminar = llista.subList(1, llista.size());
                fetRepository.deleteAllInBatch(aEliminar);
                fetsEliminats += aEliminar.size();
            }
        }

        log.info("[Compactacio] Compactació {}: fets fusionats/actualitzats: {}, fets eliminats: {}", target, fetsActualitzats, fetsEliminats);
        return fetsActualitzats;
    }

}
