package es.caib.comanda.alarmes.logic.service;

import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventPublisher;
import es.caib.comanda.alarmes.logic.helper.AlarmaComprovacioHelper;
import es.caib.comanda.alarmes.logic.helper.AlarmaMailHelper;
import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaConfigRepository;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import es.caib.comanda.alarmes.logic.intf.model.Alarma.AlarmaReduidaResource;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService.ReportGenerator;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
class AlarmaServiceImplTest {

    @Mock
    private AlarmaComprovacioHelper alarmaComprovacioHelper;
    @Mock
    private AlarmaConfigRepository alarmaConfigRepository;
    @Mock
    private AlarmaMailHelper alarmaMailHelper;
    @Mock
    private AuthenticationHelper authenticationHelper;
    @Mock
    private AlarmaRepository alarmaRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private ComandaSseEventPublisher comandaSseEventPublisher;

    @InjectMocks
    private AlarmaServiceImpl alarmaService;

    private static final String CURRENT_USER = "user1";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(alarmaService, "entityRepository", alarmaRepository);
        ReflectionTestUtils.setField(alarmaService, "schedulerBack", false);
    }

    @Test
    @DisplayName("comprovacioScheduledTask executa comprovacions si és líder")
    void comprovacioScheduledTask_quanLeader_executaComprovacions() {
        // Arrange
        ReflectionTestUtils.setField(alarmaService, "schedulerLeader", true);
        ReflectionTestUtils.setField(alarmaService, "schedulerBack", true);
        AlarmaConfigEntity config = new AlarmaConfigEntity();
        when(alarmaConfigRepository.findAllByEsborratFalse()).thenReturn(Arrays.asList(config));
        when(alarmaComprovacioHelper.comprovar(config)).thenReturn(true);

        // Act
        alarmaService.comprovacioScheduledTask();

        // Assert
        verify(alarmaConfigRepository).findAllByEsborratFalse();
        verify(alarmaComprovacioHelper).comprovar(config);
    }

    @Test
    @DisplayName("comprovacioScheduledTask no fa res si no és líder")
    void comprovacioScheduledTask_quanNoLeader_noFaRes() {
        // Arrange
        ReflectionTestUtils.setField(alarmaService, "schedulerLeader", false);

        // Act
        alarmaService.comprovacioScheduledTask();

        // Assert
        verifyNoInteractions(alarmaConfigRepository);
    }

    @Test
    @DisplayName("enviamentsAgrupatsScheduledTask envia correus si és líder")
    void enviamentsAgrupatsScheduledTask_quanLeader_enviaCorreus() {
        // Arrange
        ReflectionTestUtils.setField(alarmaService, "schedulerLeader", true);
        ReflectionTestUtils.setField(alarmaService, "schedulerBack", true);

        // Act
        alarmaService.enviamentsAgrupatsScheduledTask();

        // Assert
        verify(alarmaMailHelper).sendAlarmesAgrupades();
    }

    @Test
    @DisplayName("additionalSpringFilter retorna filtre correcte per a usuari normal")
    void additionalSpringFilter_quanUsuari_retornaFiltreRestringit() {
        // Arrange
        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        // Act
        String result = alarmaService.additionalSpringFilter("", new String[0]);

        // Assert
        assertThat(result).contains("alarmaConfig.admin:false");
        assertThat(result).contains("alarmaConfig.createdBy:'" + CURRENT_USER + "'");
    }

    @Test
    @DisplayName("additionalSpringFilter retorna filtre correcte per a administrador")
    void additionalSpringFilter_quanAdmin_retornaFiltreAmpli() {
        // Arrange
        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        // Act
        String result = alarmaService.additionalSpringFilter("", new String[0]);

        // Assert
        assertThat(result).contains("alarmaConfig.admin:true");
        assertThat(result).contains("alarmaConfig.admin:false");
    }

    @Test
    @DisplayName("EsborrarActionExecutor canvia estat a ESBORRADA si l'alarma està ACTIVA")
    void esborrarActionExecutor_quanActiva_canviaEstat() {
        // Arrange
        AlarmaEntity entity = crearAlarmaEntity(AlarmaEstat.ACTIVA, false, CURRENT_USER);

        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        AlarmaServiceImpl.EsborrarActionExecutor executor = new AlarmaServiceImpl.EsborrarActionExecutor(authenticationHelper, alarmaRepository);

        // Act
        executor.exec(Alarma.ESBORRAR_ACTION, entity, null);

        // Assert
        assertThat(entity.getEstat()).isEqualTo(AlarmaEstat.ESBORRADA);
        assertThat(entity.getDataEsborrat()).isNotNull();
    }

    @Test
    @DisplayName("EsborrarActionExecutor llança excepció si l'alarma no està ACTIVA")
    void esborrarActionExecutor_quanNoActiva_llancaExcepcio() {
        // Arrange
        AlarmaEntity entity = crearAlarmaEntity(AlarmaEstat.ESBORRANY, false, CURRENT_USER);

        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        AlarmaServiceImpl.EsborrarActionExecutor executor = new AlarmaServiceImpl.EsborrarActionExecutor(authenticationHelper, alarmaRepository);

        // Act & Assert
        assertThatThrownBy(() -> executor.exec(Alarma.ESBORRAR_ACTION, entity, null))
                .isInstanceOf(ActionExecutionException.class)
                .hasMessageContaining("Només es poden esborrar alarmes actives");
    }

    @Test
    @DisplayName("ReactivarActionExecutor canvia estat a ACTIVA si l'alarma està ESBORRADA")
    void reactivarActionExecutor_quanEsborrada_canviaEstat() {
        // Arrange
        AlarmaEntity entity = crearAlarmaEntity(AlarmaEstat.ESBORRADA, false, CURRENT_USER);

        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        AlarmaServiceImpl.ReactivarActionExecutor executor = new AlarmaServiceImpl.ReactivarActionExecutor(authenticationHelper);

        // Act
        executor.exec(Alarma.REACTIVAR_ACTION, entity, null);

        // Assert
        assertThat(entity.getEstat()).isEqualTo(AlarmaEstat.ACTIVA);
        assertThat(entity.getDataEsborrat()).isNull();
    }

    @Test
    @DisplayName("ReactivarActionExecutor llança excepció si l'usuari no té permisos")
    void reactivarActionExecutor_quanSensePermisos_llancaExcepcio() {
        // Arrange
        AlarmaEntity entity = crearAlarmaEntity(AlarmaEstat.ESBORRADA, true, "altre_usuari");

        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        AlarmaServiceImpl.ReactivarActionExecutor executor = new AlarmaServiceImpl.ReactivarActionExecutor(authenticationHelper);

        // Act & Assert
        assertThatThrownBy(() -> executor.exec(Alarma.REACTIVAR_ACTION, entity, null))
                .isInstanceOf(ActionExecutionException.class)
                .hasMessageContaining("Sense permisos per a reactivar l'alarma");
    }

    @Test
    @DisplayName("ReactivarActionExecutor llança excepció si l'alarma no està ESBORRADA")
    void reactivarActionExecutor_quanNoEsborrada_llancaExcepcio() {
        // Arrange
        AlarmaEntity entity = crearAlarmaEntity(AlarmaEstat.ACTIVA, false, CURRENT_USER);

        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        AlarmaServiceImpl.ReactivarActionExecutor executor = new AlarmaServiceImpl.ReactivarActionExecutor(authenticationHelper);

        // Act & Assert
        assertThatThrownBy(() -> executor.exec(Alarma.REACTIVAR_ACTION, entity, null))
                .isInstanceOf(ActionExecutionException.class)
                .hasMessageContaining("Només es poden reactivar alarmes esborrades");
    }

    @Test
    @DisplayName("ReactivarActionExecutor: admin pot reactivar alarma admin")
    void reactivarActionExecutor_adminReactivaAlarmaAdmin_permesConcedit() {
        // Arrange
        AlarmaEntity entity = crearAlarmaEntity(AlarmaEstat.ESBORRADA, true, "altre_usuari");

        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        AlarmaServiceImpl.ReactivarActionExecutor executor =
                new AlarmaServiceImpl.ReactivarActionExecutor(authenticationHelper);

        // Act
        Serializable result = executor.exec(Alarma.REACTIVAR_ACTION, entity, null);

        // Assert
        assertThat(entity.getEstat()).isEqualTo(AlarmaEstat.ACTIVA);
        assertThat(entity.getDataEsborrat()).isNull();
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("ReactivarActionExecutor: usuari pot reactivar la seva pròpia alarma no-admin")
    void reactivarActionExecutor_usuariReactivaSevaAlarmaNoAdmin_permesConcedit() {
        // Arrange
        AlarmaEntity entity = crearAlarmaEntity(AlarmaEstat.ESBORRADA, false, CURRENT_USER);

        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        AlarmaServiceImpl.ReactivarActionExecutor executor =
                new AlarmaServiceImpl.ReactivarActionExecutor(authenticationHelper);

        // Act
        Serializable result = executor.exec(Alarma.REACTIVAR_ACTION, entity, null);

        // Assert
        assertThat(entity.getEstat()).isEqualTo(AlarmaEstat.ACTIVA);
        assertThat(entity.getDataEsborrat()).isNull();
    }

    @Test
    @DisplayName("ReactivarActionExecutor: entitat null no llança excepció")
    void reactivarActionExecutor_entitatNull_noLlancaExcepcio() {
        // Arrange
        AlarmaServiceImpl.ReactivarActionExecutor executor =
                new AlarmaServiceImpl.ReactivarActionExecutor(authenticationHelper);

        // Act & Assert
        assertThatCode(() -> executor.exec(Alarma.REACTIVAR_ACTION, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ReactivarActionExecutor: codi d'acció desconegut no modifica entitat")
    void reactivarActionExecutor_codiAccioDesconegut_noModificaEntitat() {
        // Arrange
        AlarmaEntity entity = new AlarmaEntity();
        entity.setEstat(AlarmaEstat.ESBORRADA);
        LocalDateTime dataOriginal = LocalDateTime.now().minusDays(1);
        entity.setDataEsborrat(dataOriginal);

        AlarmaServiceImpl.ReactivarActionExecutor executor =
                new AlarmaServiceImpl.ReactivarActionExecutor(authenticationHelper);

        // Act
        executor.exec("CODI_DESCONEGUT", entity, null);

        // Assert
        assertThat(entity.getEstat()).isEqualTo(AlarmaEstat.ESBORRADA);
        assertThat(entity.getDataEsborrat()).isEqualTo(dataOriginal);
    }

    @Test
    @DisplayName("ReactivarActionExecutor.onChange no llança excepció")
    void reactivarActionExecutor_onChange() {
        // Arrange
        AlarmaServiceImpl.ReactivarActionExecutor executor =
                new AlarmaServiceImpl.ReactivarActionExecutor(authenticationHelper);

        // Act & Assert
        assertThatCode(() -> executor.onChange(1L, null, "campo", "valor", Map.of(), new String[0], null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ReportLlistatIdAlarmaActiva.generateData retorna llista de recursos")
    @SuppressWarnings("unchecked")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void reportLlistatIdAlarmaActiva_generateData_retornaRecursos() throws Exception {
        // Arrange
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Long> query = mock(CriteriaQuery.class);
        Root<AlarmaEntity> root = mock(Root.class);
        TypedQuery<Long> typedQuery = mock(TypedQuery.class);
        Path idPath = mock(Path.class);
        Path estatPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Long.class)).thenReturn(query);
        when(query.from(AlarmaEntity.class)).thenReturn(root);
        when(root.get("id")).thenReturn(idPath);
        when(root.get("estat")).thenReturn(estatPath);
        when(root.get(anyString())).thenAnswer(invocation -> {
            String fieldName = invocation.getArgument(0);
            Path mockPath = mock(Path.class);
            when(mockPath.getJavaType()).thenReturn((Class) String.class);
            return mockPath;
        });

        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(1L, 2L));

        Class<?> reportClass = Class.forName("es.caib.comanda.alarmes.logic.service.AlarmaServiceImpl$ReportLlistatIdAlarmaActiva");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(AlarmaServiceImpl.class, EntityManager.class);
        constructor.setAccessible(true);

        ReportGenerator<AlarmaEntity, Serializable, AlarmaReduidaResource> reportGenerator =
                (ReportGenerator<AlarmaEntity, Serializable, AlarmaReduidaResource>) constructor.newInstance(alarmaService, entityManager);

        // Act
        List<AlarmaReduidaResource> result = reportGenerator.generateData(null, null, null);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    private AlarmaEntity crearAlarmaEntity(AlarmaEstat estat, boolean isAdmin, String createdBy) {
        AlarmaEntity entity = new AlarmaEntity();
        entity.setEstat(estat);

        AlarmaConfigEntity config = new AlarmaConfigEntity();
        config.setAdmin(isAdmin);
        ReflectionTestUtils.setField(config, "createdBy", createdBy);
        entity.setAlarmaConfig(config);

        return entity;
    }
}
