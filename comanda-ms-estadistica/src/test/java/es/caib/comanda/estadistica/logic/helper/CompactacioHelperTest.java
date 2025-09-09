package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.CompactacioEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Proves unitàries per CompactacioHelper.
 * - Comentaris i noms en català.
 * - Estructura AAA.
 */
@ExtendWith(MockitoExtension.class)
public class CompactacioHelperTest {

    @Mock DimensioValorRepository dimensioValorRepository;
    @Mock FetRepository fetRepository;
    @Mock IndicadorRepository indicadorRepository;

    @InjectMocks CompactacioHelper helper;

    private EntornApp entornApp;

    @BeforeEach
    void setUp() {
        entornApp = EntornApp.builder()
                .id(21L)
                .compactable(true)
                .compactacioMensualMesos(12)
                .compactacioSetmanalMesos(6)
                .eliminacioMesos(24)
                .build();
    }

    private FetEntity fet(Long entornId, LocalDate data, Map<String,String> dims, Map<String,Double> inds){
        FetEntity f = new FetEntity();
        TempsEntity t = new TempsEntity();
        t.setData(data);
        f.setTemps(t);
        f.setDimensionsJson(dims==null?null:new HashMap<>(dims));
        f.setIndicadorsJson(inds==null?null:new HashMap<>(inds));
        f.setEntornAppId(entornId);
        return f;
    }

    private IndicadorEntity indicador(String codi, CompactacioEnum tipus){
        IndicadorEntity i = new IndicadorEntity();
        i.setCodi(codi);
        i.setTipusCompactacio(tipus);
        return i;
    }

    @Test
    void compactarPerDimensioAgrupable_HappyPath_fusionaISuprimeix() {
        // Arrange
        // No paginat: no es configura countByEntornAppId, així el mètode cau al camí original.
        // Preparem una dimensió agrupable: codi DIM, valor "a" -> agrupa a "A"
        var dim = new DimensioEntity();
        dim.setCodi("DIM");
        var dv = new DimensioValorEntity();
        dv.setDimensio(dim); dv.setAgrupable(true); dv.setValor("a"); dv.setValorAgrupacio("A");
        when(dimensioValorRepository.findByDimensioEntornAppIdAndAgrupableTrueAndValorAgrupacioIsNotNull(21L)).thenReturn(List.of(dv));

        // Tenim 2 fets amb dimensions que es re-etiqueten a la mateixa clau i s'han de fusionar
        Map<String,String> d1 = Map.of("DIM", "a", "ALT", "x");
        Map<String,String> d2 = Map.of("DIM", "a", "ALT", "x");
        Map<String,Double> i1 = Map.of("V", 2d);
        Map<String,Double> i2 = Map.of("V", 5d);
        FetEntity f1 = fet(21L, LocalDate.of(2024,5,10), d1, i1);
        FetEntity f2 = fet(21L, LocalDate.of(2024,5,10), d2, i2);
        when(fetRepository.findByEntornAppId(21L)).thenReturn(List.of(f1, f2));

        // Configuració d'indicadors: V = SUMA
        var indV = indicador("V", CompactacioEnum.SUMA);
        when(indicadorRepository.findByEntornAppId(21L)).thenReturn(List.of(indV));

        // Act
        helper.compactarPerDimensioAgrupable(entornApp);

        // Assert
        // S'ha de guardar la fusió (V=7) i eliminar un duplicat
        ArgumentCaptor<FetEntity> cap = ArgumentCaptor.forClass(FetEntity.class);
        verify(fetRepository).save(cap.capture());
        FetEntity guardat = cap.getValue();
        assertEquals(7d, guardat.getIndicadorsJson().get("V"));
        assertEquals("A", guardat.getDimensionsJson().get("DIM"));
        verify(fetRepository).deleteAllInBatch(anyList());
    }

