package es.caib.comanda.usuaris.logic.service;

import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.usuaris.logic.helper.UsuarisRefreshHelper;
import es.caib.comanda.usuaris.logic.intf.model.Usuari;
import es.caib.comanda.usuaris.persist.entity.UsuariEntity;
import es.caib.comanda.usuaris.persist.repository.UsuariRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a UsuariServiceImpl")
class UsuariServiceImplTest {

    @Mock private AuthenticationHelper authenticationHelper;
    @Mock private ParametresHelper parametresHelper;
    @Mock private UsuarisRefreshHelper usuarisRefreshHelper;
    @Mock private UsuariRepository usuariRepository;
    @Mock private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @InjectMocks
    private UsuariServiceImpl usuariService;

    @BeforeEach
    void setUp() {
        // Injecció del repositori que ve de la classe base BaseMutableResourceService
        ReflectionTestUtils.setField(usuariService, "entityRepository", usuariRepository);
        ReflectionTestUtils.setField(usuariService, "resourceEntityMappingHelper", resourceEntityMappingHelper);
    }

    // ========================================================================
    // 1. TESTOS PER A additionalSpringFilter
    // ========================================================================

    @Test
    @DisplayName("additionalSpringFilter: afegeix el filtre de l'usuari actual al filtre existent")
    void additionalSpringFilter_quanHiHaFiltreActual_llavorsAfegeixFiltreUsuari() {
        // Arrange
        String currentFilter = "nom:'Test'";
        when(authenticationHelper.getCurrentUserName()).thenReturn("usuariActual");

        // Act
        String result = usuariService.additionalSpringFilter(currentFilter, new String[0]);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("nom : 'Test'");
        assertThat(result).contains("usuariActual");
        verify(authenticationHelper, times(1)).getCurrentUserName();
    }

    @Test
    @DisplayName("additionalSpringFilter: crea filtre només amb l'usuari actual quan no hi ha filtre previ")
    void additionalSpringFilter_quanNoHiHaFiltreActual_llavorsCreaFiltreNomesUsuari() {
        // Arrange
        when(authenticationHelper.getCurrentUserName()).thenReturn("usuariActual");

        // Act
        String result = usuariService.additionalSpringFilter(null, new String[0]);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("usuariActual");
        verify(authenticationHelper, times(1)).getCurrentUserName();
    }

    // ========================================================================
    // 2. TESTOS PER A afterConversion
    // ========================================================================

    @Test
    @DisplayName("afterConversion: assigna els rols de l'usuari autenticat al recurs")
    void afterConversion_quanEsValida_llavorsAssignaRols() {
        // Arrange
        UsuariEntity entity = new UsuariEntity();
        Usuari resource = new Usuari();

        Usuari usuariAuth = new Usuari();
        usuariAuth.setRols(new String[]{"ROLE_ADMIN", "ROLE_USER"});
        when(usuarisRefreshHelper.getUsuariFromAuth()).thenReturn(usuariAuth);

        // Act
        usuariService.afterConversion(entity, resource);

        // Assert
        assertThat(resource.getRols()).containsExactly("ROLE_ADMIN", "ROLE_USER");
        verify(usuarisRefreshHelper, times(1)).getUsuariFromAuth();
    }

    // ========================================================================
    // 3. TESTOS PER A findOneInternalByCodi
    // ========================================================================

    @Test
    @DisplayName("findOneInternalByCodi: retorna l'usuari quan existeix")
    void findOneInternalByCodi_quanUsuariExisteix_llavorsRetornaUsuari() {
        // Arrange
        String codi = "user1";
        UsuariEntity entity = new UsuariEntity();
        entity.setCodi(codi);

        when(usuariRepository.findByCodi(codi)).thenReturn(Optional.of(entity));
        when(resourceEntityMappingHelper.entityToResource(any(), any())).thenReturn(new Usuari());

        // Act
        Usuari result = usuariService.findOneInternalByCodi(codi);

        // Assert
        assertThat(result).isNotNull();
        verify(usuariRepository, times(1)).findByCodi(codi);
    }

