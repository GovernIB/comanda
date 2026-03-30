package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a IndicadorTaulaServiceImpl")
class IndicadorTaulaServiceImplTest {

    @InjectMocks
    private IndicadorTaulaServiceImpl indicadorTaulaService;

    @Test
    @DisplayName("El servei s'inicialitza correctament")
    void servei_inicialitzatCorrectament() {
        // Assert
        assertThat(indicadorTaulaService).isNotNull();
    }
}