    @Test
    void compactarPerDimensioAgrupable_Paginat_processaPerPaginesIMergeja() {
        // Arrange
        // Paginat: forçam total > PAGE_SIZE (5000)
        when(fetRepository.countByEntornAppId(21L)).thenReturn(6000L);

        // Reemplaç DIM: "a" -> "A" perquè s'agrupin
        var dim = new DimensioEntity();
        dim.setCodi("DIM");
        var dv = new DimensioValorEntity();
        dv.setDimensio(dim);
        dv.setAgrupable(true);
        dv.setValor("a");
        dv.setValorAgrupacio("A");
        when(dimensioValorRepository.findByDimensioEntornAppIdAndAgrupableTrueAndValorAgrupacioIsNotNull(21L)).thenReturn(List.of(dv));

        // Construïm dues pàgines. Pàgina 0: dos fets que, un cop normalitzats, coincideixen i s'han de fusionar.
        var f1 = fet(21L, LocalDate.of(2024,5,10), Map.of("DIM","a","ALT","x"), Map.of("V",2d));
        var f2 = fet(21L, LocalDate.of(2024,5,10), Map.of("DIM","a","ALT","x"), Map.of("V",5d));
        // Pàgina 1: cap canvi (no hi ha DIM), per assegurar que no es fusiona res.
        var f3 = fet(21L, LocalDate.of(2024,5,11), Map.of("ALT","y"), Map.of("V",3d));

        var page0 = new PageImpl<>(java.util.List.of(f1, f2), PageRequest.of(0, 5000), 6000);
        var page1 = new PageImpl<>(java.util.List.of(f3), PageRequest.of(1, 5000), 6000);

        when(fetRepository.findByEntornAppId(eq(21L), argThat(p -> p.getPageNumber()==0 && p.getPageSize()==5000))).thenReturn(page0);
        when(fetRepository.findByEntornAppId(eq(21L), argThat(p -> p.getPageNumber()==1 && p.getPageSize()==5000))).thenReturn(page1);

        // Configuració d'indicadors: V = SUMA
        var indV = indicador("V", CompactacioEnum.SUMA);
        when(indicadorRepository.findByEntornAppId(21L)).thenReturn(List.of(indV));

        // Act
        helper.compactarPerDimensioAgrupable(entornApp);

        // Assert
        // S'han de fer crides paginades i no a la llista completa
        verify(fetRepository, never()).findByEntornAppId(21L);
        verify(fetRepository, times(2)).findByEntornAppId(eq(21L), any(Pageable.class));

        // Ha d'haver una fusió a la pàgina 0 i un delete dels sobrants
        verify(fetRepository, atLeastOnce()).save(any(FetEntity.class));
        verify(fetRepository, atLeastOnce()).deleteAllInBatch(anyList());
    }

