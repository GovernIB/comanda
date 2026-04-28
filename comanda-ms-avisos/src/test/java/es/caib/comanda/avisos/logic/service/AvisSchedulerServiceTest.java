package es.caib.comanda.avisos.logic.service;

import es.caib.comanda.avisos.persist.repository.AvisRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvisSchedulerServiceTest {

    @Mock
    private ParametresHelper parametresHelper;

    @Mock
    private AvisRepository avisRepository;

    @InjectMocks
    private AvisSchedulerService avisSchedulerService;

    @Test
    @DisplayName("buidatAvisScheduler ha d'esborrar avisos si diesBorrar > 0")
    void buidatAvisScheduler_quanDiesPositius_esborraAvisos() {
        // Arrange
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_AVIS_BORRAT_DIES), anyInt())).thenReturn(30);
        when(avisRepository.deleteByLastModifiedDateBefore(any(LocalDateTime.class))).thenReturn(5);

        // Act
        avisSchedulerService.buidatAvisScheduler();

        // Assert
        verify(avisRepository).deleteByLastModifiedDateBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("buidatAvisScheduler no ha d'esborrar avisos si diesBorrar <= 0")
    void buidatAvisScheduler_quanDiesZero_noFaRes() {
        // Arrange
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_AVIS_BORRAT_DIES), anyInt())).thenReturn(0);

        // Act
        avisSchedulerService.buidatAvisScheduler();

        // Assert
        verify(avisRepository, never()).deleteByLastModifiedDateBefore(any(LocalDateTime.class));
    }
}