    @Test
    @DisplayName("findOneInternalByCodi: llança ResourceNotFoundException quan l'usuari no existeix")
    void findOneInternalByCodi_quanUsuariNoExisteix_llancaExcepcio() {
        // Arrange
        String codi = "unknownUser";
        when(usuariRepository.findByCodi(codi)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuariService.findOneInternalByCodi(codi))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("codi:" + codi);

        verify(usuariRepository, times(1)).findByCodi(codi);
    }

    // ========================================================================
    // 4. TESTOS PER A AlarmaMailOnchangeLogicProcessor
    // ========================================================================

    @Test
    @DisplayName("AlarmaMailOnchangeLogicProcessor: desactiva alarmaMailAgrupar quan alarmaMail es posa a false")
    void alarmaMailOnchangeLogicProcessor_quanAlarmaMailEsFalse_llavorsDesactivaAgrupar() {
        // Arrange
        UsuariServiceImpl.AlarmaMailOnchangeLogicProcessor processor = usuariService.new AlarmaMailOnchangeLogicProcessor();
        Usuari target = new Usuari();
        target.setAlarmaMailAgrupar(true); // Estat inicial

        // Act
        processor.onChange(1L, null, "alarmaMail", false, new HashMap<>(), new String[0], target);

        // Assert
        assertThat(target.isAlarmaMailAgrupar()).isFalse();
    }

    @Test
    @DisplayName("AlarmaMailOnchangeLogicProcessor: no modifica alarmaMailAgrupar quan alarmaMail es posa a true")
    void alarmaMailOnchangeLogicProcessor_quanAlarmaMailEsTrue_llavorsNoModificaAgrupar() {
        // Arrange
        UsuariServiceImpl.AlarmaMailOnchangeLogicProcessor processor = usuariService.new AlarmaMailOnchangeLogicProcessor();
        Usuari target = new Usuari();
        target.setAlarmaMailAgrupar(true); // Estat inicial

        // Act
        processor.onChange(1L, null, "alarmaMail", true, new HashMap<>(), new String[0], target);

        // Assert
        assertThat(target.isAlarmaMailAgrupar()).isTrue(); // Ha de romandre true
    }

    // ========================================================================
    // 5. TESTOS PER A Perspectives
    // ========================================================================

    @Test
    @DisplayName("PreviewAutogeneratedEmailPerspectiveApplicator: genera l'email amb el domini per defecte")
    void previewAutogeneratedEmailPerspectiveApplicator_quanEsValida_llavorsGeneraEmail() throws PerspectiveApplicationException {
        // Arrange
        UsuariServiceImpl.PreviewAutogeneratedEmailPerspectiveApplicator applicator =
            usuariService.new PreviewAutogeneratedEmailPerspectiveApplicator();
        UsuariEntity entity = new UsuariEntity();
        entity.setCodi("user1");
        Usuari resource = new Usuari();

        when(parametresHelper.getParametreText(anyString(), eq("caib.es"))).thenReturn("test.es");

        // Act
        applicator.applySingle("PREVIEW", entity, resource);

        // Assert
        assertThat(resource.getAutogeneratedEmail()).isEqualTo("user1@test.es");
        verify(parametresHelper, times(1)).getParametreText(anyString(), eq("caib.es"));
    }

    @Test
    @DisplayName("DarreraConnexioPerspectiveApplicator: assigna la data de fi del darrer període")
    void darreraConnexioPerspectiveApplicator_quanEsValida_llavorsAssignaDataConnexio() throws PerspectiveApplicationException {
        // Arrange
        UsuariServiceImpl.DarreraConnexioPerspectiveApplicator applicator =
            usuariService.new DarreraConnexioPerspectiveApplicator();
        UsuariEntity entity = new UsuariEntity();
        LocalDateTime dataConnexio = LocalDateTime.now().minusDays(5);
        entity.setFiDarrerPeriode(dataConnexio);
        Usuari resource = new Usuari();

        // Act
        applicator.applySingle("DARRERA_CONNEXIO", entity, resource);

        // Assert
        assertThat(resource.getDarreraConnexio()).isEqualTo(dataConnexio);
    }
}
