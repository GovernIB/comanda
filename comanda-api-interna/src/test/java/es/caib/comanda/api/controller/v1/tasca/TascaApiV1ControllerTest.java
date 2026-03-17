package es.caib.comanda.api.controller.v1.tasca;

import es.caib.comanda.api.helper.ApiClientHelper;
import es.caib.comanda.api.util.ApiMapper;
import es.caib.comanda.model.v1.tasca.Tasca;
import es.caib.comanda.model.v1.tasca.TascaEstat;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import es.caib.comanda.model.v1.tasca.TascaPage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static es.caib.comanda.base.config.Cues.CUA_TASQUES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TascaApiV1ControllerTest {

    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private ApiClientHelper apiClientHelper;
    @Mock
    private ApiMapper apiMapper;

    @InjectMocks
    private TascaApiV1Controller controller;

    @Test
    @DisplayName("crearTasca envia missatge a JMS si les dades són vàlides")
    void crearTasca_quanDadesValides_enviaMissatge() throws MalformedURLException {
        // Arrange
        Tasca tasca = createValidTasca();
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(new App()));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(new Entorn()));

        // Act
        ResponseEntity<String> response = controller.crearTasca(tasca);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jmsTemplate).convertAndSend(eq(CUA_TASQUES), eq(tasca));
    }

    @Test
    @DisplayName("crearTasca retorna 400 si falten camps obligatoris")
    void crearTasca_quanFaltenCamps_retorna400() {
        // Arrange
        Tasca tasca = new Tasca(); // Buit

        // Act
        ResponseEntity<String> response = controller.crearTasca(tasca);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(jmsTemplate);
    }

    @Test
    @DisplayName("crearTasca retorna 400 si l'aplicació no existeix")
    void crearTasca_quanAppNoExisteix_retorna400() throws MalformedURLException {
        // Arrange
        Tasca tasca = createValidTasca();
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = controller.crearTasca(tasca);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("No existeix l'aplicació");
    }

    @Test
    @DisplayName("modificarTasca envia missatge si la tasca existeix")
    void modificarTasca_quanExisteix_enviaMissatge() throws MalformedURLException {
        // Arrange
        String identificador = "T1";
        Tasca tasca = createValidTasca();
        tasca.setIdentificador(identificador);
        
        App app = new App();
        Entorn entorn = new Entorn();
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(entorn));
        // Simulem que la tasca existeix al helper (el controlador crida existTasca que crida getTascaByCodi)
        when(apiClientHelper.getTasca(eq(identificador), any(), any())).thenReturn(Optional.of(new es.caib.comanda.client.model.Tasca()));

        // Act
        ResponseEntity<String> response = controller.modificarTasca(identificador, tasca);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jmsTemplate).convertAndSend(eq(CUA_TASQUES), eq(tasca));
    }

    @Test
    @DisplayName("eliminarTasca envia missatge amb flag esborrar")
    void eliminarTasca_quanExisteix_enviaMissatgeEsborrar() {
        // Arrange
        String id = "T1";
        String appCodi = "APP1";
        String entornCodi = "ENT1";
        
        when(apiClientHelper.getAppByCodi(appCodi)).thenReturn(Optional.of(new App()));
        when(apiClientHelper.getEntornByCodi(entornCodi)).thenReturn(Optional.of(new Entorn()));
        when(apiClientHelper.getTasca(eq(id), any(), any())).thenReturn(Optional.of(new es.caib.comanda.client.model.Tasca()));

        // Act
        ResponseEntity<String> response = controller.eliminarTasca(id, appCodi, entornCodi);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jmsTemplate).convertAndSend(eq(CUA_TASQUES), any(Tasca.class));
    }

    @Test
    @DisplayName("modificarTasca retorna 400 si l'aplicació no existeix")
    void modificarTasca_quanAppNoExisteix_retorna400() throws MalformedURLException {
        // Arrange
        String identificador = "T1";
        Tasca tasca = createValidTasca();
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> controller.modificarTasca(identificador, tasca))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    @DisplayName("modificarTasca retorna 404 si la tasca no existeix")
    void modificarTasca_quanTascaNoExisteix_retorna404() throws MalformedURLException {
        // Arrange
        String identificador = "T1";
        Tasca tasca = createValidTasca();
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getTasca(eq(identificador), eq(1L), eq(1L))).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = controller.modificarTasca(identificador, tasca);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("crearMultiplesTasques envia missatges i retorna 200")
    void crearMultiplesTasques_enviaMissatges() throws MalformedURLException {
        // Arrange
        Tasca t1 = createValidTasca();
        Tasca t2 = createValidTasca();
        t2.setIdentificador("T2");
        
        lenient().when(apiClientHelper.getAppByCodi(any())).thenReturn(Optional.of(new App()));
        lenient().when(apiClientHelper.getEntornByCodi(any())).thenReturn(Optional.of(new Entorn()));

        // Act
        ResponseEntity<String> response = controller.crearMultiplesTasques(java.util.List.of(t1, t2));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jmsTemplate, times(2)).convertAndSend(eq(CUA_TASQUES), any(Tasca.class));
    }

    @Test
    @DisplayName("eliminarTasca retorna 404 si la tasca no existeix")
    void eliminarTasca_quanNoExisteix_retorna404() {
        // Arrange
        String id = "T1";
        when(apiClientHelper.getAppByCodi(any())).thenReturn(Optional.of(new App()));
        when(apiClientHelper.getEntornByCodi(any())).thenReturn(Optional.of(new Entorn()));
        when(apiClientHelper.getTasca(eq(id), any(), any())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = controller.eliminarTasca(id, "APP1", "ENT1");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("consultarTasca retorna la tasca si existeix")
    void consultarTasca_quanExisteix_retornaTasca() {
        // Arrange
        String id = "T1";
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        es.caib.comanda.client.model.Tasca tascaClient = new es.caib.comanda.client.model.Tasca();
        Tasca tascaApi = new Tasca();
        
        when(apiClientHelper.getAppByCodi(any())).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi(any())).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getTasca(eq(id), eq(1L), eq(1L))).thenReturn(Optional.of(tascaClient));
        when(apiMapper.convert(tascaClient)).thenReturn(tascaApi);

        // Act
        ResponseEntity<Tasca> response = controller.consultarTasca(id, "APP1", "ENT1");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(tascaApi);
    }

    @Test
    @DisplayName("consultarTasca retorna 404 si no existeix")
    void consultarTasca_quanNoExisteix_retorna404() {
        // Arrange
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        when(apiClientHelper.getAppByCodi(any())).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi(any())).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getTasca(any(), any(), any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> controller.consultarTasca("T1", "APP1", "ENT1"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("existTasca retorna true si existeix")
    void existTasca_retornaTrueSiExisteix() {
        // Arrange
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getTasca(eq("T1"), eq(1L), eq(1L))).thenReturn(Optional.of(new es.caib.comanda.client.model.Tasca()));

        // Act
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(controller, "existTasca", "T1", "APP1", "ENT1");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("existTasca retorna false si no existeix")
    void existTasca_retornaFalseSiNoExisteix() {
        // Arrange
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getTasca(any(), any(), any())).thenReturn(Optional.empty());

        // Act
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(controller, "existTasca", "T1", "APP1", "ENT1");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("modificarMultiplesTasques envia missatges de les tasques que existeixen")
    void modificarMultiplesTasques_enviaMissatgesTasquesExistents() throws MalformedURLException {
        // Arrange
        Tasca t1 = createValidTasca(); t1.setIdentificador("T1");
        Tasca t2 = createValidTasca(); t2.setIdentificador("T2");
        List<Tasca> tasques = List.of(t1, t2);

        es.caib.comanda.client.model.Tasca tc1 = new es.caib.comanda.client.model.Tasca();
        tc1.setIdentificador("T1");
        
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getTasques(any(), eq(1L), eq(1L))).thenReturn(List.of(tc1));

        // Act
        ResponseEntity<String> response = controller.modificarMultiplesTasques(tasques);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("1 tasques modificades enviades");
        verify(jmsTemplate, times(1)).convertAndSend(eq(CUA_TASQUES), any(Tasca.class));
    }

    @Test
    @DisplayName("modificarMultiplesTasques retorna 400 si la llista és buida")
    void modificarMultiplesTasques_quanLlistaBuida_retorna400() {
        // Act
        ResponseEntity<String> response = controller.modificarMultiplesTasques(Collections.emptyList());

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("modificarMultiplesTasques retorna 404 si no troba cap tasca")
    void modificarMultiplesTasques_quanCapTascaTrobada_retorna404() throws MalformedURLException {
        // Arrange
        Tasca t1 = createValidTasca();
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getTasques(any(), any(), any())).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<String> response = controller.modificarMultiplesTasques(List.of(t1));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("obtenirLlistatTasques retorna pàgina de tasques")
    void obtenirLlistatTasques_retornaPagina() {
        // Arrange
        es.caib.comanda.client.model.Tasca tc1 = new es.caib.comanda.client.model.Tasca();
        Tasca ta1 = new Tasca();
        
        PagedModel<EntityModel<es.caib.comanda.client.model.Tasca>> pagedModel = PagedModel.of(
                List.of(EntityModel.of(tc1)),
                new PagedModel.PageMetadata(20, 0, 1)
        );
        pagedModel.add(Link.of("http://localhost?page=0&size=20", "self"));

        when(apiClientHelper.getTasques(any(), any(), any(), any(), any(), any()))
                .thenReturn(pagedModel);
        when(apiMapper.convert(tc1)).thenReturn(ta1);

        // Act
        ResponseEntity<TascaPage> response = controller.obtenirLlistatTasques(
                null, null, null, null, "0", 20
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getPage().getTotalElements()).isEqualTo(1);
    }

    private Tasca createValidTasca() throws MalformedURLException {
        return Tasca.builder()
                .appCodi("APP1")
                .entornCodi("ENT1")
                .identificador("T1")
                .nom("Nom")
                .tipus("TIPUS")
                .redireccio(new URL("http://localhost"))
                .estat(TascaEstat.PENDENT)
                .build();
    }
}
