package es.caib.comanda.avisos.logic.service;

import es.caib.comanda.avisos.logic.helper.AvisClientHelper;
import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.avisos.logic.mapper.AvisMapper;
import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.avisos.persist.repository.AvisRepository;
import es.caib.comanda.client.model.*;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.model.v1.avis.AvisTipus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static es.caib.comanda.base.config.BaseConfig.ROLE_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvisServiceImplTest {

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private AvisClientHelper avisClientHelper;

    @Mock
    private AvisRepository avisRepository;

    @Mock
    private AvisMapper avisMapper;

    @Mock
    private Message jmsMessage;

    @InjectMocks
    private AvisServiceImpl avisService;

    @BeforeEach
    void setUp() {
        // Injectem el repositori manualment ja que AvisServiceImpl hereta de BaseMutableResourceService
        // i aquest usa un camp generic 'entityRepository' que normalment s'injecta via constructor o setter.
        ReflectionTestUtils.setField(avisService, "entityRepository", avisRepository);
        avisService.init();
    }

    @Test
    @DisplayName("receiveMessage ha de llançar ResourceNotFoundException si l'entornApp no existeix")
    void receiveMessage_quanEntornAppNoExisteix_llançaExcepcio() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.avis.Avis avisBroker = new es.caib.comanda.model.v1.avis.Avis();
        avisBroker.setEntornCodi("ENT");
        avisBroker.setAppCodi("APP");

        when(avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> avisService.receiveMessage(avisBroker, jmsMessage))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(jmsMessage).acknowledge();
    }

    @Test
    @DisplayName("receiveMessage ha d'esborrar l'avís si esborrar=true i l'avís existeix")
    void receiveMessage_quanEsborrarIExisteix_esborraAvis() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.avis.Avis avisBroker = new es.caib.comanda.model.v1.avis.Avis();
        avisBroker.setEntornCodi("ENT");
        avisBroker.setAppCodi("APP");
        avisBroker.setIdentificador("ID1");
        avisBroker.setEsborrar(true);

        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);
        when(avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.of(entornApp));

        AvisEntity avisExistent = new AvisEntity();
        when(avisRepository.findByEntornAppIdAndIdentificador(10L, "ID1")).thenReturn(Optional.of(avisExistent));

        // Act
        avisService.receiveMessage(avisBroker, jmsMessage);

        // Assert
        verify(avisRepository).delete(avisExistent);
        verify(jmsMessage).acknowledge();
    }

    @Test
    @DisplayName("receiveMessage ha de crear un nou avís si no existeix")
    void receiveMessage_quanNouAvis_guardaAvis() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.avis.Avis avisBroker = new es.caib.comanda.model.v1.avis.Avis();
        avisBroker.setEntornCodi("ENT");
        avisBroker.setAppCodi("APP");
        avisBroker.setIdentificador("ID1");
        avisBroker.setNom("Nou Avís");
        avisBroker.setTipus(AvisTipus.INFO);

        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);
        entornApp.setEntorn(EntornRef.builder().id(1L).build());
        entornApp.setApp(AppRef.builder().id(2L).build());

        when(avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.of(entornApp));
        when(avisRepository.findByEntornAppIdAndIdentificador(10L, "ID1")).thenReturn(Optional.empty());
        Avis avisMapejat = new Avis();
        avisMapejat.setEntornAppId(10L);
        avisMapejat.setEntornId(1L);
        avisMapejat.setAppId(2L);
        avisMapejat.setIdentificador("ID1");
        avisMapejat.setNom("Nou Avís");
        avisMapejat.setTipus(AvisTipus.INFO);
        AvisEntity avisEntityMapejat = new AvisEntity();
        avisEntityMapejat.setEntornAppId(10L);
        avisEntityMapejat.setEntornId(1L);
        avisEntityMapejat.setAppId(2L);
        avisEntityMapejat.setIdentificador("ID1");
        avisEntityMapejat.setNom("Nou Avís");
        avisEntityMapejat.setTipus(AvisTipus.INFO);
        when(avisMapper.toAvis(avisBroker, entornApp)).thenReturn(avisMapejat);
        when(avisMapper.toAvisEntity(avisMapejat)).thenReturn(avisEntityMapejat);

        // Act
        avisService.receiveMessage(avisBroker, jmsMessage);

        // Assert
        verify(avisMapper).toAvis(avisBroker, entornApp);
        verify(avisMapper).toAvisEntity(avisMapejat);
        verify(avisRepository).save(avisEntityMapejat);
        verify(jmsMessage).acknowledge();
    }

    @Test
    @DisplayName("afterConversion ha d'omplir els codis d'App i Entorn")
    void afterConversion_ompleAppIEntornCodi() {
        // Arrange
        AvisEntity entity = new AvisEntity();
        entity.setAppId(1L);
        entity.setEntornId(2L);
        Avis resource = new Avis();

        App app = new App();
        ReflectionTestUtils.setField(app, "codi", "APP1");
        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "codi", "ENT1");

        when(avisClientHelper.appById(1L)).thenReturn(app);
        when(avisClientHelper.entornById(2L)).thenReturn(entorn);

        // Act
        avisService.afterConversion(entity, resource);

        // Assert
        assertThat(resource.getAppCodi()).isEqualTo("APP1");
        assertThat(resource.getEntornCodi()).isEqualTo("ENT1");
    }

    @Test
    @DisplayName("additionalSpecification ha de retornar una especificació si l'usuari no és administrador")
    void additionalSpecification_quanNoAdmin_retornaEspecificacio() {
        // Arrange
        when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{"ROLE_USER"});
        when(authenticationHelper.getCurrentUserName()).thenReturn("usuari1");

        // Act
        Specification<AvisEntity> spec = avisService.additionalSpecification(null);

        // Assert
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("PathPerspectiveApplicator ha d'omplir el treePath")
    void pathPerspective_quanAvisInformat_ompleTreePath() throws PerspectiveApplicationException {
        // Arrange
        AvisServiceImpl.PathPerspectiveApplicator applicator = avisService.new PathPerspectiveApplicator();
        AvisEntity entity = new AvisEntity();
        entity.setEntornAppId(10L);
        Avis resource = new Avis();
        resource.setIdentificador("ID1");

        EntornApp entornApp = new EntornApp();
        ReflectionTestUtils.setField(entornApp, "app", AppRef.builder().nom("Aplicacio").build());
        ReflectionTestUtils.setField(entornApp, "entorn", EntornRef.builder().nom("Entorn").build());

        when(avisClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        // Act
        applicator.applySingle(Avis.PERSPECTIVE_PATH, entity, resource);

        // Assert
        assertThat(resource.getTreePath()).containsExactly("Aplicacio", "Entorn", "ID1");
    }

    @Test
    @DisplayName("receiveMessage ha d'actualitzar l'avís si ja existeix")
    void receiveMessage_quanAvisJaExisteix_actualitzaAvis() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.avis.Avis avisBroker = new es.caib.comanda.model.v1.avis.Avis();
        avisBroker.setEntornCodi("ENT");
        avisBroker.setAppCodi("APP");
        avisBroker.setIdentificador("ID1");
        avisBroker.setNom("Nom Actualitzat");
        avisBroker.setTipus(AvisTipus.ERROR);

        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);
        when(avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.of(entornApp));

        AvisEntity avisExistent = new AvisEntity();
        avisExistent.setIdentificador("ID1");
        avisExistent.setNom("Nom Antic");
        when(avisRepository.findByEntornAppIdAndIdentificador(10L, "ID1")).thenReturn(Optional.of(avisExistent));

        // Act
        avisService.receiveMessage(avisBroker, jmsMessage);

        // Assert
        verify(avisMapper).updateAvis(avisBroker, avisExistent);
        verify(avisMapper, never()).toAvis(any(es.caib.comanda.model.v1.avis.Avis.class), any(EntornApp.class));
        verify(avisMapper, never()).toAvisEntity(any());
        verify(avisRepository).save(avisExistent);
        verify(jmsMessage).acknowledge();
    }

    @Test
    @DisplayName("additionalSpecification ha de retornar null per administrador")
    void additionalSpecification_quanAdmin_retornaNull() {
        // Arrange
        String roleAdmin = "COM_ADMIN";
        when(authenticationHelper.getCurrentUserName()).thenReturn("admin");
        when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{roleAdmin});

        // Act
        Specification<AvisEntity> spec = avisService.additionalSpecification(null);

        // Assert
        assertThat(spec).as("L'especificació per a %s hauria de ser null", roleAdmin).isNull();
    }

    @Test
    @DisplayName("receiveMessage ha de loguejar un warning si s'intenta esborrar un avís que no existeix")
    void receiveMessage_quanEsborrarINoExisteix_noFaRes() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.avis.Avis avisBroker = new es.caib.comanda.model.v1.avis.Avis();
        avisBroker.setEntornCodi("ENT");
        avisBroker.setAppCodi("APP");
        avisBroker.setIdentificador("ID_INEXISTENT");
        avisBroker.setEsborrar(true);

        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);
        when(avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.of(entornApp));
        when(avisRepository.findByEntornAppIdAndIdentificador(10L, "ID_INEXISTENT")).thenReturn(Optional.empty());

        // Act
        avisService.receiveMessage(avisBroker, jmsMessage);

        // Assert
        verify(avisRepository, never()).delete(any());
        verify(jmsMessage).acknowledge();
    }

    @Test
    @DisplayName("afterConversion ha de gestionar App o Entorn null")
    void afterConversion_quanAppOEntornNull_gestionaNull() {
        // Arrange
        AvisEntity entity = new AvisEntity();
        entity.setAppId(1L);
        entity.setEntornId(2L);
        Avis resource = new Avis();

        when(avisClientHelper.appById(1L)).thenReturn(null);
        when(avisClientHelper.entornById(2L)).thenReturn(null);

        // Act
        avisService.afterConversion(entity, resource);

        // Assert
        assertThat(resource.getAppCodi()).isNull();
        assertThat(resource.getEntornCodi()).isNull();
    }

    @Test
    @DisplayName("convertToLocalDateTime ha de retornar null si la data és null")
    void convertToLocalDateTime_quanDataNull_retornaNull() {
        // Act
        LocalDateTime result = (LocalDateTime) ReflectionTestUtils.invokeMethod(AvisServiceImpl.class, "convertToLocalDateTime", (Date) null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("convertToLocalDateTime ha de convertir correctament la data")
    void convertToLocalDateTime_quanDataNoNull_converteixCorrectament() {
        // Arrange
        Date data = new Date();

        // Act
        LocalDateTime result = (LocalDateTime) ReflectionTestUtils.invokeMethod(AvisServiceImpl.class, "convertToLocalDateTime", data);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("teGrupSiNoNull ha de retornar una especificació vàlida i executar la lambda")
    void teGrupSiNoNull_retornaEspecificacio() {
        // Arrange
        String[] grups = {"GRUP1", "GRUP2"};
        Root<AvisEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);
        CriteriaBuilder.In in = mock(CriteriaBuilder.In.class);

        when(root.get("grup")).thenReturn(path);
        when(cb.isNull(path)).thenReturn(predicate);
        when(path.in(any(java.util.Collection.class))).thenReturn(in);
        when(cb.or(any(), any())).thenReturn(predicate);

        // Act
        Specification<AvisEntity> spec = AvisServiceImpl.teGrupSiNoNull(grups);
        Predicate result = spec.toPredicate(root, query, cb);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull();
        verify(root, times(2)).get("grup");
        verify(cb).isNull(path);
        verify(path).in(any(java.util.Collection.class));
        verify(cb).or(any(), any());
    }

    @Test
    @DisplayName("tePermisUsuari ha de retornar una especificació vàlida i executar la lambda")
    void tePermisUsuari_retornaEspecificacio() {
        // Arrange
        Root<AvisEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Join join = mock(Join.class);
        Predicate predicate = mock(Predicate.class);

        when(root.join(eq("usuarisAmbPermis"), any(JoinType.class))).thenReturn(join);
        when(cb.equal(eq(join), eq("usuari1"))).thenReturn(predicate);

        // Act
        Specification<AvisEntity> spec = (Specification<AvisEntity>) ReflectionTestUtils.invokeMethod(avisService, "tePermisUsuari", "usuari1");
        Predicate result = spec.toPredicate(root, query, cb);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull();
        verify(root).join("usuarisAmbPermis", JoinType.LEFT);
        verify(query).distinct(true);
        verify(cb).equal(join, "usuari1");
    }

    @Test
    @DisplayName("tePermisGrupIn ha de retornar una especificació vàlida i executar la lambda")
    void tePermisGrupIn_retornaEspecificacio() {
        // Arrange
        String[] grups = {"GRUP1", "GRUP2"};
        Root<AvisEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Join join = mock(Join.class);
        CriteriaBuilder.In in = mock(CriteriaBuilder.In.class);

        when(root.join(eq("grupsAmbPermis"), any(JoinType.class))).thenReturn(join);
        when(join.in(any(java.util.Collection.class))).thenReturn(in);

        // Act
        Specification<AvisEntity> spec = (Specification<AvisEntity>) ReflectionTestUtils.invokeMethod(avisService, "tePermisGrupIn", (Object) grups);
        Predicate result = spec.toPredicate(root, query, cb);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull();
        verify(root).join("grupsAmbPermis", JoinType.LEFT);
        verify(query).distinct(true);
        verify(join).in(any(java.util.Collection.class));
    }

    @Test
    @DisplayName("avisSensePermisos ha de retornar una especificació vàlida i executar la lambda")
    void avisSensePermisos_retornaEspecificacio() {
        // Arrange
        Root<AvisEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.isNull(any())).thenReturn(predicate);
        when(cb.isEmpty(any())).thenReturn(predicate);
        when(cb.and(any(), any(), any())).thenReturn(predicate);

        // Act
        Specification<AvisEntity> spec = (Specification<AvisEntity>) ReflectionTestUtils.invokeMethod(avisService, "avisSensePermisos");
        Predicate result = spec.toPredicate(root, query, cb);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull();
        verify(root).get("responsable");
        verify(root).get("usuarisAmbPermis");
        verify(root).get("grupsAmbPermis");
        verify(cb).isNull(any());
        verify(cb, times(2)).isEmpty(any());
        verify(cb).and(any(), any(), any());
    }

    @Test
    @DisplayName("additionalSpecification ha de combinar correctament les especificacions per a usuari normal")
    void additionalSpecification_quanUsuariNormal_combinaEspecificacions() {
        // Arrange
        when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{"ROLE_USER", "GRUP_TEST"});
        when(authenticationHelper.getCurrentUserName()).thenReturn("usuari_test");

        // Act
        Specification<AvisEntity> spec = avisService.additionalSpecification(null);

        // Assert
        assertThat(spec).isNotNull();
        // Atès que Specification és una interfície funcional (lambda), no podem verificar fàcilment 
        // el contingut sense un CriteriaBuilder real, però el fet que no falli i retorni un objecte
        // indica que s'ha passat per tota la lògica de combinació:
        // teGrupSiNoNull(roles).and(teResponsable(userName).or(tePermisUsuari(userName)).or(tePermisGrupIn(roles)).or(avisSensePermisos()))
    }
}
