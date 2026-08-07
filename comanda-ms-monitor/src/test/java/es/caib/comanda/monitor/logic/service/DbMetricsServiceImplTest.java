package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.*;
import es.caib.comanda.monitor.persist.repository.DbMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DbMetricsServiceImpl")
class DbMetricsServiceImplTest {

    @Mock
    private DbMetricsRepository repository;

    @InjectMocks
    private DbMetricsServiceImpl dbMetricsService;

    @BeforeEach
    void setUp() {
        // Inicialitzem amb llistes buides per defecte per evitar NPE en tests aïllats
        lenient().when(repository.findTableStorage()).thenReturn(Collections.emptyList());
        lenient().when(repository.findTablespaces()).thenReturn(Collections.emptyList());
        lenient().when(repository.findIndexos()).thenReturn(Collections.emptyList());
        lenient().when(repository.findSegmentStats()).thenReturn(Collections.emptyList());
        lenient().when(repository.findSessions()).thenReturn(Collections.emptyList());
        lenient().when(repository.findTopSql()).thenReturn(Collections.emptyList());
        lenient().when(repository.findHitRatioCache()).thenReturn(95.5);
        lenient().when(repository.findBloqueigs()).thenReturn(Collections.emptyList());
    }

    // ========================================================================
    // 1. TESTOS PER A MÈTODES DE RECOL·LECCIÓ (@Scheduled)
    // ========================================================================

    @Test
    @DisplayName("collectStorage: actualitza les cachés d'emmagatzematge, tablespaces i índexs")
    void collectStorage_quanEsCrida_llavorsActualitzaCachesCorresponents() {
        // Arrange
        List<TaulaStorageDto> storage = List.of(new TaulaStorageDto());
        List<TablespaceDto> tablespaces = List.of(new TablespaceDto());
        List<IndexDto> indexos = List.of(new IndexDto());

        when(repository.findTableStorage()).thenReturn(storage);
        when(repository.findTablespaces()).thenReturn(tablespaces);
        when(repository.findIndexos()).thenReturn(indexos);

        // Act
        dbMetricsService.collectStorage();

        // Assert
        assertThat(dbMetricsService.getStorage()).isSameAs(storage);
        assertThat(dbMetricsService.getTablespaces()).isSameAs(tablespaces);
        assertThat(dbMetricsService.getIndexos()).isSameAs(indexos);
        verify(repository, times(1)).findTableStorage();
        verify(repository, times(1)).findTablespaces();
        verify(repository, times(1)).findIndexos();
    }

    @Test
    @DisplayName("collectActivity: actualitza les cachés d'activitat i la marca de temps")
    void collectActivity_quanEsCrida_llavorsActualitzaCachesIUltimaActualitzacio() {
        // Arrange
        List<TaulaActivitatDto> activitat = List.of(new TaulaActivitatDto());
        List<SessionsResumDto> sessions = List.of(new SessionsResumDto());
        List<TopSqlDto> topSql = List.of(new TopSqlDto());

        when(repository.findSegmentStats()).thenReturn(activitat);
        when(repository.findSessions()).thenReturn(sessions);
        when(repository.findTopSql()).thenReturn(topSql);

        // Act
        dbMetricsService.collectActivity();

        // Assert
        assertThat(dbMetricsService.getActivitat()).isSameAs(activitat);
        assertThat(dbMetricsService.getSessions()).isSameAs(sessions);
        assertThat(dbMetricsService.getTopSql()).isSameAs(topSql);
        assertThat(dbMetricsService.getOverview().getUltimaActualitzacio()).isNotNull();
    }

    @Test
    @DisplayName("collectBloqueigs: marca com a disponibles quan la consulta és exitosa")
    void collectBloqueigs_quanConsultaEsExitosa_llavorsMarcaComDisponibles() {
        // Arrange
        List<BloqueigDto> bloqueigs = List.of(new BloqueigDto());
        when(repository.findBloqueigs()).thenReturn(bloqueigs);

        // Act
        dbMetricsService.collectBloqueigs();

        // Assert
        assertThat(dbMetricsService.getBloqueigs()).isSameAs(bloqueigs);
        assertThat(dbMetricsService.getOverview().isBloqueigsDisponibles()).isTrue();
    }

    @Test
    @DisplayName("collectBloqueigs: captura excepció i marca com a no disponibles quan la vista falla")
    void collectBloqueigs_quanConsultaFalla_llavorsMarcaComNoDisponiblesIResetejaLlista() {
        // Arrange
        when(repository.findBloqueigs()).thenThrow(new RuntimeException("Vista no creada"));

        // Act
        dbMetricsService.collectBloqueigs();

        // Assert
        assertThat(dbMetricsService.getBloqueigs()).isEmpty();
        assertThat(dbMetricsService.getOverview().isBloqueigsDisponibles()).isFalse();
    }

    // ========================================================================
    // 2. TESTOS PER A getOverview (Lògica d'Agregació)
    // ========================================================================

