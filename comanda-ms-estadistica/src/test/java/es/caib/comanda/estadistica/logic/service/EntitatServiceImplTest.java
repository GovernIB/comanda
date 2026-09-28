package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.dir3.SistemaExternException;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPlugin;
import es.caib.comanda.estadistica.logic.helper.EntitatRefreshUOProgressHelper;
import es.caib.comanda.estadistica.logic.helper.UnitatOrganitzativaHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Entitat;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UnitatOrganitzativa;
import es.caib.comanda.estadistica.persist.entity.estadistiques.EntitatEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EntitatServiceImpl")
class EntitatServiceImplTest {

    @Mock
    private UnitatsOrganitzativesPlugin unitatsOrganitzativesPlugin;

    @Mock
    private UnitatOrganitzativaHelper unitatOrganitzativaHelper;

    @Mock
    private EntitatRefreshUOProgressHelper entitatRefreshUOProgressHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @InjectMocks
    private EntitatServiceImpl entitatService;

    private static final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        // Injecció del helper de mapeig que ve de la classe base BaseMutableResourceService
        ReflectionTestUtils.setField(entitatService, "resourceEntityMappingHelper", resourceEntityMappingHelper);

        // Configuració lenient per a crides comunes
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        lenient().when(entitatRefreshUOProgressHelper.tryStart(any())).thenReturn(true);
    }

    // ========================================================================
    // 1. TESTOS PER A PermisPerspective
    // ========================================================================

    @Test
    @DisplayName("PermisPerspective: assigna el nombre de permisos quan l'ACL retorna un valor")
    void permisPerspective_quanAclRetornaValor_llavorsAssignaNumPermisos() {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(10L);
        Entitat resource = new Entitat();

        when(aclServiceClient.countSidsWithPermission(eq(ResourceType.ENTITAT), eq(10L), eq(AUTH_HEADER)))
            .thenReturn(ResponseEntity.ok(5));

        EntitatServiceImpl.PermisPerspective perspective = entitatService.new PermisPerspective();

        // Act
        perspective.applySingle("PERSP_PERMIS_NUM", entity, resource);

        // Assert
        assertThat(resource.getNumPermisos()).isEqualTo(5);
        verify(aclServiceClient, times(1)).countSidsWithPermission(ResourceType.ENTITAT, 10L, AUTH_HEADER);
    }

    @Test
    @DisplayName("PermisPerspective: assigna 0 permisos quan l'ACL retorna un cos nul")
    void permisPerspective_quanAclRetornaNull_llavorsAssignaZeroPermisos() {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(10L);
        Entitat resource = new Entitat();

        when(aclServiceClient.countSidsWithPermission(eq(ResourceType.ENTITAT), eq(10L), eq(AUTH_HEADER)))
            .thenReturn(ResponseEntity.ok(null));

        EntitatServiceImpl.PermisPerspective perspective = entitatService.new PermisPerspective();

        // Act
        perspective.applySingle("PERSP_PERMIS_NUM", entity, resource);

        // Assert
        assertThat(resource.getNumPermisos()).isEqualTo(0);
    }

    // ========================================================================
    // 2. TESTOS PER A RefreshUOActionExecutor
    // ========================================================================

    @Test
    @DisplayName("RefreshUOActionExecutor: executa correctament l'actualització de les unitats organitzatives")
    void refreshUoActionExecutor_quanEsValid_llavorsActualitzaIretornaRecurs() throws ActionExecutionException, SistemaExternException {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(10L);
        entity.setCodiDir3("DIR3_123");

        UnitatOrganitzativaEntity uo = new UnitatOrganitzativaEntity();
        uo.setCodi("UO1");

        Entitat expectedResource = new Entitat();

        when(unitatsOrganitzativesPlugin.findAll("DIR3_123")).thenReturn(Collections.singletonList(uo));
        when(resourceEntityMappingHelper.entityToResource(entity, Entitat.class)).thenReturn(expectedResource);

        EntitatServiceImpl.RefreshUOActionExecutor executor = entitatService.new RefreshUOActionExecutor();

        // Act
        Entitat result = executor.exec(Entitat.ACTION_REFRESH_UO, entity, null);

        // Assert
        verify(unitatsOrganitzativesPlugin, times(1)).findAll("DIR3_123");
        verify(unitatOrganitzativaHelper, times(1)).updateAll(eq(Collections.singletonList(uo)), any());
        verify(unitatOrganitzativaHelper, times(1)).evictOrganigramaCache("DIR3_123");
        verify(resourceEntityMappingHelper, times(1)).entityToResource(entity, Entitat.class);
        assertThat(result).isSameAs(expectedResource);
    }

    @Test
    @DisplayName("RefreshUOActionExecutor: llança ActionExecutionException quan el plugin falla")
    void refreshUoActionExecutor_quanPluginFalla_llancaActionExecutionException() throws SistemaExternException {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(10L);
        entity.setCodiDir3("DIR3_123");

        when(unitatsOrganitzativesPlugin.findAll("DIR3_123")).thenThrow(new RuntimeException("Error de connexió a DIR3"));

        EntitatServiceImpl.RefreshUOActionExecutor executor = entitatService.new RefreshUOActionExecutor();

        // Act & Assert
        assertThatThrownBy(() -> executor.exec(Entitat.ACTION_REFRESH_UO, entity, null))
            .isInstanceOf(ActionExecutionException.class)
            .hasMessageContaining("Error de connexió a DIR3");

        verify(unitatOrganitzativaHelper, never()).updateAll(any(), any());
    }

    @Test
    @DisplayName("RefreshUOActionExecutor: publica el progrés per SSE mentre processa les unitats, inclòs l'estat inicial")
    void refreshUoActionExecutor_quanProcessaUnitats_llavorsPublicaElProgres() throws SistemaExternException {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(7L);
        entity.setCodiDir3("DIR3_123");

        UnitatOrganitzativaEntity uo1 = new UnitatOrganitzativaEntity();
        uo1.setCodi("UO1");
        UnitatOrganitzativaEntity uo2 = new UnitatOrganitzativaEntity();
        uo2.setCodi("UO2");
        List<UnitatOrganitzativaEntity> uoList = Arrays.asList(uo1, uo2);

        when(unitatsOrganitzativesPlugin.findAll("DIR3_123")).thenReturn(uoList);
        when(unitatOrganitzativaHelper.updateAll(eq(uoList), any())).thenAnswer(invocation -> {
            IntConsumer onProgress = invocation.getArgument(1);
            onProgress.accept(1);
            onProgress.accept(2);
            return Collections.emptyList();
        });

        // Act
        entitatService.new RefreshUOActionExecutor().exec(Entitat.ACTION_REFRESH_UO, entity, null);

        // Assert
        verify(entitatRefreshUOProgressHelper).publishProgress(7L, 0, 2);
        verify(entitatRefreshUOProgressHelper).publishProgress(7L, 1, 2);
        verify(entitatRefreshUOProgressHelper).publishProgress(7L, 2, 2);
    }

    @Test
    @DisplayName("RefreshUOActionExecutor: publica l'estat inicial (0 de 0) encara que l'entitat no tingui cap unitat a Dir3, " +
        "perquè la modal de progrés sàpiga que el procés ja ha acabat")
    void refreshUoActionExecutor_quanNoHiHaUnitats_llavorsPublicaEstatInicialAmbTotalZero() throws SistemaExternException {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(7L);
        entity.setCodiDir3("DIR3_123");

        when(unitatsOrganitzativesPlugin.findAll("DIR3_123")).thenReturn(Collections.emptyList());
        when(unitatOrganitzativaHelper.updateAll(eq(Collections.emptyList()), any())).thenReturn(Collections.emptyList());

        // Act
        entitatService.new RefreshUOActionExecutor().exec(Entitat.ACTION_REFRESH_UO, entity, null);

        // Assert
        verify(entitatRefreshUOProgressHelper).publishProgress(7L, 0, 0);
    }

    @Test
    @DisplayName("RefreshUOActionExecutor: quan ja hi ha una execució en curs per a l'entitat, no fa cap feina " +
        "(tryStart ja s'ha encarregat de republicar l'últim progrés per SSE)")
    void refreshUoActionExecutor_quanJaHiHaExecucioEnCurs_llavorsNoFaCapFeina() throws SistemaExternException {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(7L);
        entity.setCodiDir3("DIR3_123");

        when(entitatRefreshUOProgressHelper.tryStart(7L)).thenReturn(false);

        // Act
        entitatService.new RefreshUOActionExecutor().exec(Entitat.ACTION_REFRESH_UO, entity, null);

        // Assert
        verify(unitatsOrganitzativesPlugin, never()).findAll(any());
        verify(unitatOrganitzativaHelper, never()).updateAll(any(), any());
        verify(entitatRefreshUOProgressHelper, never()).publishProgress(any(), anyInt(), anyInt());
        verify(entitatRefreshUOProgressHelper, never()).finish(any());
    }

    @Test
    @DisplayName("RefreshUOActionExecutor: quan és propietària de l'execució i acaba correctament, allibera el registre amb finish")
    void refreshUoActionExecutor_quanAcabaCorrectament_llavorsAlliberaElRegistre() throws SistemaExternException {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(7L);
        entity.setCodiDir3("DIR3_123");

        when(unitatsOrganitzativesPlugin.findAll("DIR3_123")).thenReturn(Collections.emptyList());
        when(unitatOrganitzativaHelper.updateAll(eq(Collections.emptyList()), any())).thenReturn(Collections.emptyList());

        // Act
        entitatService.new RefreshUOActionExecutor().exec(Entitat.ACTION_REFRESH_UO, entity, null);

        // Assert
        verify(entitatRefreshUOProgressHelper).finish(7L);
        verify(entitatRefreshUOProgressHelper, never()).publishError(any());
    }

    @Test
    @DisplayName("RefreshUOActionExecutor: quan és propietària de l'execució i falla, publica l'error per SSE " +
        "i allibera igualment el registre (finally) perquè una propera crida no es quedi bloquejada per sempre")
    void refreshUoActionExecutor_quanFalla_llavorsPublicaErrorIAlliberaElRegistre() throws SistemaExternException {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(7L);
        entity.setCodiDir3("DIR3_123");

        when(unitatsOrganitzativesPlugin.findAll("DIR3_123")).thenThrow(new RuntimeException("Error de connexió a DIR3"));

        EntitatServiceImpl.RefreshUOActionExecutor executor = entitatService.new RefreshUOActionExecutor();

        // Act & Assert
        assertThatThrownBy(() -> executor.exec(Entitat.ACTION_REFRESH_UO, entity, null))
            .isInstanceOf(ActionExecutionException.class);
        verify(entitatRefreshUOProgressHelper).publishError(7L);
        verify(entitatRefreshUOProgressHelper).finish(7L);
    }

    @Test
    @DisplayName("RefreshUOActionExecutor.onChange: s'executa sense errors (mètode buit)")
    void refreshUoActionExecutor_onChange_quanEsCrida_llavorsNoFaRes() {
        // Arrange
        EntitatServiceImpl.RefreshUOActionExecutor executor = entitatService.new RefreshUOActionExecutor();

        // Act & Assert
        assertDoesNotThrow(() ->
            executor.onChange(1L, null, "fieldName", "fieldValue", new HashMap<>(), new String[0], null)
        );
    }

    // ========================================================================
    // 3. TESTOS PER A afterCreate
    // ========================================================================

    @Test
    @DisplayName("afterCreate: sincronitza les unitats organitzatives quan l'entitat té codiDir3")
    void afterCreate_quanEntitatAmbCodiDir3_llavorsSincronitzaUO() {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setCodiDir3("DIR3_999");

        // Act
        entitatService.afterCreate(entity, new Entitat(), new HashMap<>());

        // Assert
        verify(unitatOrganitzativaHelper, times(1)).refreshFromEntitatCodiDir3("DIR3_999");
    }

    @Test
    @DisplayName("afterCreate: crida el mètode de sincronització encara que l'entitat no tingui codiDir3")
    void afterCreate_quanEntitatSenseCodiDir3_llavorsCridaAmbNull() {
        // Arrange
        EntitatEntity entity = new EntitatEntity();

        // Act
        entitatService.afterCreate(entity, new Entitat(), new HashMap<>());

        // Assert - la decisió de no fer res quan és null és responsabilitat de UnitatOrganitzativaHelper
        verify(unitatOrganitzativaHelper, times(1)).refreshFromEntitatCodiDir3(null);
    }

    // ========================================================================
    // 3b. TESTOS PER A beforeUpdateEntity
    // ========================================================================

    @Test
    @DisplayName("beforeUpdateEntity: sincronitza les UO quan s'afegeix el codiDir3 (abans buit)")
    void beforeUpdateEntity_quanSAfegeixCodiDir3_llavorsSincronitzaUO() {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        Entitat resource = new Entitat();
        resource.setCodiDir3("DIR3_NOU");

        // Act
        entitatService.beforeUpdateEntity(entity, resource, new HashMap<>());

        // Assert
        verify(unitatOrganitzativaHelper, times(1)).refreshFromEntitatCodiDir3("DIR3_NOU");
    }

    @Test
    @DisplayName("beforeUpdateEntity: no fa res si l'entitat ja tenia codiDir3")
    void beforeUpdateEntity_quanJaTeniaCodiDir3_llavorsNoFaRes() {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setCodiDir3("DIR3_EXISTENT");
        Entitat resource = new Entitat();
        resource.setCodiDir3("DIR3_NOU");

        // Act
        entitatService.beforeUpdateEntity(entity, resource, new HashMap<>());

        // Assert
        verify(unitatOrganitzativaHelper, never()).refreshFromEntitatCodiDir3(any());
    }

    @Test
    @DisplayName("beforeUpdateEntity: no fa res si el nou codiDir3 també és buit")
    void beforeUpdateEntity_quanNouCodiDir3EsBuit_llavorsNoFaRes() {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        Entitat resource = new Entitat();

        // Act
        entitatService.beforeUpdateEntity(entity, resource, new HashMap<>());

        // Assert
        verify(unitatOrganitzativaHelper, never()).refreshFromEntitatCodiDir3(any());
    }

    // ========================================================================
    // 4. TESTOS PER A OrganigramaActionExecutor
    // ========================================================================

    @Test
    @DisplayName("OrganigramaActionExecutor: retorna l'organigrama construït per UnitatOrganitzativaHelper")
    void organigramaActionExecutor_quanEsValid_llavorsRetornaOrganigrama() throws ActionExecutionException {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setCodiDir3("DIR3_123");

        UnitatOrganitzativa uo = new UnitatOrganitzativa();
        when(unitatOrganitzativaHelper.buildOrganigrama("DIR3_123")).thenReturn(Collections.singletonList(uo));

        EntitatServiceImpl.OrganigramaActionExecutor executor = entitatService.new OrganigramaActionExecutor();

        // Act
        var result = executor.exec(Entitat.ACTION_ORGANIGRAMA, entity, null);

        // Assert
        assertThat(result).containsExactly(uo);
    }

    @Test
    @DisplayName("OrganigramaActionExecutor: llança ActionExecutionException quan falla la construcció de l'organigrama")
    void organigramaActionExecutor_quanFalla_llancaActionExecutionException() {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setCodiDir3("DIR3_123");

        when(unitatOrganitzativaHelper.buildOrganigrama("DIR3_123")).thenThrow(new RuntimeException("Error ACL"));

        EntitatServiceImpl.OrganigramaActionExecutor executor = entitatService.new OrganigramaActionExecutor();

        // Act & Assert
        assertThatThrownBy(() -> executor.exec(Entitat.ACTION_ORGANIGRAMA, entity, null))
            .isInstanceOf(ActionExecutionException.class)
            .hasMessageContaining("Error ACL");
    }

    @Test
    @DisplayName("OrganigramaActionExecutor.onChange: s'executa sense errors (mètode buit)")
    void organigramaActionExecutor_onChange_quanEsCrida_llavorsNoFaRes() {
        // Arrange
        EntitatServiceImpl.OrganigramaActionExecutor executor = entitatService.new OrganigramaActionExecutor();

        // Act & Assert
        assertDoesNotThrow(() ->
            executor.onChange(1L, null, "fieldName", "fieldValue", new HashMap<>(), new String[0], null)
        );
    }
}
