package es.caib.comanda.alarmes.logic.service;

import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventPublisher;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlarmaConfigServiceImplTest {

    @Mock
    private AuthenticationHelper authenticationHelper;
    @Mock
    private AlarmaRepository alarmaRepository;
    @Mock
    private I18nUtil i18nUtil;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ComandaSseEventPublisher comandaSseEventPublisher;

    @InjectMocks
    private AlarmaConfigServiceImpl alarmaConfigService;

    private static final String CURRENT_USER = "user1";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(I18nUtil.class, "applicationContext", applicationContext);
        lenient().when(applicationContext.getBean(I18nUtil.class)).thenReturn(i18nUtil);
    }

    @Test
    @DisplayName("additionalSpringFilter retorna filtre base per a administrador")
    void additionalSpringFilter_quanAdmin_retornaFiltreBase() {
        // Arrange
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        // Act
        String result = alarmaConfigService.additionalSpringFilter("", new String[0]);

        // Assert
        assertThat(result).isEqualTo("esborrat:false");
    }

    @Test
    @DisplayName("additionalSpringFilter retorna filtre base i creatPer per a usuari normal")
    void additionalSpringFilter_quanUsuari_retornaFiltreRestringit() {
        // Arrange
        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        // Act
        String result = alarmaConfigService.additionalSpringFilter("", new String[0]);

        // Assert
        assertThat(result).isEqualTo("esborrat:false and createdBy:'" + CURRENT_USER + "'");
    }

    @Test
    @DisplayName("DeleteAlarmaConfigAction marca com esborrat i finalitza alarmes")
    void deleteAlarmaConfigAction_exec_marcaEsborratIFinalitza() throws Exception {
        // Arrange
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setEsborrat(false);

        // Obtenim l'executor de l'acció registrat a l'init()
        alarmaConfigService.init();
        BaseMutableResourceService.ActionExecutor actionExecutor = (BaseMutableResourceService.ActionExecutor) 
            ((java.util.Map)ReflectionTestUtils.getField(alarmaConfigService, "actionExecutorMap")).get(AlarmaConfig.ALARMA_CONFIG_DELETE_ACTION);

        // Act
        actionExecutor.exec(AlarmaConfig.ALARMA_CONFIG_DELETE_ACTION, entity, null);

        // Assert
        assertThat(entity.isEsborrat()).isTrue();
        verify(alarmaRepository).deleteByAlarmaConfigAndEstat(entity, AlarmaEstat.ESBORRANY);
        verify(alarmaRepository).finalizeByAlarmaConfig(eq(entity), any(LocalDateTime.class));
    }

    @ParameterizedTest
    @MethodSource("proporcionarCasosPermisosCreate")
    @DisplayName("beforeCreateEntity: matriz de permisos")
    void beforeCreateEntity_matrizPermisos(
            boolean resourceIsAdmin,
            boolean resourceIsCorreuGeneric,
            boolean userIsAdmin,
            boolean shouldThrow,
            String descripcion) {
        // Arrange
        AlarmaConfig resource = new AlarmaConfig();
        resource.setAdmin(resourceIsAdmin);
        resource.setCorreuGeneric(resourceIsCorreuGeneric);

        lenient().when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(userIsAdmin);

        // Act & Assert
        if (shouldThrow) {
            assertThatThrownBy(() ->
                    alarmaConfigService.beforeCreateEntity(new AlarmaConfigEntity(), resource, null)
            ).as(descripcion).isInstanceOf(ResourceNotCreatedException.class);
        } else {
            assertThatCode(() ->
                    alarmaConfigService.beforeCreateEntity(new AlarmaConfigEntity(), resource, null)
            ).as(descripcion).doesNotThrowAnyException();
        }
    }

    private static Stream<Arguments> proporcionarCasosPermisosCreate() {
        return Stream.of(
                // isAdmin, isCorreuGeneric, userIsAdmin, shouldThrow, descripció
                arguments(true,  false, true,  false, "Admin crea alarma admin → OK"),
                arguments(true,  false, false, true,  "No-admin crea alarma admin → EXCEPCIÓN"),
                arguments(false, true,  true,  false, "Admin crea alarma genérica → OK"),
                arguments(false, true,  false, true,  "No-admin crea alarma genérica → EXCEPCIÓN"),
                arguments(false, false, true,  false, "Admin crea alarma normal → OK"),
                arguments(false, false, false, false, "No-admin crea alarma normal → OK")
        );
    }

    @ParameterizedTest
    @MethodSource("proporcionarCasosPermisosUpdate")
    @DisplayName("beforeUpdateEntity: matriz de permisos")
    void beforeUpdateEntity_matrizPermisos(
            boolean entityIsAdmin,
            boolean entityIsCorreuGeneric,
            boolean userIsAdmin,
            boolean shouldThrow,
            String descripcion) {
        // Arrange
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setAdmin(entityIsAdmin);
        entity.setCorreuGeneric(entityIsCorreuGeneric);
        AlarmaConfig resource = new AlarmaConfig();

        lenient().when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(userIsAdmin);

        // Act & Assert
        if (shouldThrow) {
            assertThatThrownBy(() ->
                    alarmaConfigService.beforeUpdateEntity(entity, resource, null)
            ).as(descripcion).isInstanceOf(ResourceNotUpdatedException.class);
        } else {
            assertThatCode(() ->
                    alarmaConfigService.beforeUpdateEntity(entity, resource, null)
            ).as(descripcion).doesNotThrowAnyException();
        }
    }

    private static Stream<Arguments> proporcionarCasosPermisosUpdate() {
        return Stream.of(
                // entityIsAdmin, entityIsCorreuGeneric, userIsAdmin, shouldThrow, descripció
                arguments(true,  false, true,  false, "Admin actualitza alarma admin → OK"),
                arguments(true,  false, false, true,  "No-admin actualitza alarma admin → EXCEPCIÓ"),
                arguments(false, true,  true,  false, "Admin actualitza alarma genèrica → OK"),
                arguments(false, true,  false, true,  "No-admin actualitza alarma genèrica → EXCEPCIÓ"),
                arguments(false, false, true,  false, "Admin actualitza alarma normal → OK"),
                arguments(false, false, false, false, "No-admin actualitza alarma normal → OK")
        );
    }
}
