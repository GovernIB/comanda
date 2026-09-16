package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorTaulaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EstadisticaNetejaServiceImpl")
class EstadisticaNetejaServiceImplTest {

    @Mock private FetRepository fetRepository;
    @Mock private IndicadorTaulaRepository indicadorTaulaRepository;
    @Mock private IndicadorRepository indicadorRepository;
    @Mock private DimensioValorRepository dimensioValorRepository;
    @Mock private DimensioRepository dimensioRepository;

    @InjectMocks
    private EstadisticaNetejaServiceImpl estadisticaNetejaService;

    @Test
    @DisplayName("netejaPerEntornApp: neteja taules estadístiques sense esborrar widgets d'app")
    void netejaPerEntornApp_quanEntornAppId_llavorsNetejaTaulesEstadistiquesSenseEsborrarWidgets() {
        Long entornAppId = 42L;

        estadisticaNetejaService.netejaPerEntornApp(entornAppId);

        InOrder inOrder = inOrder(indicadorTaulaRepository, fetRepository, indicadorRepository, dimensioValorRepository, dimensioRepository);
        inOrder.verify(indicadorTaulaRepository).deleteByIndicadorEntornAppId(entornAppId);
        inOrder.verify(fetRepository).deleteByEntornAppId(entornAppId);
        inOrder.verify(indicadorRepository).deleteByEntornAppId(entornAppId);
        inOrder.verify(dimensioValorRepository).deleteByDimensioEntornAppId(entornAppId);
        inOrder.verify(dimensioRepository).deleteByEntornAppId(entornAppId);
        verifyNoMoreInteractions(indicadorTaulaRepository, fetRepository, indicadorRepository, dimensioValorRepository, dimensioRepository);
    }
}
