package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.logic.intf.model.SalutNivell;
import es.caib.comanda.salut.persist.entity.SalutDetallEntity;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.entity.SalutIntegracioEntity;
import es.caib.comanda.salut.persist.entity.SalutMissatgeEntity;
import es.caib.comanda.salut.persist.entity.SalutSubsistemaEntity;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalutInfoHelperBuidatICompactatParameterizedTest {

    @Mock private SalutRepository salutRepository;
    @Mock private SalutIntegracioRepository salutIntegracioRepository;
    @Mock private SalutSubsistemaRepository salutSubsistemaRepository;
    @Mock private SalutMissatgeRepository salutMissatgeRepository;
    @Mock private SalutDetallRepository salutDetallRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private SalutInfoHelper helper;

    static class Step {
        LocalDateTime time;
        SalutEstat app;
        Integer appLat;
        SalutEstat bd;
        Integer bdLat;
        Long ok;
        Long err;
        String msgText; SalutNivell msgLevel; // INFO/WARN/ERROR
        String detailCode; String detailName; String detailValue;
        Step(LocalDateTime t, SalutEstat app, Integer appLat, SalutEstat bd, Integer bdLat,
             Long ok, Long err, String msgText, SalutNivell msgLevel,
             String detailCode, String detailName, String detailValue) {
            this.time = t; this.app = app; this.appLat = appLat; this.bd = bd; this.bdLat = bdLat;
            this.ok = ok; this.err = err; this.msgText = msgText; this.msgLevel = msgLevel;
            this.detailCode = detailCode; this.detailName = detailName; this.detailValue = detailValue;
        }
    }

    static Stream<List<Step>> params() {
        LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0).withHour(0).withMinute(0);
        List<Step> scenario = Arrays.asList(
                // 1) Primer registre (crea MINUTS via numeroDiesAgrupacio=1), i també HORA i DIA per ser 00:00
                new Step(base, SalutEstat.UP, 100, SalutEstat.UP, 80, 1L, 0L, "m1", SalutNivell.INFO, "d1", "Detall 1", "v1"),
                // 2) Segon minut (agrega a MINUTS existent)
                new Step(base.plusMinutes(1), SalutEstat.WARN, 200, SalutEstat.UP, 70, 2L, 1L, "m1", SalutNivell.WARN, "d1", "Detall 1", "v1b"),
                // 3) Tercer minut
                new Step(base.plusMinutes(2), SalutEstat.UP, 300, SalutEstat.UP, 60, 1L, 0L, "m2", SalutNivell.INFO, "d2", "Detall 2", "x"),
                // 4) Quart minut (encara dins finestra MINUTS actual -> no nou registre MINUTS)
                new Step(base.plusMinutes(3), SalutEstat.DEGRADED, 400, SalutEstat.WARN, 90, 3L, 2L, "m2", SalutNivell.ERROR, "d2", "Detall 2", "y"),
                // 5) Cinquè minut (fora finestra -> nou registre MINUTS)
                new Step(base.plusMinutes(4), SalutEstat.UP, 500, SalutEstat.UP, 50, 1L, 1L, "m3", SalutNivell.INFO, "d3", "Detall 3", "z"),
                // 6) Canvi d'hora (01:00)
                new Step(base.plusHours(1), SalutEstat.WARN, 150, SalutEstat.UP, 70, 0L, 1L, "m1", SalutNivell.WARN, "d1", "Detall 1", "v2"),
                // 7) Canvi de dia (endemà 00:00)
                new Step(base.plusDays(1), SalutEstat.UP, 120, SalutEstat.UP, 80, 5L, 0L, "m4", SalutNivell.INFO, "d4", "Detall 4", "w")
        );
        return Stream.of(scenario);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("params")
    @DisplayName("buidatIcompactat: comprova agregacions per MINUTS, HORA, DIA i merges de fills")
    void test_compactar_parametrized(List<Step> steps) {
        Long entornAppId = 100L;
        Long salutId = 500L;

        // Emulació d'emmagatzematge en memòria dels agregats per tipus
        Map<TipusRegistreSalut, List<SalutEntity>> agregats = new EnumMap<>(TipusRegistreSalut.class);
        Map<TipusRegistreSalut, SalutEntity> lastAgg = new EnumMap<>(TipusRegistreSalut.class);
        AtomicLong idGen = new AtomicLong(1000);

        // Mock: findTop retorna darrer agregat guardat per tipus
        when(salutRepository.findTopByEntornAppIdAndTipusRegistreOrderByIdDesc(eq(entornAppId), any()))
                .thenAnswer(inv -> {
                    TipusRegistreSalut tipus = inv.getArgument(1);
                    return lastAgg.get(tipus);
                });
        // Mock: neteja retorna buit
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // save(SalutEntity): assigna id, inicialitza conjunts i desa per tipus quan és agregat (MINUTS/HORA/DIA)
        when(salutRepository.save(any(SalutEntity.class))).thenAnswer(inv -> {
            SalutEntity s = inv.getArgument(0);
            if (s.getId() == null) s.setId(idGen.getAndIncrement());
            if (s.getSalutIntegracions() == null) s.setSalutIntegracions(new HashSet<>());
            if (s.getSalutSubsistemes() == null) s.setSalutSubsistemes(new HashSet<>());
            if (s.getSalutMissatges() == null) s.setSalutMissatges(new HashSet<>());
            if (s.getSalutDetalls() == null) s.setSalutDetalls(new HashSet<>());
            if (s.getTipusRegistre() != null && s.getTipusRegistre() != TipusRegistreSalut.MINUT) {
                agregats.computeIfAbsent(s.getTipusRegistre(), k -> new ArrayList<>()).add(s);
                lastAgg.put(s.getTipusRegistre(), s);
            }
            return s;
        });
        // save fills: afegeix a conjunts del pare per simular JPA
        when(salutIntegracioRepository.save(any(SalutIntegracioEntity.class))).thenAnswer(inv -> {
            SalutIntegracioEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(idGen.getAndIncrement());
            if (e.getSalut() != null) {
                SalutEntity p = e.getSalut();
                if (p.getSalutIntegracions() == null) p.setSalutIntegracions(new HashSet<>());
                p.getSalutIntegracions().add(e);
            }
            return e;
        });
        when(salutSubsistemaRepository.save(any(SalutSubsistemaEntity.class))).thenAnswer(inv -> {
            SalutSubsistemaEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(idGen.getAndIncrement());
            if (e.getSalut() != null) {
                SalutEntity p = e.getSalut();
                if (p.getSalutSubsistemes() == null) p.setSalutSubsistemes(new HashSet<>());
                p.getSalutSubsistemes().add(e);
            }
            return e;
        });
        when(salutMissatgeRepository.save(any(SalutMissatgeEntity.class))).thenAnswer(inv -> {
            SalutMissatgeEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(idGen.getAndIncrement());
            if (e.getSalut() != null) {
                SalutEntity p = e.getSalut();
                if (p.getSalutMissatges() == null) p.setSalutMissatges(new HashSet<>());
                p.getSalutMissatges().add(e);
            }
            return e;
        });
        when(salutDetallRepository.save(any(SalutDetallEntity.class))).thenAnswer(inv -> {
            SalutDetallEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(idGen.getAndIncrement());
            if (e.getSalut() != null) {
                SalutEntity p = e.getSalut();
                if (p.getSalutDetalls() == null) p.setSalutDetalls(new HashSet<>());
                p.getSalutDetalls().add(e);
            }
            return e;
        });

        // Execució seqüencial
        for (int i = 0; i < steps.size(); i++) {
            Step st = steps.get(i);
            SalutEntity dades = new SalutEntity();
            dades.setId(salutId);
            dades.setEntornAppId(entornAppId);
            dades.setData(st.time);
            dades.setAppEstat(st.app);
            dades.setAppLatencia(st.appLat);
            dades.setBdEstat(st.bd);
            dades.setBdLatencia(st.bdLat);
            // Fills per al registre de minut
            SalutIntegracioEntity i1 = new SalutIntegracioEntity();
            i1.setSalut(dades); i1.setCodi("API"); i1.setEstat(st.app); i1.setLatencia(st.appLat); i1.setTotalOk(st.ok); i1.setTotalError(st.err);
            SalutSubsistemaEntity s1 = new SalutSubsistemaEntity();
            s1.setSalut(dades); s1.setCodi("SUB"); s1.setEstat(st.bd); s1.setLatencia(st.bdLat); s1.setTotalOk(st.ok); s1.setTotalError(st.err);
            SalutMissatgeEntity m1 = new SalutMissatgeEntity();
            m1.setSalut(dades); m1.setData(st.time); m1.setNivell(st.msgLevel); m1.setMissatge(st.msgText);
            SalutDetallEntity d1 = new SalutDetallEntity();
            d1.setSalut(dades); d1.setCodi(st.detailCode); d1.setNom(st.detailName); d1.setValor(st.detailValue);
            dades.setSalutIntegracions(new HashSet<>(Collections.singletonList(i1)));
            dades.setSalutSubsistemes(new HashSet<>(Collections.singletonList(s1)));
            dades.setSalutMissatges(new HashSet<>(Collections.singleton(m1)));
            dades.setSalutDetalls(new HashSet<>(Collections.singleton(d1)));

            when(salutRepository.findById(salutId)).thenReturn(Optional.of(dades));

            helper.compactar(entornAppId, salutId);

            // Validacions en punts clau
            if (i == 0) {
                // Primer registre: s'han creat 3 agregats
                assertEquals(1, agregats.getOrDefault(TipusRegistreSalut.MINUTS, List.of()).size(), "Ha de crear 1 agregat MINUTS");
                assertNotNull(lastAgg.get(TipusRegistreSalut.HORA), "Ha de crear agregat HORA al minut 0");
                assertNotNull(lastAgg.get(TipusRegistreSalut.DIA), "Ha de crear agregat DIA al 00:00");
            }
            if (i == 3) {
                // Després del 4t: encara un únic agregat MINUTS amb 4 elements
                List<SalutEntity> mins = agregats.getOrDefault(TipusRegistreSalut.MINUTS, List.of());
                assertEquals(1, mins.size(), "El 4t ha d'agregar al mateix MINUTS");
                SalutEntity agg = mins.get(0);
                assertEquals(4, agg.getNumElements());
                // Mitjana app
                assertEquals(Integer.valueOf((100+200+300+400)/4), agg.getAppLatenciaMitjana());
                // Integració totals
                SalutIntegracioEntity api = agg.getSalutIntegracions().stream().filter(x -> "API".equals(x.getCodi())).findFirst().orElseThrow();
                assertEquals(Long.valueOf(1+2+1+3), api.getTotalOk());
                assertEquals(Long.valueOf(0+1+0+2), api.getTotalError());
                // Missatges: m1 actualitzat a WARN, m2 creat
                assertTrue(agg.getSalutMissatges().stream().anyMatch(x -> "m1".equals(x.getMissatge()) && x.getNivell() == SalutNivell.WARN));
                assertTrue(agg.getSalutMissatges().stream().anyMatch(x -> "m2".equals(x.getMissatge())));
                // Detalls: d1 actualitzat valor v1b, d2 creat i actualitzat a y
                assertTrue(agg.getSalutDetalls().stream().anyMatch(x -> "d1".equals(x.getCodi()) && "v1b".equals(x.getValor())));
                assertTrue(agg.getSalutDetalls().stream().anyMatch(x -> "d2".equals(x.getCodi()) && "y".equals(x.getValor())));
            }
            if (i == 4) {
                // Cinquè crea un nou MINUTS
                List<SalutEntity> mins = agregats.getOrDefault(TipusRegistreSalut.MINUTS, List.of());
                assertEquals(2, mins.size(), "El 5è ha de crear un nou agregat MINUTS");
                SalutEntity prev = mins.get(0);
                SalutEntity curr = mins.get(1);
                assertEquals(4, prev.getNumElements());
                assertEquals(1, curr.getNumElements());
            }
            if (i == 5) {
                // Hora nova: ha de crear un nou HORA a l'01:00
                List<SalutEntity> hores = agregats.getOrDefault(TipusRegistreSalut.HORA, List.of());
                assertTrue(hores.size() >= 2, "Ha d'existir nou agregat HORA en canviar d'hora");
                assertEquals(steps.get(5).time, lastAgg.get(TipusRegistreSalut.HORA).getData());
            }
            if (i == 6) {
                // Dia nou: ha de crear un nou DIA a l'endemà 00:00
                List<SalutEntity> dies = agregats.getOrDefault(TipusRegistreSalut.DIA, List.of());
                assertTrue(dies.size() >= 2, "Ha d'existir nou agregat DIA en canviar de dia");
                assertEquals(steps.get(6).time, lastAgg.get(TipusRegistreSalut.DIA).getData());
            }
        }

        // Comprovació addicional: percentatges d'estats (almenys no nuls)
        SalutEntity primerMinuts = agregats.get(TipusRegistreSalut.MINUTS).get(0);
        assertNotNull(primerMinuts.getAppPctUp());
        assertNotNull(primerMinuts.getBdPctUp());
    }
}
