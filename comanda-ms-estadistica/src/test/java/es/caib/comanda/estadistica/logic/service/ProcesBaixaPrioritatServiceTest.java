package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet.FetObtenirResponse;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Indicador.SincronitzarCatalegResponse;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.ProcesBaixaPrioritat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a ProcesBaixaPrioritatService")
class ProcesBaixaPrioritatServiceTest {

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @Mock
    private EstadisticaHelper estadisticaHelper;

    @Mock
    private TaskExecutor taskExecutor;

    private ProcesBaixaPrioritatService service;

    private EntornApp entornApp;

    @BeforeEach
    void setUp() {
        service = new ProcesBaixaPrioritatService(estadisticaClientHelper, estadisticaHelper, taskExecutor);
        service.setPausaMs(0); // Sense pauses en tests

        AppRef app = AppRef.builder().nom("App Test").build();
        EntornRef entorn = EntornRef.builder().nom("PRO").build();

        entornApp = new EntornApp();
        entornApp.setId(10L);
        entornApp.setApp(app);
        entornApp.setEntorn(entorn);
        entornApp.setActiva(true);
        entornApp.setEstadisticaUrl("http://localhost:8080/stats");
        entornApp.setEstadisticaInfoUrl("http://localhost:8080/stats/info");
    }

    @Test
    @DisplayName("iniciarProces: crea el procés i l'encola al worker")
    void iniciarProces_quanParametresValids_creaIEncolaProces() {
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        LocalDate inici = LocalDate.of(2026, 3, 1);
        LocalDate fi = LocalDate.of(2026, 3, 5);

        ProcesBaixaPrioritat proces = service.iniciarProces(10L, inici, fi);

        assertThat(proces).isNotNull();
        assertThat(proces.getId()).isNotBlank();
        assertThat(proces.getEntornAppId()).isEqualTo(10L);
        assertThat(proces.getEntornAppNom()).isEqualTo("App Test - PRO");
        assertThat(proces.getTotalDies()).isEqualTo(5);
        assertThat(proces.getDiesProcessats()).isEqualTo(0);
        assertThat(proces.getEstat()).isEqualTo(ProcesBaixaPrioritat.ESTAT_PENDENT);

        verify(taskExecutor, times(1)).execute(any(Runnable.class));
        assertThat(service.getProces(proces.getId())).isPresent();
    }

    @Test
    @DisplayName("iniciarProces: utilitza la pausa personalitzada quan s'especifica")
    void iniciarProces_quanPausaPersonalitzada_assignaPausaAlProces() {
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        LocalDate inici = LocalDate.of(2026, 3, 1);
        LocalDate fi = LocalDate.of(2026, 3, 5);

        ProcesBaixaPrioritat proces = service.iniciarProces(10L, inici, fi, 180000L);

        assertThat(proces).isNotNull();
        assertThat(proces.getPausaMs()).isEqualTo(180000L);
    }

    @Test
    @DisplayName("iniciarProces: processa les dates en ordre descendent (les més recents primer)")
    void iniciarProces_processaDatesEnOrdreDescendent() {
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(entornApp);
        FetObtenirResponse response = FetObtenirResponse.builder().success(true).build();
        when(estadisticaHelper.getEstadisticaDadesAmbUrl(eq(entornApp), anyString(), eq(false)))
                .thenReturn(response);

        org.mockito.ArgumentCaptor<Runnable> runnableCaptor = org.mockito.ArgumentCaptor.forClass(Runnable.class);

        LocalDate inici = LocalDate.of(2026, 3, 1);
        LocalDate fi = LocalDate.of(2026, 3, 3);

        service.iniciarProces(10L, inici, fi);

        verify(taskExecutor, atLeastOnce()).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        org.mockito.ArgumentCaptor<String> urlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(estadisticaHelper, times(3)).getEstadisticaDadesAmbUrl(eq(entornApp), urlCaptor.capture(), eq(false));

        List<String> cridesUrls = urlCaptor.getAllValues();
        assertThat(cridesUrls).hasSize(3);
        assertThat(cridesUrls.get(0)).contains("03-03-2026");
        assertThat(cridesUrls.get(1)).contains("02-03-2026");
        assertThat(cridesUrls.get(2)).contains("01-03-2026");
    }

    @Test
    @DisplayName("iniciarProces: llança excepció quan dataFi és anterior a dataInici")
    void iniciarProces_quanDataFiAnterior_llancaExcepcio() {
        LocalDate inici = LocalDate.of(2026, 3, 5);
        LocalDate fi = LocalDate.of(2026, 3, 1);

        assertThatThrownBy(() -> service.iniciarProces(10L, inici, fi))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dataFi no pot ser anterior a dataInici");
    }

    @Test
    @DisplayName("iniciarProces: llança excepció quan l'entornApp no existeix")
    void iniciarProces_quanEntornAppNoExisteix_llancaExcepcio() {
        when(estadisticaClientHelper.entornAppFindById(99L)).thenReturn(null);

        LocalDate inici = LocalDate.of(2026, 3, 1);
        LocalDate fi = LocalDate.of(2026, 3, 5);

        assertThatThrownBy(() -> service.iniciarProces(99L, inici, fi))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existeix");
    }

