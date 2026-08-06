package es.caib.comanda.avisos.logic.service;

import es.caib.comanda.avisos.logic.helper.AvisClientHelper;
import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.avisos.logic.mapper.AvisMapper;
import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.avisos.persist.entity.AvisLlegitEntity;
import es.caib.comanda.avisos.persist.repository.AvisLlegitRepository;
import es.caib.comanda.avisos.persist.repository.AvisRepository;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.model.v1.avis.AvisTipus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.*;

import static es.caib.comanda.base.config.BaseConfig.ROLE_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a AvisServiceImpl")
class AvisServiceImplTest {

    @Mock private AuthenticationHelper authenticationHelper;
    @Mock private AvisClientHelper avisClientHelper;
    @Mock private AvisRepository avisRepository;
    @Mock private AvisLlegitRepository avisLlegitRepository;
    @Mock private AvisMapper avisMapper;
    @Mock private Message jmsMessage;

    @InjectMocks
    private AvisServiceImpl avisService;

    @BeforeEach
    void setUp() {
        // Injectem el repositori manualment ja que AvisServiceImpl hereta de BaseMutableResourceService
        ReflectionTestUtils.setField(avisService, "entityRepository", avisRepository);
        avisService.init();
    }

    // ========================================================================
    // 1. TESTOS PER A netejaPerEntornApp
    // ========================================================================

    @Test
    @DisplayName("netejaPerEntornApp: esborra els avisos quan la llista no és buida")
    void netejaPerEntornApp_quanHiHaAvisos_llavorsElsEsborra() {
        // Arrange
        Long entornAppId = 10L;
        List<AvisEntity> avisos = List.of(new AvisEntity(), new AvisEntity());
        when(avisRepository.findByEntornAppId(entornAppId)).thenReturn(avisos);

        // Act
        avisService.netejaPerEntornApp(entornAppId);

        // Assert
        verify(avisRepository, times(1)).findByEntornAppId(entornAppId);
        verify(avisRepository, times(1)).deleteAll(avisos);
    }

    @Test
    @DisplayName("netejaPerEntornApp: no fa res quan la llista d'avisos és buida")
    void netejaPerEntornApp_quanLlistaBuida_llavorsNoEsborraRes() {
        // Arrange
        Long entornAppId = 10L;
        when(avisRepository.findByEntornAppId(entornAppId)).thenReturn(Collections.emptyList());

        // Act
        avisService.netejaPerEntornApp(entornAppId);

        // Assert
        verify(avisRepository, times(1)).findByEntornAppId(entornAppId);
        verify(avisRepository, never()).deleteAll(anyList());
    }

    // ========================================================================
    // 2. TESTOS PER A receiveMessage (JMS Listener)
    // ========================================================================

    @Test
    @DisplayName("receiveMessage: llança ResourceNotFoundException si l'entornApp no existeix")
    void receiveMessage_quanEntornAppNoExisteix_llancaExcepcio() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.avis.Avis avisBroker = new es.caib.comanda.model.v1.avis.Avis();
        avisBroker.setEntornCodi("ENT");
        avisBroker.setAppCodi("APP");
        when(avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.empty());

        // Act & Assert
        // L'excepció es captura dins del catch (Throwable t), per tant no es propaga,
        // però hem de verificar que no es crida a save ni delete.
        assertThatCode(() -> avisService.receiveMessage(avisBroker, jmsMessage)).doesNotThrowAnyException();

