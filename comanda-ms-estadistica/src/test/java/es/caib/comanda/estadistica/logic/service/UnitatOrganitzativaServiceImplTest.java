package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UnitatOrganitzativa;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a UnitatOrganitzativaServiceImpl")
class UnitatOrganitzativaServiceImplTest {

    @Mock
    private AclServiceClient aclServiceClient;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @InjectMocks
    private UnitatOrganitzativaServiceImpl unitatOrganitzativaService;

    private static final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
    }

    // ========================================================================
    // 1. TESTOS PER A afterConversion
    // ========================================================================

    @Test
    @DisplayName("afterConversion: assigna correctament denominacio i codiNom")
    void afterConversion_quanEsValida_llavorsAssignaValors() {
        // Arrange
        UnitatOrganitzativaEntity entity = new UnitatOrganitzativaEntity();
        entity.setDenominacioCa("Denominació Test");
        entity.setDenominacioEs("Denominació Test");
        entity.setCodi("Codi Test");

        UnitatOrganitzativa resource = new UnitatOrganitzativa();

        // Act
        unitatOrganitzativaService.afterConversion(entity, resource);

        // Assert
        assertThat(resource.getDenominacio()).isEqualTo("Denominació Test");
        assertThat(resource.getCodiNom()).contains("Codi Test");
        assertThat(resource.getCodiNom()).contains("Denominació Test");
    }

    // ========================================================================
    // 2. TESTOS PER A PermisPerspective.applySingle
    // ========================================================================

    @Test
    @DisplayName("PermisPerspective.applySingle: s'executa sense errors (mètode buit)")
    void permisPerspective_applySingle_quanEsCrida_llavorsNoFaRes() {
        // Arrange
        UnitatOrganitzativaServiceImpl.PermisPerspective perspective = unitatOrganitzativaService.new PermisPerspective();
        UnitatOrganitzativaEntity entity = new UnitatOrganitzativaEntity();
        UnitatOrganitzativa resource = new UnitatOrganitzativa();

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
            perspective.applySingle("CODE", entity, resource)
        );
    }

    // ========================================================================
    // 3. TESTOS PER A PermisPerspective.applyMultiple
    // ========================================================================

    @Test
    @DisplayName("PermisPerspective.applyMultiple: retorna false quan entities és null")
    void permisPerspective_applyMultiple_quanEntitiesSonNull_llavorsRetornaFalse() {
        // Arrange
        UnitatOrganitzativaServiceImpl.PermisPerspective perspective = unitatOrganitzativaService.new PermisPerspective();
        List<UnitatOrganitzativa> resources = Collections.singletonList(new UnitatOrganitzativa());

        // Act
        boolean result = perspective.applyMultiple("CODE", null, resources);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("PermisPerspective.applyMultiple: retorna false quan entities és buida")
    void permisPerspective_applyMultiple_quanEntitiesSonBuides_llavorsRetornaFalse() {
        // Arrange
        UnitatOrganitzativaServiceImpl.PermisPerspective perspective = unitatOrganitzativaService.new PermisPerspective();
        List<UnitatOrganitzativa> resources = Collections.singletonList(new UnitatOrganitzativa());

        // Act
        boolean result = perspective.applyMultiple("CODE", Collections.emptyList(), resources);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("PermisPerspective.applyMultiple: retorna false quan les mides de entities i resources són diferents")
    void permisPerspective_applyMultiple_quanMidesDiferents_llavorsRetornaFalse() {
        // Arrange
        UnitatOrganitzativaServiceImpl.PermisPerspective perspective = unitatOrganitzativaService.new PermisPerspective();
        List<UnitatOrganitzativaEntity> entities = Collections.singletonList(new UnitatOrganitzativaEntity());
        List<UnitatOrganitzativa> resources = Arrays.asList(new UnitatOrganitzativa(), new UnitatOrganitzativa());

        // Act
        boolean result = perspective.applyMultiple("CODE", entities, resources);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("PermisPerspective.applyMultiple: retorna false quan el client ACL retorna un cos null")
    void permisPerspective_applyMultiple_quanAclRetornaNull_llavorsRetornaFalse() {
        // Arrange
        UnitatOrganitzativaServiceImpl.PermisPerspective perspective = unitatOrganitzativaService.new PermisPerspective();

        UnitatOrganitzativaEntity entity = new UnitatOrganitzativaEntity();
        entity.setId(1L);
        List<UnitatOrganitzativaEntity> entities = Collections.singletonList(entity);

        UnitatOrganitzativa resource = new UnitatOrganitzativa();
        List<UnitatOrganitzativa> resources = Collections.singletonList(resource);

        when(aclServiceClient.countAllSidsWithPermission(eq(ResourceType.UNITAT), eq("1"), eq(AUTH_HEADER)))
            .thenReturn(ResponseEntity.ok(null));

        // Act
        boolean result = perspective.applyMultiple("CODE", entities, resources);

        // Assert
        assertThat(result).isFalse();
        assertThat(resource.getNumPermisos()).isEqualTo(0); // getOrDefault retorna 0
    }

    @Test
    @DisplayName("PermisPerspective.applyMultiple: assigna correctament numPermisos i retorna true quan hi ha dades")
    void permisPerspective_applyMultiple_quanAclRetornaDades_llavorsAssignaNumPermisosIRetornaTrue() {
        // Arrange
        UnitatOrganitzativaServiceImpl.PermisPerspective perspective = unitatOrganitzativaService.new PermisPerspective();

        UnitatOrganitzativaEntity entity1 = new UnitatOrganitzativaEntity();
        entity1.setId(1L);
        UnitatOrganitzativaEntity entity2 = new UnitatOrganitzativaEntity();
        entity2.setId(2L);
        List<UnitatOrganitzativaEntity> entities = Arrays.asList(entity1, entity2);

        UnitatOrganitzativa resource1 = new UnitatOrganitzativa();
        UnitatOrganitzativa resource2 = new UnitatOrganitzativa();
        List<UnitatOrganitzativa> resources = Arrays.asList(resource1, resource2);

        Map<Serializable, Integer> countsMap = new HashMap<>();
        countsMap.put("1", 5);
        countsMap.put("2", 10);

        when(aclServiceClient.countAllSidsWithPermission(eq(ResourceType.UNITAT), eq("1,2"), eq(AUTH_HEADER)))
            .thenReturn(ResponseEntity.ok(countsMap));

        // Act
        boolean result = perspective.applyMultiple("CODE", entities, resources);

        // Assert
        assertThat(result).isTrue();
        assertThat(resource1.getNumPermisos()).isEqualTo(5);
        assertThat(resource2.getNumPermisos()).isEqualTo(10);
        verify(aclServiceClient, times(1)).countAllSidsWithPermission(eq(ResourceType.UNITAT), eq("1,2"), eq(AUTH_HEADER));
    }

    @Test
    @DisplayName("PermisPerspective.applyMultiple: assigna 0 a numPermisos quan l'ID no està al mapa retornat")
    void permisPerspective_applyMultiple_quanIdNoEstaAlMapa_llavorsAssignaZero() {
        // Arrange
        UnitatOrganitzativaServiceImpl.PermisPerspective perspective = unitatOrganitzativaService.new PermisPerspective();

        UnitatOrganitzativaEntity entity = new UnitatOrganitzativaEntity();
        entity.setId(99L);
        List<UnitatOrganitzativaEntity> entities = Collections.singletonList(entity);

        UnitatOrganitzativa resource = new UnitatOrganitzativa();
        List<UnitatOrganitzativa> resources = Collections.singletonList(resource);

        Map<Serializable, Integer> countsMap = new HashMap<>();
        countsMap.put("1", 5);

        when(aclServiceClient.countAllSidsWithPermission(eq(ResourceType.UNITAT), eq("99"), eq(AUTH_HEADER)))
            .thenReturn(ResponseEntity.ok(countsMap));

        // Act
        boolean result = perspective.applyMultiple("CODE", entities, resources);

        // Assert
        assertThat(result).isTrue(); // El mapa no és buit, per tant retorna true
        assertThat(resource.getNumPermisos()).isEqualTo(0); // getOrDefault retorna 0
    }
}
