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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @DisplayName("beforeCreateEntity permet crear alarma admin si l'usuari és administrador")
    void beforeCreateEntity_quanAdminAlarmaIUsuariAdmin_permetCrear() throws ResourceNotCreatedException {
        // Arrange
        AlarmaConfig resource = new AlarmaConfig();
        resource.setAdmin(true);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        // Act & Assert (no ha de llançar excepció)
        alarmaConfigService.beforeCreateEntity(new AlarmaConfigEntity(), resource, null);
    }

    @Test
    @DisplayName("beforeCreateEntity llança excepció si s'intenta crear alarma admin per usuari no administrador")
    void beforeCreateEntity_quanAdminAlarmaIUsuariNoAdmin_llancaExcepcio() {
        // Arrange
        AlarmaConfig resource = new AlarmaConfig();
        resource.setAdmin(true);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> alarmaConfigService.beforeCreateEntity(new AlarmaConfigEntity(), resource, null))
                .isInstanceOf(ResourceNotCreatedException.class);
    }

    @Test
    @DisplayName("beforeUpdateEntity llança excepció si s'intenta actualitzar alarma admin per usuari no administrador")
    void beforeUpdateEntity_quanAdminAlarmaIUsuariNoAdmin_llancaExcepcio() {
        // Arrange
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setAdmin(true);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> alarmaConfigService.beforeUpdateEntity(entity, new AlarmaConfig(), null))
                .isInstanceOf(ResourceNotUpdatedException.class);
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
}
