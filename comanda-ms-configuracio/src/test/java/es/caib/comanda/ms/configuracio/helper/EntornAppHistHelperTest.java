package es.caib.comanda.ms.configuracio.helper;

import es.caib.comanda.configuracio.logic.helper.EntornAppHistHelper;
import es.caib.comanda.configuracio.persist.repository.EntornAppHistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaris per a EntornAppHistHelper")
class EntornAppHistHelperTest {

    @Mock
    private EntornAppHistRepository entornAppHistRepository;

    @InjectMocks
    private EntornAppHistHelper helper;

    @Test
    @DisplayName("Ha de cridar al repositori i retornar el nombre de registres eliminats quan n'hi ha")
    void deleteHistoryBeforeDate_AmbRegistres_RetornaComptadorPositiu() {
        // Arrange
        LocalDateTime dataTall = LocalDateTime.of(2023, 10, 1, 0, 0);
        int registresEsperats = 5;
        when(entornAppHistRepository.deleteByDataBefore(dataTall)).thenReturn(registresEsperats);
        // Act
        int resultat = helper.deleteHistoryBeforeDate(dataTall);
        // Assert
        assertEquals(registresEsperats, resultat, "El nombre de registres eliminats ha de coincidir");
        verify(entornAppHistRepository).deleteByDataBefore(dataTall);
    }

    @Test
    @DisplayName("Ha de retornar 0 quan no hi ha registres per eliminar")
    void deleteHistoryBeforeDate_SenseRegistres_RetornaZero() {
        // Arrange
        LocalDateTime dataTall = LocalDateTime.of(2025, 1, 1, 0, 0);
        when(entornAppHistRepository.deleteByDataBefore(dataTall)).thenReturn(0);
        // Act
        int resultat = helper.deleteHistoryBeforeDate(dataTall);
        // Assert
        assertEquals(0, resultat, "No s'haurien d'haver eliminat registres");
        verify(entornAppHistRepository).deleteByDataBefore(dataTall);
    }
}