    @Test
    @DisplayName("getOverview: calcula correctament els agregats quan hi ha dades completes")
    void getOverview_quanHiHaDadesCompletes_llavorsCalculaAgregatsCorrectament() {
        // Arrange
        SessionsResumDto s1 = new SessionsResumDto();
        s1.setEstat("ACTIVE");
        s1.setQuantitat(5L);
        SessionsResumDto s2 = new SessionsResumDto();
        s2.setEstat("INACTIVE");
        s2.setQuantitat(10L);

        TaulaStorageDto t1 = new TaulaStorageDto();
        t1.setBytesReservats(1000L);

        IndexDto i1 = new IndexDto();
        i1.setStatus("UNUSABLE");
        IndexDto i2 = new IndexDto();
        i2.setStatus("VALID");

        when(repository.findSegmentStats()).thenReturn(List.of(new TaulaActivitatDto()));
        when(repository.findSessions()).thenReturn(List.of(s1, s2));
        when(repository.findTableStorage()).thenReturn(List.of(t1));
        when(repository.findTablespaces()).thenReturn(List.of(new TablespaceDto()));
        when(repository.findIndexos()).thenReturn(List.of(i1, i2));
        when(repository.findHitRatioCache()).thenReturn(98.0);
        when(repository.findTopSql()).thenReturn(List.of(new TopSqlDto()));
        when(repository.findBloqueigs()).thenReturn(List.of(new BloqueigDto()));

        // Forcem una actualització prèvia per tenir dades
        dbMetricsService.collectActivity();
        dbMetricsService.collectStorage();
        dbMetricsService.collectBloqueigs();

        // Act
        DbOverviewDto overview = dbMetricsService.getOverview();

        // Assert
        assertThat(overview.getSessionsActives()).isEqualTo(5L);
        assertThat(overview.getSessionsTotal()).isEqualTo(15L);
        assertThat(overview.getTotalBytesReservats()).isEqualTo(1000L);
        assertThat(overview.getIndexosInvalidsCount()).isEqualTo(1);
        assertThat(overview.getHitRatioCache()).isEqualTo(98.0);
        assertThat(overview.isSessionsDisponibles()).isTrue();
        assertThat(overview.isActivitatDisponible()).isTrue();
        assertThat(overview.isSqlDisponible()).isTrue();
        assertThat(overview.isTablespacesDisponibles()).isTrue();
        assertThat(overview.isBloqueigsDisponibles()).isTrue();
    }

    @Test
    @DisplayName("getOverview: retorna valors per defecte i zero quan no hi ha dades")
    void getOverview_quanNoHiHaDades_llavorsRetornaValorsPerDefecteIZero() {
        // Act
        DbOverviewDto overview = dbMetricsService.getOverview();

        // Assert
        assertThat(overview.getSessionsActives()).isZero();
        assertThat(overview.getSessionsTotal()).isZero();
        assertThat(overview.getTotalBytesReservats()).isZero();
        assertThat(overview.getIndexosInvalidsCount()).isZero();
        assertThat(overview.isSessionsDisponibles()).isFalse();
        assertThat(overview.isBloqueigsDisponibles()).isFalse();
    }

    // ========================================================================
    // 3. TESTOS PER A rebuildIndex
    // ========================================================================

    @Test
    @DisplayName("rebuildIndex: reconstrueix l'índex i actualitza la caché quan és vàlid")
    void rebuildIndex_quanNomEsValidIEsTaulaCom_llavorsReconstrueixIActualitzaCache() {
        // Arrange
        String indexName = "IDX_COM_TEST";
        when(repository.isIndexForComTable(indexName)).thenReturn(true);
        List<IndexDto> updatedIndexos = List.of(new IndexDto());
        when(repository.findIndexos()).thenReturn(updatedIndexos);

        // Act
        dbMetricsService.rebuildIndex(indexName);

        // Assert
        verify(repository, times(1)).rebuildIndex(indexName);
        verify(repository, times(1)).findIndexos();
        assertThat(dbMetricsService.getIndexos()).isSameAs(updatedIndexos);
    }

    @Test
    @DisplayName("rebuildIndex: llança excepció quan el nom de l'índex no compleix el patró")
    void rebuildIndex_quanNomEsInvalid_llavorsLlancaIllegalArgumentException() {
        // Arrange
        String invalidIndexName = "idx-invalido!"; // Conté caràcters no permesos i minúscules

        // Act & Assert
        assertThatThrownBy(() -> dbMetricsService.rebuildIndex(invalidIndexName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nom d'índex no vàlid");

        verify(repository, never()).isIndexForComTable(anyString());
        verify(repository, never()).rebuildIndex(anyString());
    }

    @Test
    @DisplayName("rebuildIndex: llança excepció quan l'índex no pertany a una taula COM_*")
    void rebuildIndex_quanNomEsValidPeroNoEsTaulaCom_llavorsLlancaIllegalArgumentException() {
        // Arrange
        String indexName = "IDX_NO_COM";
        when(repository.isIndexForComTable(indexName)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> dbMetricsService.rebuildIndex(indexName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Índex no pertany a cap taula COM_*");

        verify(repository, times(1)).isIndexForComTable(indexName);
        verify(repository, never()).rebuildIndex(anyString());
    }

    // ========================================================================
    // 4. TESTOS PER A GETTERS (Cobertura explícita)
    // ========================================================================

    @Test
    @DisplayName("getters: retornen l'estat actual de la caché després d'una actualització")
    void getters_quanSInvoquenDespresDActualitzar_llavorsRetornenEstatActual() {
        // Arrange
        List<TaulaStorageDto> storage = List.of(new TaulaStorageDto());
        when(repository.findTableStorage()).thenReturn(storage);
        dbMetricsService.collectStorage();

        // Act & Assert
        assertThat(dbMetricsService.getStorage()).hasSize(1);
        assertThat(dbMetricsService.getActivitat()).isEmpty();
        assertThat(dbMetricsService.getSessions()).isEmpty();
        assertThat(dbMetricsService.getTopSql()).isEmpty();
        assertThat(dbMetricsService.getTablespaces()).isEmpty();
        assertThat(dbMetricsService.getBloqueigs()).isEmpty();
        assertThat(dbMetricsService.getIndexos()).isEmpty();
    }
}
