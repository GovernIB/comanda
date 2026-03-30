package es.caib.comanda.tasques.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.tasques.persist.repository.TascaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TascaSchedulerServiceTest {

    @Mock
    private ParametresHelper parametresHelper;

    @Mock
    private TascaRepository tascaRepository;

    @InjectMocks
    private TascaSchedulerService tascaSchedulerService;

    @Test
    void buidatTasquesScheduler_quanParametresSetejats_esborraTasquesCorrectament() {
        // Arrange
        when(parametresHelper.getParametreEnter(BaseConfig.PROP_TASCA_BORRAT_DIES, 0)).thenReturn(30);
        when(parametresHelper.getParametreEnter(BaseConfig.PROP_TASCA_PEND_BORRAT_DIES, 0)).thenReturn(7);
        
        when(tascaRepository.deleteByDataFiBefore(any(LocalDateTime.class))).thenReturn(5);
        when(tascaRepository.deleteByDataIniciBeforeAndDataFiIsNull(any(LocalDateTime.class))).thenReturn(2);

        // Act
        tascaSchedulerService.buidatTasquesScheduler();

        // Assert
        verify(tascaRepository).deleteByDataFiBefore(any(LocalDateTime.class));
        verify(tascaRepository).deleteByDataIniciBeforeAndDataFiIsNull(any(LocalDateTime.class));
    }

    @Test
    void buidatTasquesScheduler_quanParametresNegatiusOZero_noEsborra() {
        // Arrange
        when(parametresHelper.getParametreEnter(BaseConfig.PROP_TASCA_BORRAT_DIES, 0)).thenReturn(-1);
        when(parametresHelper.getParametreEnter(BaseConfig.PROP_TASCA_PEND_BORRAT_DIES, 0)).thenReturn(0);

        // Act
        tascaSchedulerService.buidatTasquesScheduler();

        // Assert
        verify(tascaRepository, never()).deleteByDataFiBefore(any(LocalDateTime.class));
        verify(tascaRepository, never()).deleteByDataIniciBeforeAndDataFiIsNull(any(LocalDateTime.class));
    }

    @Test
    void buidatTasquesScheduler_quanNomésUnParametreSetejat_esborraNomésUn() {
        // Arrange
        when(parametresHelper.getParametreEnter(BaseConfig.PROP_TASCA_BORRAT_DIES, 0)).thenReturn(30);
        when(parametresHelper.getParametreEnter(BaseConfig.PROP_TASCA_PEND_BORRAT_DIES, 0)).thenReturn(0);
        
        when(tascaRepository.deleteByDataFiBefore(any(LocalDateTime.class))).thenReturn(5);

        // Act
        tascaSchedulerService.buidatTasquesScheduler();

        // Assert
        verify(tascaRepository).deleteByDataFiBefore(any(LocalDateTime.class));
        verify(tascaRepository, never()).deleteByDataIniciBeforeAndDataFiIsNull(any(LocalDateTime.class));
    }
}
