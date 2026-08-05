package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltre;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltreTipus;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardFiltreEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardFiltreRepository;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests per a la validació d'unicitat de DashboardFiltreServiceImpl: només s'admet un filtre de tipus PERIODE
 * per dashboard, i no es pot repetir la mateixa dimensió en dos filtres de tipus DIMENSIO.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardFiltreServiceImpl")
class DashboardFiltreServiceImplTest {

    private static final String PERIODE_DUPLICAT_MISSATGE = "Ja existeix un filtre de període al tauler de control.";
    private static final String DIMENSIO_DUPLICADA_MISSATGE = "Ja existeix un filtre per a aquesta dimensió al tauler de control.";
    private static final Long DASHBOARD_ID = 1L;

    @Mock
    private AuthenticationHelper authenticationHelper;
    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock
    private AclServiceClient aclServiceClient;
    @Mock
    private DashboardFiltreRepository dashboardFiltreRepository;
    @Mock
    private I18nUtil i18nUtil;
    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private DashboardFiltreServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(I18nUtil.class, "applicationContext", applicationContext);
        lenient().when(applicationContext.getBean(I18nUtil.class)).thenReturn(i18nUtil);
        lenient().when(i18nUtil.getI18nMessage(
                "es.caib.comanda.estadistica.logic.service.DashboardFiltreServiceImpl.periodeDuplicat"))
                .thenReturn(PERIODE_DUPLICAT_MISSATGE);
        lenient().when(i18nUtil.getI18nMessage(
                "es.caib.comanda.estadistica.logic.service.DashboardFiltreServiceImpl.dimensioDuplicada"))
                .thenReturn(DIMENSIO_DUPLICADA_MISSATGE);
    }

    private DashboardFiltreEntity existingEntity(Long id, DashboardFiltreTipus tipus, String dimensioCodi) {
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setId(DASHBOARD_ID);
        DashboardFiltreEntity entity = new DashboardFiltreEntity();
        entity.setId(id);
        entity.setDashboard(dashboard);
        entity.setTipus(tipus);
        entity.setDimensioCodi(dimensioCodi);
        return entity;
    }

    private DashboardFiltreEntity newEntity(Long dashboardId) {
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setId(dashboardId);
        DashboardFiltreEntity entity = new DashboardFiltreEntity();
        entity.setDashboard(dashboard);
        return entity;
    }

    private DashboardFiltre resource(Long dashboardId, DashboardFiltreTipus tipus, String dimensioCodi) {
        DashboardFiltre resource = new DashboardFiltre();
        resource.setDashboard(ResourceReference.toResourceReference(dashboardId));
        resource.setTipus(tipus);
        resource.setDimensioCodi(dimensioCodi);
        return resource;
    }

    // ------------------------------------------------------------------------------------------------------
    // beforeCreateEntity
    // ------------------------------------------------------------------------------------------------------

    @Test
    @DisplayName("beforeCreateEntity: permet crear un filtre PERIODE si el dashboard encara no en té cap")
    void beforeCreateEntity_periodeSenseDuplicats_noLlancaExcepcio() {
        when(dashboardFiltreRepository.findByDashboardIdOrderByOrdre(DASHBOARD_ID))
                .thenReturn(List.of(existingEntity(10L, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR")));

        DashboardFiltreEntity entity = newEntity(DASHBOARD_ID);
        DashboardFiltre resource = resource(DASHBOARD_ID, DashboardFiltreTipus.PERIODE, null);

        assertThatCode(() -> service.beforeCreateEntity(entity, resource, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("beforeCreateEntity: rebutja un segon filtre PERIODE al mateix dashboard")
    void beforeCreateEntity_periodeDuplicat_llancaResourceNotCreatedException() {
        when(dashboardFiltreRepository.findByDashboardIdOrderByOrdre(DASHBOARD_ID))
                .thenReturn(List.of(existingEntity(10L, DashboardFiltreTipus.PERIODE, null)));

        DashboardFiltreEntity entity = newEntity(DASHBOARD_ID);
        DashboardFiltre resource = resource(DASHBOARD_ID, DashboardFiltreTipus.PERIODE, null);

        assertThatThrownBy(() -> service.beforeCreateEntity(entity, resource, null))
                .isInstanceOf(ResourceNotCreatedException.class)
                .hasMessageContaining(PERIODE_DUPLICAT_MISSATGE);
    }

    @Test
    @DisplayName("beforeCreateEntity: permet crear un filtre DIMENSIO amb un codi encara no usat")
    void beforeCreateEntity_dimensioSenseDuplicats_noLlancaExcepcio() {
        when(dashboardFiltreRepository.findByDashboardIdOrderByOrdre(DASHBOARD_ID))
                .thenReturn(List.of(existingEntity(10L, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR")));

        DashboardFiltreEntity entity = newEntity(DASHBOARD_ID);
        DashboardFiltre resource = resource(DASHBOARD_ID, DashboardFiltreTipus.DIMENSIO, "CONSELLERIA");

        assertThatCode(() -> service.beforeCreateEntity(entity, resource, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("beforeCreateEntity: rebutja un filtre DIMENSIO que repeteix el codi d'un altre filtre existent")
    void beforeCreateEntity_dimensioDuplicada_llancaResourceNotCreatedException() {
        when(dashboardFiltreRepository.findByDashboardIdOrderByOrdre(DASHBOARD_ID))
                .thenReturn(List.of(existingEntity(10L, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR")));

        DashboardFiltreEntity entity = newEntity(DASHBOARD_ID);
        DashboardFiltre resource = resource(DASHBOARD_ID, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR");

        assertThatThrownBy(() -> service.beforeCreateEntity(entity, resource, null))
                .isInstanceOf(ResourceNotCreatedException.class)
                .hasMessageContaining(DIMENSIO_DUPLICADA_MISSATGE);
    }

    @Test
    @DisplayName("beforeCreateEntity: filtres DIMENSIO de dashboards diferents no interfereixen entre si")
    void beforeCreateEntity_mateixCodiEnAltreDashboard_noLlancaExcepcio() {
        DashboardFiltreEntity entity = newEntity(DASHBOARD_ID);
        DashboardFiltre resource = resource(DASHBOARD_ID, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR");
        // El repositori ja filtra per dashboardId; si el filtre existent és d'un altre dashboard, no es retorna
        when(dashboardFiltreRepository.findByDashboardIdOrderByOrdre(DASHBOARD_ID)).thenReturn(List.of());

        assertThatCode(() -> service.beforeCreateEntity(entity, resource, null))
                .doesNotThrowAnyException();
    }

    // ------------------------------------------------------------------------------------------------------
    // beforeUpdateEntity
    // ------------------------------------------------------------------------------------------------------

    @Test
    @DisplayName("beforeUpdateEntity: permet desar sense canvis el mateix filtre PERIODE (s'exclou d'ell mateix)")
    void beforeUpdateEntity_editantElMateixFiltrePeriode_noLlancaExcepcio() {
        Long filtreId = 10L;
        when(dashboardFiltreRepository.findByDashboardIdOrderByOrdre(DASHBOARD_ID))
                .thenReturn(List.of(existingEntity(filtreId, DashboardFiltreTipus.PERIODE, null)));

        DashboardFiltreEntity entity = existingEntity(filtreId, DashboardFiltreTipus.PERIODE, null);
        DashboardFiltre resource = resource(DASHBOARD_ID, DashboardFiltreTipus.PERIODE, null);

        assertThatCode(() -> service.beforeUpdateEntity(entity, resource, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("beforeUpdateEntity: rebutja canviar un filtre a PERIODE si un altre ja ho és")
    void beforeUpdateEntity_periodeDuplicat_llancaResourceNotUpdatedException() {
        Long filtreId = 10L;
        Long altreFiltreId = 20L;
        when(dashboardFiltreRepository.findByDashboardIdOrderByOrdre(DASHBOARD_ID))
                .thenReturn(List.of(
                        existingEntity(filtreId, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR"),
                        existingEntity(altreFiltreId, DashboardFiltreTipus.PERIODE, null)));

        DashboardFiltreEntity entity = existingEntity(filtreId, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR");
        DashboardFiltre resource = resource(DASHBOARD_ID, DashboardFiltreTipus.PERIODE, null);

        assertThatThrownBy(() -> service.beforeUpdateEntity(entity, resource, null))
                .isInstanceOf(ResourceNotUpdatedException.class)
                .hasMessageContaining(PERIODE_DUPLICAT_MISSATGE);
    }

    @Test
    @DisplayName("beforeUpdateEntity: rebutja canviar el codi de dimensió a un ja usat per un altre filtre")
    void beforeUpdateEntity_dimensioDuplicada_llancaResourceNotUpdatedException() {
        Long filtreId = 10L;
        Long altreFiltreId = 20L;
        when(dashboardFiltreRepository.findByDashboardIdOrderByOrdre(DASHBOARD_ID))
                .thenReturn(List.of(
                        existingEntity(filtreId, DashboardFiltreTipus.DIMENSIO, "CONSELLERIA"),
                        existingEntity(altreFiltreId, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR")));

        DashboardFiltreEntity entity = existingEntity(filtreId, DashboardFiltreTipus.DIMENSIO, "CONSELLERIA");
        DashboardFiltre resource = resource(DASHBOARD_ID, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR");

        assertThatThrownBy(() -> service.beforeUpdateEntity(entity, resource, null))
                .isInstanceOf(ResourceNotUpdatedException.class)
                .hasMessageContaining(DIMENSIO_DUPLICADA_MISSATGE);
    }

    @Test
    @DisplayName("beforeUpdateEntity: permet mantenir el mateix codi de dimensió en el propi filtre")
    void beforeUpdateEntity_mateixCodiEnElMateixFiltre_noLlancaExcepcio() {
        Long filtreId = 10L;
        when(dashboardFiltreRepository.findByDashboardIdOrderByOrdre(DASHBOARD_ID))
                .thenReturn(List.of(existingEntity(filtreId, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR")));

        DashboardFiltreEntity entity = existingEntity(filtreId, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR");
        DashboardFiltre resource = resource(DASHBOARD_ID, DashboardFiltreTipus.DIMENSIO, "ORGAN_GESTOR");

        assertThatCode(() -> service.beforeUpdateEntity(entity, resource, null))
                .doesNotThrowAnyException();
    }
}
