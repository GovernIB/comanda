package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.mapper.FetMapper;
import es.caib.comanda.estadistica.logic.mapper.TempsMapper;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.TempsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a FetServiceImpl")
class FetServiceImplTest {

    @Mock
    private EstadisticaHelper estadisticaHelper;
    @Mock
    private ConsultaEstadisticaHelper consultaEstadisticaHelper;
    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;
    @Mock
    private TempsRepository tempsRepository;
    @Mock
    private FetRepository fetRepository;
    @Mock
    private TempsMapper tempsMapper;
    @Mock
    private FetMapper fetMapper;

    @InjectMocks
    private FetServiceImpl fetService;

    @Test
    @DisplayName("obtenirFets delega correctament a estadisticaHelper")
    void obtenirFets_quanIdValida_cridaHelper() {
        // Arrange
        Long entornAppId = 1L;
        EntornApp entornApp = new EntornApp();
        when(estadisticaClientHelper.entornAppFindById(entornAppId)).thenReturn(entornApp);

        // Act
        fetService.obtenirFets(entornAppId);

        // Assert
        verify(estadisticaHelper).getEstadisticaInfoDades(entornApp);
    }

    @Test
    @DisplayName("obtenirFets amb dies delega correctament a estadisticaHelper")
    void obtenirFetsAmbDies_quanParametresValids_cridaHelper() {
        // Arrange
        Long entornAppId = 1L;
        int dies = 5;
        EntornApp entornApp = new EntornApp();
        when(estadisticaClientHelper.entornAppFindById(entornAppId)).thenReturn(entornApp);

        // Act
        fetService.obtenirFets(entornAppId, dies);

        // Assert
        verify(estadisticaHelper).getEstadisticaInfoDades(entornApp, dies);
    }

    @Test
    @DisplayName("getEstadistiquesPeriode delega correctament a consultaEstadisticaHelper")
    void getEstadistiquesPeriode_quanParametresValids_retornaLlista() {
        // Arrange
        Long entornAppId = 1L;
        LocalDate inici = LocalDate.now().minusDays(7);
        LocalDate fi = LocalDate.now();
        List<Fet> fets = new ArrayList<>();
        fets.add(new Fet());
        when(consultaEstadisticaHelper.getEstadistiquesPeriode(entornAppId, inici, fi)).thenReturn(fets);

        // Act
        List<Fet> result = fetService.getEstadistiquesPeriode(entornAppId, inici, fi);

        // Assert
        assertThat(result).isEqualTo(fets);
        verify(consultaEstadisticaHelper).getEstadistiquesPeriode(entornAppId, inici, fi);
    }

    @Test
    @DisplayName("getEstadistiquesPeriodeAmbDimensions delega correctament a consultaEstadisticaHelper")
    void getEstadistiquesPeriodeAmbDimensions_quanParametresValids_retornaLlista() {
        // Arrange
        Long entornAppId = 1L;
        LocalDate inici = LocalDate.now().minusDays(7);
        LocalDate fi = LocalDate.now();
        var filtre = Collections.<String, List<String>>emptyMap();
        List<Fet> fets = new ArrayList<>();
        when(consultaEstadisticaHelper.getEstadistiquesPeriodeAmbDimensions(entornAppId, inici, fi, filtre)).thenReturn(fets);

        // Act
        List<Fet> result = fetService.getEstadistiquesPeriodeAmbDimensions(entornAppId, inici, fi, filtre);

        // Assert
        assertThat(result).isEqualTo(fets);
        verify(consultaEstadisticaHelper).getEstadistiquesPeriodeAmbDimensions(entornAppId, inici, fi, filtre);
    }
}