    @Test
    @DisplayName("iniciarProces: llança excepció quan l'entornApp està inactiu")
    void iniciarProces_quanEntornAppInactiu_llancaExcepcio() {
        entornApp.setActiva(false);
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        LocalDate inici = LocalDate.of(2026, 3, 1);
        LocalDate fi = LocalDate.of(2026, 3, 5);

        assertThatThrownBy(() -> service.iniciarProces(10L, inici, fi))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no està actiu");
    }

    @Test
    @DisplayName("iniciarProces: llança excepció quan l'entornApp no té estadisticaUrl")
    void iniciarProces_quanSenseEstadisticaUrl_llancaExcepcio() {
        entornApp.setEstadisticaUrl(null);
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        LocalDate inici = LocalDate.of(2026, 3, 1);
        LocalDate fi = LocalDate.of(2026, 3, 5);

        assertThatThrownBy(() -> service.iniciarProces(10L, inici, fi))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no té URL d'estadística configurada");
    }

    @Test
    @DisplayName("executarProces: itera dia a dia i actualitza progrés")
    void executarProces_quanExecuta_iteraDiaADiaIActualitzaProgres() {
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        FetObtenirResponse response = FetObtenirResponse.builder()
                .success(true)
                .diesAmbDades(Map.of("2026-03-01", true, "2026-03-02", true))
                .build();
        when(estadisticaHelper.getEstadisticaDadesAmbUrl(eq(entornApp), anyString(), eq(false)))
                .thenReturn(response);

        LocalDate inici = LocalDate.of(2026, 3, 1);
        LocalDate fi = LocalDate.of(2026, 3, 2);

        ProcesBaixaPrioritat proces = ProcesBaixaPrioritat.builder()
                .id("test-proc")
                .entornAppId(10L)
                .dataInici(inici)
                .dataFi(fi)
                .totalDies(2)
                .build();

        service.executarProces(proces, List.of(inici, fi));

        assertThat(proces.getEstat()).isEqualTo(ProcesBaixaPrioritat.ESTAT_FINALITZAT);
        assertThat(proces.getDiesProcessats()).isEqualTo(2);
        assertThat(proces.getPercentatge()).isEqualTo(100);
        assertThat(proces.getDiesAmbDades()).isEqualTo(2);
        assertThat(proces.getDiesAmbErrors()).isEqualTo(0);
        assertThat(proces.getDataFiExecucio()).isNotNull();

        verify(estadisticaHelper, times(1)).sincronitzarEstadisticaInfo(eq(entornApp));
        verify(estadisticaHelper, times(2)).getEstadisticaDadesAmbUrl(eq(entornApp), anyString(), eq(false));
    }

    @Test
    @DisplayName("executarProces: quan es cancel·la s'atura")
    void executarProces_quanCancelat_sAtura() {
        LocalDate d1 = LocalDate.of(2026, 3, 1);
        LocalDate d2 = LocalDate.of(2026, 3, 2);

        ProcesBaixaPrioritat proces = ProcesBaixaPrioritat.builder()
                .id("test-proc")
                .entornAppId(10L)
                .dataInici(d1)
                .dataFi(d2)
                .totalDies(2)
                .build();

        // Marquem com a cancel·lat directament
        proces.setCancelled(true);

        service.executarProces(proces, List.of(d1, d2));

        assertThat(proces.getEstat()).isEqualTo(ProcesBaixaPrioritat.ESTAT_CANCELAT);
        verify(estadisticaHelper, times(0)).getEstadisticaDadesAmbUrl(any(), anyString(), anyBoolean());
        verify(estadisticaHelper, times(0)).sincronitzarEstadisticaInfo(any());
    }

    @Test
    @DisplayName("cancelarProces: cancel·la el procés actiu")
    void cancelarProces_quanExisteix_retornaTrueICancela() {
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        ProcesBaixaPrioritat proces = service.iniciarProces(10L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2));

        boolean cancelat = service.cancelarProces(proces.getId());