    @Test
    void calcularIndicadorsAgregats_sumaMaxMinMitjana_ambComptadorEspecial() throws Exception {
        // Arrange
        // Creem 3 fets amb indicadors A,B,C i un comptador especial CNT per a la mitjana de C
        Map<String,Double> i1 = Map.of("A", 10d, "B", 5d, "C", 6d, "CNT", 2d, "D", 5d);
        Map<String,Double> i2 = Map.of("A", 7d,  "B", 8d, "C", 4d, "CNT", 3d, "D", 7d);
        Map<String,Double> i3 = Map.of("A", 3d,  "B", 2d, "C", 10d, "CNT", 5d, "D", 3d);
        List<FetEntity> llista = List.of(
                fet(21L, LocalDate.of(2024,1,1), Map.of("X","a"), i1),
                fet(21L, LocalDate.of(2024,1,2), Map.of("X","a"), i2),
                fet(21L, LocalDate.of(2024,1,3), Map.of("X","a"), i3)
        );
        // Configuració d'indicadors: A=SUMA, B=MAXIMA, C=MITJANA amb comptador CNT
        IndicadorEntity indA = indicador("A", CompactacioEnum.SUMA);
        IndicadorEntity indB = indicador("B", CompactacioEnum.MAXIMA);
        IndicadorEntity indC = indicador("C", CompactacioEnum.MITJANA);
        IndicadorEntity indCNT = indicador("CNT", CompactacioEnum.SUMA);
        indC.setIndicadorComptadorPerMitjana(indCNT);
        IndicadorEntity indD = indicador("D", CompactacioEnum.MITJANA);
        Map<String, IndicadorEntity> cfg = new HashMap<>();
        cfg.put("A", indA);
        cfg.put("B", indB);
        cfg.put("C", indC);
        cfg.put("CNT", indCNT);
        cfg.put("D", indD);

        // Act
        // Fem servir reflexió per accedir al mètode privat calcularIndicadorsAgregats
        var m = CompactacioHelper.class.getDeclaredMethod("calcularIndicadorsAgregats", List.class, Map.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String,Double> result = (Map<String, Double>) m.invoke(helper, llista, cfg);

        // Assert
        assertEquals(20d, result.get("A")); // 10+7+3
        assertEquals(8d, result.get("B")); // max(5,8,2)
        // Mitjana de C per divisor especial: sum(C)=20, sum(CNT)=10 -> 20/10 = 2.0
        assertEquals(2.0, result.get("C"));
        assertEquals(5.0, result.get("D"));
    }

    @Test
    void compactarTemporalIEsborraPerRetencio_eliminaIAgrupa_PerSetmanaIMes() throws Exception {
        // Arrange
        // Llindars dependran de LocalDate.now(). No els sobreescrivim; només assegurem que les dates siguin prou antigues.
        LocalDate ara = LocalDate.now();
        Long entornId = 21L;
        // Dades antigues per eliminació (molt antigues)
        when(fetRepository.deleteByEntornAppIdAndTempsDataBefore(eq(entornId), any(LocalDate.class))).thenReturn(5L);

        // Dades per compactació mensual (abans del llindar mensual)
        FetEntity m1 = fet(entornId, ara.minusMonths(30).withDayOfMonth(15), Map.of("D","v1"), Map.of("A", 1d));
        FetEntity m2 = fet(entornId, ara.minusMonths(30).withDayOfMonth(20), Map.of("D","v1"), Map.of("A", 2d));
        when(indicadorRepository.findByEntornAppId(entornId)).thenReturn(List.of(indicador("A", CompactacioEnum.SUMA)));
        when(fetRepository.findByEntornAppIdAndTempsDataBefore(eq(entornId), any(LocalDate.class))).thenReturn(List.of(m1, m2));

        // Dades per compactació setmanal (entre llindar setmanal i mensual)
        FetEntity s1 = fet(entornId, ara.minusMonths(8).withDayOfMonth(3), Map.of("D","v2"), Map.of("A", 3d));
        FetEntity s2 = fet(entornId, ara.minusMonths(8).withDayOfMonth(4), Map.of("D","v2"), Map.of("A", 7d));
//        when(fetRepository.findByEntornAppIdAndTempsDataBetween(eq(entornId), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(s1, s2));

        // Act
        helper.compactarTemporalIEsborraPerRetencio(entornApp);

        // Assert
        // Com a mínim s'ha de guardar una fusió (mensual) i eliminar duplicats
        verify(fetRepository, atLeast(1)).save(any(FetEntity.class));
        verify(fetRepository, atLeastOnce()).deleteAllInBatch(anyList());
        verify(fetRepository).deleteByEntornAppIdAndTempsDataBefore(eq(entornId), any(LocalDate.class));
    }

    @Test
    void compactarTemporalIEsborraPerRetencio_entornNoCompactable_noFaRes() {
        // Arrange
        EntornApp noComp = EntornApp.builder()
                .id(99L)
                .compactable(false)
                .build();

        // Act
        helper.compactarTemporalIEsborraPerRetencio(noComp);

        // Assert
        verifyNoInteractions(fetRepository);
        verifyNoInteractions(indicadorRepository);
    }
}
