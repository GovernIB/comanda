package es.caib.comanda.tasques.logic.service;

import es.caib.comanda.client.model.*;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.tasques.logic.helper.TasquesClientHelper;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import es.caib.comanda.tasques.persist.entity.TascaEntity;
import es.caib.comanda.tasques.persist.repository.TascaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static es.caib.comanda.base.config.BaseConfig.ROLE_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TascaServiceImplTest {

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private TasquesClientHelper tasquesClientHelper;

    @Mock
    private TascaRepository tascaRepository;

    @Mock
    private Message jmsMessage;

    @InjectMocks
    private TascaServiceImpl tascaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tascaService, "entityRepository", tascaRepository);
    }

    @Test
    void receiveMessage_quanEntornAppNoExisteix_llançaExcepcio() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.tasca.Tasca tascaBroker = new es.caib.comanda.model.v1.tasca.Tasca();
        tascaBroker.setEntornCodi("ENT");
        tascaBroker.setAppCodi("APP");

        when(tasquesClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            tascaService.receiveMessage(tascaBroker, jmsMessage);
        });
        verify(jmsMessage).acknowledge();
    }

    @Test
    void receiveMessage_quanEsborrarIExisteix_esborraTasca() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.tasca.Tasca tascaBroker = new es.caib.comanda.model.v1.tasca.Tasca();
        tascaBroker.setEntornCodi("ENT");
        tascaBroker.setAppCodi("APP");
        tascaBroker.setIdentificador("ID1");
        tascaBroker.setEsborrar(true);

        EntornApp entornApp = new EntornApp();
        entornApp.setId(100L);
        when(tasquesClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.of(entornApp));

        TascaEntity entity = new TascaEntity();
        when(tascaRepository.findByEntornAppIdAndIdentificador(100L, "ID1")).thenReturn(Optional.of(entity));

        // Act
        tascaService.receiveMessage(tascaBroker, jmsMessage);

        // Assert
        verify(tascaRepository).delete(entity);
        verify(jmsMessage).acknowledge();
    }

    @Test
    void receiveMessage_quanNovaTasca_guardaTasca() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.tasca.Tasca tascaBroker = new es.caib.comanda.model.v1.tasca.Tasca();
        tascaBroker.setEntornCodi("ENT");
        tascaBroker.setAppCodi("APP");
        tascaBroker.setIdentificador("ID1");
        tascaBroker.setNom("Tasca Nova");

        EntornApp entornApp = new EntornApp();
        entornApp.setId(100L);
        AppRef app = AppRef.builder().id(10L).build();
        EntornRef entorn = EntornRef.builder().id(20L).build();
        entornApp.setApp(app);
        entornApp.setEntorn(entorn);
        
        when(tasquesClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.of(entornApp));
        when(tascaRepository.findByEntornAppIdAndIdentificador(100L, "ID1")).thenReturn(Optional.empty());

        // Act
        tascaService.receiveMessage(tascaBroker, jmsMessage);

        // Assert
        verify(tascaRepository).save(any(TascaEntity.class));
        verify(jmsMessage).acknowledge();
    }

    @Test
    void receiveMessage_quanActualitzarTascaExistent_actualitzaCamps() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.tasca.Tasca tascaBroker = new es.caib.comanda.model.v1.tasca.Tasca();
        tascaBroker.setEntornCodi("ENT");
        tascaBroker.setAppCodi("APP");
        tascaBroker.setIdentificador("ID1");
        tascaBroker.setNom("Nom Nou");

        EntornApp entornApp = new EntornApp();
        entornApp.setId(100L);
        entornApp.setApp(AppRef.builder().id(10L).build());
        entornApp.setEntorn(EntornRef.builder().id(20L).build());

        when(tasquesClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.of(entornApp));

        TascaEntity entity = spy(new TascaEntity());
        when(tascaRepository.findByEntornAppIdAndIdentificador(100L, "ID1")).thenReturn(Optional.of(entity));

        // Act
        tascaService.receiveMessage(tascaBroker, jmsMessage);

        // Assert
        verify(entity).setNom("Nom Nou");
        verify(tascaRepository, never()).save(any());
        verify(jmsMessage).acknowledge();
    }

    @Test
    void receiveMessage_quanEsborrarInexistent_noFaRes() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.tasca.Tasca tascaBroker = new es.caib.comanda.model.v1.tasca.Tasca();
        tascaBroker.setEntornCodi("ENT");
        tascaBroker.setAppCodi("APP");
        tascaBroker.setIdentificador("ID1");
        tascaBroker.setEsborrar(true);

        EntornApp entornApp = new EntornApp();
        entornApp.setId(100L);
        when(tasquesClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.of(entornApp));
        when(tascaRepository.findByEntornAppIdAndIdentificador(100L, "ID1")).thenReturn(Optional.empty());

        // Act
        tascaService.receiveMessage(tascaBroker, jmsMessage);

        // Assert
        verify(tascaRepository, never()).delete(any());
        verify(jmsMessage).acknowledge();
    }

    @Test
    void expirationPerspective_quanTascaNoFinalitzada_calculaDies() throws Exception {
        // Arrange
        TascaServiceImpl.ExpirationPerspectiveApplicator applicator = new TascaServiceImpl.ExpirationPerspectiveApplicator();
        Tasca resource = new Tasca();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        resource.setDataCaducitat(now.plusDays(5));
        resource.setDataFi(null);
        TascaEntity entity = new TascaEntity();

        // Act
        applicator.applySingle("EXPIRATION", entity, resource);

        // Assert
        assertThat(resource.getDiesPerCaducar()).isGreaterThanOrEqualTo(4); // Depèn de l'hora exacta
    }

    @Test
    void pathPerspective_quanTascaInformada_ompleTreePath() throws Exception {
        // Arrange
        TascaServiceImpl.PathPerspectiveApplicator applicator = new TascaServiceImpl.PathPerspectiveApplicator(tasquesClientHelper);
        Tasca resource = new Tasca();
        resource.setNom("Tasca");
        resource.setIdentificador("ID-IDENT");
        TascaEntity entity = new TascaEntity();
        entity.setEntornAppId(1L);

        EntornApp entornApp = new EntornApp();
        AppRef app = AppRef.builder().nom("APP-NOM").build();
        EntornRef entorn = EntornRef.builder().nom("ENT-NOM").build();
        entornApp.setApp(app);
        entornApp.setEntorn(entorn);
        
        when(tasquesClientHelper.entornAppFindById(1L)).thenReturn(entornApp);

        // Act
        applicator.applySingle("PATH", entity, resource);

        // Assert
        assertThat(resource.getTreePath()).containsExactly("APP-NOM", "ENT-NOM", "ID-IDENT");
    }

    @Test
    void convertToLocalDateTime_quanDataNoNull_converteixCorrectament() {
        // Arrange
        Date date = new Date();
        LocalDateTime expected = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // Act
        LocalDateTime result = ReflectionTestUtils.invokeMethod(TascaServiceImpl.class, "convertToLocalDateTime", date);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void convertToLocalDateTime_quanDataNull_retornaNull() {
        // Act
        LocalDateTime result = ReflectionTestUtils.invokeMethod(TascaServiceImpl.class, "convertToLocalDateTime", (Date) null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void afterConversion_ompleAppIEntornCodi() {
        // Arrange
        TascaEntity entity = new TascaEntity();
        entity.setAppId(10L);
        entity.setEntornId(20L);
        Tasca resource = new Tasca();

        App app = mock(App.class);
        when(app.getCodi()).thenReturn("APP_CODI");
        Entorn entorn = mock(Entorn.class);
        when(entorn.getCodi()).thenReturn("ENT_CODI");

        when(tasquesClientHelper.appById(10L)).thenReturn(app);
        when(tasquesClientHelper.entornById(20L)).thenReturn(entorn);

        // Act
        tascaService.afterConversion(entity, resource);

        // Assert
        assertThat(resource.getAppCodi()).isEqualTo("APP_CODI");
        assertThat(resource.getEntornCodi()).isEqualTo("ENT_CODI");
    }

    @Test
    void additionalSpecification_quanAdmin_retornaNull() {
        // Arrange
        when(authenticationHelper.getCurrentUserRoles()).thenReturn(new String[]{ROLE_ADMIN});

        // Act
        Specification<TascaEntity> spec = tascaService.additionalSpecification(null);

        // Assert
        assertThat(spec).isNull();
    }

    @Test
    void additionalSpecification_quanNoAdmin_retornaEspecificacio() {
        // Arrange
        String[] roles = {"ROLE_USER", "GRUP_TEST"};
        when(authenticationHelper.getCurrentUserName()).thenReturn("usuari1");
        when(authenticationHelper.getCurrentUserRoles()).thenReturn(roles);

        // Act
        Specification<TascaEntity> spec = tascaService.additionalSpecification(null);

        // Assert
        assertThat(spec).isNotNull();
        // Com que és una composició de specifications, no podem verificar-ne el contingut fàcilment,
        // però verifiquem que no és null i que s'han cridat els helpers d'autenticació.
        verify(authenticationHelper).getCurrentUserName();
        verify(authenticationHelper).getCurrentUserRoles();
    }
}