        assertThat(cancelat).isTrue();
        assertThat(proces.getEstat()).isEqualTo(ProcesBaixaPrioritat.ESTAT_CANCELAT);
        assertThat(proces.isCancelled()).isTrue();
    }

    @Test
    @DisplayName("getProcessos: filtra per entornAppId i retorna ordenats")
    void getProcessos_filtraPerEntornAppId() {
        when(estadisticaClientHelper.entornAppFindById(anyLong())).thenReturn(entornApp);

        service.iniciarProces(10L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1));
        service.iniciarProces(20L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1));

        List<ProcesBaixaPrioritat> tots = service.getProcessos(null);
        assertThat(tots).hasSize(2);

        List<ProcesBaixaPrioritat> només10 = service.getProcessos(10L);
        assertThat(només10).hasSize(1);
        assertThat(només10.get(0).getEntornAppId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("executarProces: si el procés està cancel·lat abans d'iniciar no s'executa")
    void executarProces_quanJaCancelatAbansDIniciar_noExecutaIConfiguraEstat() {
        ProcesBaixaPrioritat proces = ProcesBaixaPrioritat.builder()
                .id("proc-pre-cancelled")
                .entornAppId(10L)
                .cancelled(true)
                .estat(ProcesBaixaPrioritat.ESTAT_PENDENT)
                .totalDies(2)
                .build();

        service.executarProces(proces, List.of(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2)));

        assertThat(proces.getEstat()).isEqualTo(ProcesBaixaPrioritat.ESTAT_CANCELAT);
        verify(estadisticaClientHelper, times(0)).entornAppFindById(anyLong());
        verify(estadisticaHelper, times(0)).getEstadisticaDadesAmbUrl(any(), anyString(), anyBoolean());
        verify(estadisticaHelper, times(0)).sincronitzarEstadisticaInfo(any());
    }

    @Test
    @DisplayName("executarProces: quan hi ha errors parcials indica el nombre d'errors al missatge")
    void executarProces_quanErrorsParcials_indicaErrorsAlMissatge() {
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        FetObtenirResponse okResponse = FetObtenirResponse.builder().success(true).build();
        FetObtenirResponse koResponse = FetObtenirResponse.builder().success(false).message("Error").build();

        LocalDate d1 = LocalDate.of(2026, 3, 1);
        LocalDate d2 = LocalDate.of(2026, 3, 2);

        when(estadisticaHelper.getEstadisticaDadesAmbUrl(eq(entornApp), eq("http://localhost:8080/stats/of/01-03-2026"), eq(false)))
                .thenReturn(okResponse);
        when(estadisticaHelper.getEstadisticaDadesAmbUrl(eq(entornApp), eq("http://localhost:8080/stats/of/02-03-2026"), eq(false)))
                .thenReturn(koResponse);

        ProcesBaixaPrioritat proces = ProcesBaixaPrioritat.builder()
                .id("test-partial-errors")
                .entornAppId(10L)
                .dataInici(d1)
                .dataFi(d2)
                .totalDies(2)
                .build();

        service.executarProces(proces, List.of(d1, d2));

        assertThat(proces.getEstat()).isEqualTo(ProcesBaixaPrioritat.ESTAT_FINALITZAT);
        assertThat(proces.getDiesProcessats()).isEqualTo(2);
        assertThat(proces.getDiesAmbErrors()).isEqualTo(1);
        assertThat(proces.getMissatge()).contains("1 dies amb error");
    }

    @Test
    @DisplayName("iniciarProces: limita la capacitat màxima de processos històrics")
    void iniciarProces_quanSuperaCapacitat_eliminaMesAntics() {
        when(estadisticaClientHelper.entornAppFindById(anyLong())).thenReturn(entornApp);

        // Creem 102 processos i els marquem com a finalitzats
        for (int i = 0; i < 102; i++) {
            ProcesBaixaPrioritat p = service.iniciarProces(10L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1));
            p.setEstat(ProcesBaixaPrioritat.ESTAT_FINALITZAT);
        }

        List<ProcesBaixaPrioritat> restants = service.getProcessos(null);
        assertThat(restants.size()).isLessThanOrEqualTo(100);
    }

    @Test
    @DisplayName("executarProces: quan entornApp està inactiu marca estat com a ERROR")
    void executarProces_quanEntornAppInactiu_marcaError() {
        entornApp.setActiva(false);
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        ProcesBaixaPrioritat proces = ProcesBaixaPrioritat.builder()
                .id("test-inactiu")
                .entornAppId(10L)
                .dataInici(LocalDate.of(2026, 3, 1))
                .dataFi(LocalDate.of(2026, 3, 2))
                .totalDies(2)
                .build();

        service.executarProces(proces, List.of(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2)));

        assertThat(proces.getEstat()).isEqualTo(ProcesBaixaPrioritat.ESTAT_ERROR);
        assertThat(proces.getMissatge()).contains("no està actiu");
        verify(estadisticaHelper, never()).getEstadisticaDadesAmbUrl(any(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("executarProces: quan cancel·lat durant la sincronització de catàleg no processa els dies")
    void executarProces_quanCancelatDurantSincronitzacioCataleg_noProcessaDies() {
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        ProcesBaixaPrioritat proces = ProcesBaixaPrioritat.builder()
                .id("test-cancel-sync")
                .entornAppId(10L)
                .dataInici(LocalDate.of(2026, 3, 1))
                .dataFi(LocalDate.of(2026, 3, 2))
                .totalDies(2)
                .build();

        doAnswer(invocation -> {
            proces.setCancelled(true);
            return SincronitzarCatalegResponse.builder().success(true).build();
        }).when(estadisticaHelper).sincronitzarEstadisticaInfo(entornApp);

        service.executarProces(proces, List.of(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2)));

        assertThat(proces.getEstat()).isEqualTo(ProcesBaixaPrioritat.ESTAT_CANCELAT);
        assertThat(proces.getMissatge()).isEqualTo("Procés cancel·lat per l'usuari");
        verify(estadisticaHelper, never()).getEstadisticaDadesAmbUrl(any(), anyString(), anyBoolean());
    }
}