        verify(jmsMessage).acknowledge();
        verify(avisRepository, never()).save(any());
        verify(avisRepository, never()).delete(any());
    }

    @Test
    @DisplayName("receiveMessage: gestiona excepcions internes sense propagar-les")
    void receiveMessage_quanEsLlancaExcepcioInterna_llavorsNoPropagaExcepcio() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.avis.Avis avisBroker = new es.caib.comanda.model.v1.avis.Avis();
        avisBroker.setEntornCodi("ENT");
        avisBroker.setAppCodi("APP");

        when(avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP"))
            .thenThrow(new RuntimeException("Error de xarxa simulat"));

        // Act & Assert
        // El catch (Throwable t) ha d'engolir l'excepció per no reencuar el missatge indefinidament
        assertThatCode(() -> avisService.receiveMessage(avisBroker, jmsMessage)).doesNotThrowAnyException();
        verify(jmsMessage).acknowledge();
    }

    @Test
    @DisplayName("receiveMessage: esborra l'avís si esborrar=true i l'avís existeix")
    void receiveMessage_quanEsborrarIExisteix_llavorsEsborraAvis() throws JMSException {
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
        verify(avisRepository, times(1)).delete(avisExistent);
        verify(jmsMessage, times(1)).acknowledge();
    }

    @Test
    @DisplayName("receiveMessage: no fa res si esborrar=true però l'avís no existeix")
    void receiveMessage_quanEsborrarINoExisteix_llavorsNoFaRes() throws JMSException {
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
        verify(jmsMessage, times(1)).acknowledge();
    }

    @Test
    @DisplayName("receiveMessage: crea un nou avís si no existeix prèviament")
    void receiveMessage_quanNouAvis_llavorsGuardaAvis() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.avis.Avis avisBroker = new es.caib.comanda.model.v1.avis.Avis();
        avisBroker.setEntornCodi("ENT");
        avisBroker.setAppCodi("APP");
        avisBroker.setIdentificador("ID1");
        avisBroker.setNom("Nou Avís");
        avisBroker.setTipus(AvisTipus.INFO);

        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);
        when(avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.of(entornApp));
        when(avisRepository.findByEntornAppIdAndIdentificador(10L, "ID1")).thenReturn(Optional.empty());

        Avis avisMapejat = new Avis();
        AvisEntity avisEntityMapejat = new AvisEntity();
        when(avisMapper.toAvis(avisBroker, entornApp)).thenReturn(avisMapejat);
        when(avisMapper.toAvisEntity(avisMapejat)).thenReturn(avisEntityMapejat);

        // Act
        avisService.receiveMessage(avisBroker, jmsMessage);

        // Assert
        verify(avisMapper, times(1)).toAvis(avisBroker, entornApp);
        verify(avisMapper, times(1)).toAvisEntity(avisMapejat);
        verify(avisRepository, times(1)).save(avisEntityMapejat);
        verify(jmsMessage, times(1)).acknowledge();
    }

    @Test
    @DisplayName("receiveMessage: actualitza l'avís si ja existeix")
    void receiveMessage_quanAvisJaExisteix_llavorsActualitzaAvis() throws JMSException {
        // Arrange
        es.caib.comanda.model.v1.avis.Avis avisBroker = new es.caib.comanda.model.v1.avis.Avis();
        avisBroker.setEntornCodi("ENT");
        avisBroker.setAppCodi("APP");
        avisBroker.setIdentificador("ID1");
        avisBroker.setNom("Nom Actualitzat");

        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);
        when(avisClientHelper.entornAppFindByEntornCodiAndAppCodi("ENT", "APP")).thenReturn(Optional.of(entornApp));

        AvisEntity avisExistent = new AvisEntity();
        when(avisRepository.findByEntornAppIdAndIdentificador(10L, "ID1")).thenReturn(Optional.of(avisExistent));

        // Act
        avisService.receiveMessage(avisBroker, jmsMessage);

        // Assert
        verify(avisMapper, times(1)).updateAvis(avisBroker, avisExistent);
        verify(avisMapper, never()).toAvis(eq(avisClientHelper), any());
        verify(avisMapper, never()).toAvisEntity(any());
        verify(avisRepository, times(1)).save(avisExistent);
        verify(jmsMessage, times(1)).acknowledge();
    }

    // ========================================================================
    // 3. TESTOS PER A FILTRES I ESPECIFICACIONS
    // ========================================================================

    @Test
    @DisplayName("namedFilterToSpringFilter: retorna el filtre correcte per a AVIS_NO_LLEGIT")
    void namedFilterToSpringFilter_quanEsAvisNoLlegit_llavorsRetornaFiltre() {
        // Arrange
        when(authenticationHelper.getCurrentUserName()).thenReturn("usuari1");

        // Act
        String result = avisService.namedFilterToSpringFilter(Avis.NAMED_FILTER_AVIS_NO_LLEGIT);

        // Assert
        assertThat(result).contains("not(exists(avisLlegits.usuari:'usuari1'))");
    }

    @Test
    @DisplayName("namedFilterToSpringFilter: retorna null per a filtres desconeguts")
    void namedFilterToSpringFilter_quanFiltreDesconegut_llavorsRetornaNull() {
        // Act
        String result = avisService.namedFilterToSpringFilter("FILTRE_DESCONEGUT");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("additionalSpecification: retorna null per a usuaris amb rol ADMIN")
    void additionalSpecification_quanAdmin_llavorsRetornaNull() {
        // Arrange
        when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{ROLE_ADMIN});

        // Act
        Specification<AvisEntity> spec = avisService.additionalSpecification(null);

        // Assert
        assertThat(spec).isNull();
    }

    @Test
    @DisplayName("additionalSpecification: retorna especificació combinada per a usuaris normals")
    void additionalSpecification_quanUsuariNormal_llavorsRetornaEspecificacio() {
        // Arrange
        when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{"ROLE_USER", "GRUP_TEST"});
        when(authenticationHelper.getCurrentUserName()).thenReturn("usuari_test");

        // Act
        Specification<AvisEntity> spec = avisService.additionalSpecification(null);

        // Assert
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("teGrupSiNoNull: genera predicat correcte")
    void teGrupSiNoNull_quanSExecuta_llavorsGeneraPredicat() {
        // Arrange
        String[] grups = {"GRUP1", "GRUP2"};
        Root<AvisEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("grup")).thenReturn(path);
        when(cb.isNull(path)).thenReturn(predicate);
        when(path.in(Collections.singleton(any()))).thenReturn(mock(CriteriaBuilder.In.class));
        when(cb.or(any(), any())).thenReturn(predicate);

        // Act
        Specification<AvisEntity> spec = AvisServiceImpl.teGrupSiNoNull(grups);
        Predicate result = spec.toPredicate(root, query, cb);

        // Assert
        assertThat(result).isNotNull();
        verify(root, times(2)).get("grup");
        verify(cb).isNull(path);
    }

    @Test
    @DisplayName("teResponsable: genera predicat correcte")
    void teResponsable_quanSExecuta_llavorsGeneraPredicat() {
        // Arrange
        Root<AvisEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("responsable")).thenReturn(path);
        when(cb.equal(path, "usuari1")).thenReturn(predicate);

        // Act
        Specification<AvisEntity> spec = (Specification<AvisEntity>) ReflectionTestUtils.invokeMethod(avisService, "teResponsable", "usuari1");
        Predicate result = spec.toPredicate(root, query, cb);

        // Assert
        assertThat(result).isNotNull();
        verify(root).get("responsable");
        verify(cb).equal(path, "usuari1");
    }

    @Test
    @DisplayName("tePermisUsuari: genera predicat correcte amb JOIN")
    void tePermisUsuari_quanSExecuta_llavorsGeneraPredicat() {
        // Arrange
        Root<AvisEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Join join = mock(Join.class);
        Predicate predicate = mock(Predicate.class);

        when(root.join(eq("usuarisAmbPermis"), any(JoinType.class))).thenReturn(join);
        when(cb.equal(join, "usuari1")).thenReturn(predicate);

        // Act
        Specification<AvisEntity> spec = (Specification<AvisEntity>) ReflectionTestUtils.invokeMethod(avisService, "tePermisUsuari", "usuari1");
        Predicate result = spec.toPredicate(root, query, cb);

        // Assert
        assertThat(result).isNotNull();
        verify(root).join("usuarisAmbPermis", JoinType.LEFT);
        verify(query).distinct(true);
    }

    @Test
    @DisplayName("tePermisGrupIn: genera predicat correcte amb JOIN")
    void tePermisGrupIn_quanSExecuta_llavorsGeneraPredicat() {
        // Arrange
        String[] grups = {"GRUP1"};
        Root<AvisEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Join join = mock(Join.class);

        when(root.join(eq("grupsAmbPermis"), any(JoinType.class))).thenReturn(join);
        when(join.in(Collections.singleton(any()))).thenReturn(mock(CriteriaBuilder.In.class));

        // Act
        Specification<AvisEntity> spec = (Specification<AvisEntity>) ReflectionTestUtils.invokeMethod(avisService, "tePermisGrupIn", (Object) grups);
        Predicate result = spec.toPredicate(root, query, cb);

        // Assert
        assertThat(result).isNotNull();
        verify(root).join("grupsAmbPermis", JoinType.LEFT);
        verify(query).distinct(true);
    }

    @Test
    @DisplayName("avisSensePermisos: genera predicat correcte")
    void avisSensePermisos_quanSExecuta_llavorsGeneraPredicat() {
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
        assertThat(result).isNotNull();
        verify(cb).isNull(any());
        verify(cb, times(2)).isEmpty(any());
        verify(cb).and(any(), any(), any());
    }

    // ========================================================================
    // 4. TESTOS PER A CONVERSIONS I PERSPECTIVES
    // ========================================================================

    @Test
    @DisplayName("afterConversion: omple els codis d'App i Entorn correctament")
    void afterConversion_quanAppIEntornExisteixen_llavorsOmpleCodis() {
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
    @DisplayName("afterConversion: gestiona correctament quan App o Entorn són null")
    void afterConversion_quanAppOEntornSonNull_llavorsDeixaCodisNull() {
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
    @DisplayName("PathPerspectiveApplicator: omple el treePath correctament")
    void pathPerspective_quanEntornAppExisteix_llavorsOmpleTreePath() throws PerspectiveApplicationException {
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
    @DisplayName("PathPerspectiveApplicator: gestiona correctament quan EntornApp és null")
    void pathPerspective_quanEntornAppEsNull_llavorsOmpleTreePathInvalid() throws PerspectiveApplicationException {
        // Arrange
        AvisServiceImpl.PathPerspectiveApplicator applicator = avisService.new PathPerspectiveApplicator();
        AvisEntity entity = new AvisEntity();
        entity.setEntornAppId(99L);
        Avis resource = new Avis();
        resource.setIdentificador("ID1");

        when(avisClientHelper.entornAppFindById(99L)).thenReturn(null);

        // Act
        applicator.applySingle(Avis.PERSPECTIVE_PATH, entity, resource);

        // Assert
        assertThat(resource.getTreePath()).containsExactly("INVALID_ENTORNAPP 99", "ID1");
    }

    @Test
    @DisplayName("EntornAppPerspectiveApplicator: omple App i Entorn al resource")
    void entornAppPerspective_quanEntornAppExisteix_llavorsOmpleAppIEntorn() throws PerspectiveApplicationException {
        // Arrange
        AvisServiceImpl.EntornAppPerspectiveApplicator applicator = avisService.new EntornAppPerspectiveApplicator();
        AvisEntity entity = new AvisEntity();
        entity.setEntornAppId(10L);
        Avis resource = new Avis();

        EntornApp entornApp = new EntornApp();
        AppRef app = new AppRef();
        ReflectionTestUtils.setField(app, "nom", "APP");
        EntornRef entorn = new EntornRef();
        ReflectionTestUtils.setField(entorn, "nom", "ENT");
        entornApp.setApp(app);
        entornApp.setEntorn(entorn);

        when(avisClientHelper.entornAppFindById(10L)).thenReturn(entornApp);

        // Act
        applicator.applySingle(Avis.PERSPECTIVE_ENTORN_APP, entity, resource);

        // Assert
        assertThat(resource.getApp()).isNotNull();
        assertThat(resource.getApp().getNom()).isEqualTo("APP");
        assertThat(resource.getEntorn()).isNotNull();
        assertThat(resource.getEntorn().getNom()).isEqualTo("ENT");
    }

    @Test
    @DisplayName("LlegitPerspectiveApplicator: marca com a llegit si existeix al repositori")
    void llegitPerspective_quanExisteixAlRepositori_llavorsMarcaComALlegit() throws PerspectiveApplicationException {
        // Arrange
        AvisServiceImpl.LlegitPerspectiveApplicator applicator = avisService.new LlegitPerspectiveApplicator();
        AvisEntity entity = new AvisEntity();
        entity.setId(1L);
        Avis resource = new Avis();

        when(authenticationHelper.getCurrentUserName()).thenReturn("usuari1");
        when(avisLlegitRepository.existsByUsuariAndAvis("usuari1", entity)).thenReturn(true);

        // Act
        applicator.applySingle(Avis.PERSPECTIVE_LLEGIT, entity, resource);

        // Assert
        assertThat(resource.isLlegit()).isTrue();
    }

    @Test
    @DisplayName("LlegitPerspectiveApplicator: marca com a no llegit si no existeix al repositori")
    void llegitPerspective_quanNoExisteixAlRepositori_llavorsMarcaComANoLlegit() throws PerspectiveApplicationException {
        // Arrange
        AvisServiceImpl.LlegitPerspectiveApplicator applicator = avisService.new LlegitPerspectiveApplicator();
        AvisEntity entity = new AvisEntity();
        entity.setId(1L);
        Avis resource = new Avis();

        when(authenticationHelper.getCurrentUserName()).thenReturn("usuari1");
        when(avisLlegitRepository.existsByUsuariAndAvis("usuari1", entity)).thenReturn(false);

        // Act
        applicator.applySingle(Avis.PERSPECTIVE_LLEGIT, entity, resource);

        // Assert
        assertThat(resource.isLlegit()).isFalse();
    }

    // ========================================================================
    // 5. TESTOS PER A ACTION EXECUTORS (MarcarAvisLlegit)
    // ========================================================================

    @Test
    @DisplayName("MarcarAvisLlegit: retorna null si la llista d'IDs és null o buida")
    void marcarAvisLlegit_quanIdsSonNullsOBuits_llavorsRetornaNull() throws Exception {
        // Arrange
        AvisServiceImpl.MarcarAvisLlegit executor = avisService.new MarcarAvisLlegit();
        Avis.AvisMarcarLlegitsAction params = new Avis.AvisMarcarLlegitsAction();
        params.setIds(null);

        // Act
        Avis result = executor.exec(Avis.ACTION_MARCAR_AVIS_LLEGIT, new AvisEntity(), params);

        // Assert
        assertThat(result).isNull();
        verify(avisRepository, never()).findAvisosNoLlegitsByUsuariAndIds(anyString(), anyList());
    }

    @Test
    @DisplayName("MarcarAvisLlegit: guarda entitats AvisLlegit quan llegit=true")
    void marcarAvisLlegit_quanLlegitEsTrue_llavorsGuardaEntitats() throws Exception {
        // Arrange
        AvisServiceImpl.MarcarAvisLlegit executor = avisService.new MarcarAvisLlegit();
        Avis.AvisMarcarLlegitsAction params = new Avis.AvisMarcarLlegitsAction();
        params.setIds(List.of(1L, 2L));
        params.setLlegit(true);

        AvisEntity avis1 = new AvisEntity(); avis1.setId(1L);
        when(authenticationHelper.getCurrentUserName()).thenReturn("usuari1");
        when(avisRepository.findAvisosNoLlegitsByUsuariAndIds("usuari1", List.of(1L, 2L)))
            .thenReturn(List.of(avis1));

        // Act
        Avis result = executor.exec(Avis.ACTION_MARCAR_AVIS_LLEGIT, new AvisEntity(), params);

        // Assert
        assertThat(result).isNull();
        verify(avisLlegitRepository, times(1)).saveAll(argThat(list ->
            ((List<?>) list).size() == 1 && ((AvisLlegitEntity) ((List<?>) list).get(0)).getUsuari().equals("usuari1")
        ));
    }

    @Test
    @DisplayName("MarcarAvisLlegit: elimina entitats AvisLlegit quan llegit=false")
    void marcarAvisLlegit_quanLlegitEsFalse_llavorsEliminaEntitats() throws Exception {
        // Arrange
        AvisServiceImpl.MarcarAvisLlegit executor = avisService.new MarcarAvisLlegit();
        Avis.AvisMarcarLlegitsAction params = new Avis.AvisMarcarLlegitsAction();
        params.setIds(List.of(1L, 2L));
        params.setLlegit(false);

        when(authenticationHelper.getCurrentUserName()).thenReturn("usuari1");

        // Act
        Avis result = executor.exec(Avis.ACTION_MARCAR_AVIS_LLEGIT, new AvisEntity(), params);

        // Assert
        assertThat(result).isNull();
        verify(avisLlegitRepository, times(1)).deleteByUsuariAndAvisIdIn("usuari1", List.of(1L, 2L));
        verify(avisRepository, never()).findAvisosNoLlegitsByUsuariAndIds(anyString(), anyList());
    }

    // ========================================================================
    // 6. TESTOS PER A MÈTODES AUXILIARS
    // ========================================================================

    @Test
    @DisplayName("convertToLocalDateTime: retorna null si la data d'entrada és null")
    void convertToLocalDateTime_quanDataEsNull_llavorsRetornaNull() {
        // Act
        LocalDateTime result = (LocalDateTime) ReflectionTestUtils.invokeMethod(AvisServiceImpl.class, "convertToLocalDateTime", (Date) null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("convertToLocalDateTime: converteix correctament una data no nul·la")
    void convertToLocalDateTime_quanDataNoEsNull_llavorsConverteixCorrectament() {
        // Arrange
        Date data = new Date();

        // Act
        LocalDateTime result = (LocalDateTime) ReflectionTestUtils.invokeMethod(AvisServiceImpl.class, "convertToLocalDateTime", data);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(LocalDateTime.now().getYear());
    }
}
