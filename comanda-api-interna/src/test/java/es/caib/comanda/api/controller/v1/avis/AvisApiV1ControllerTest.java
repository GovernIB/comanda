package es.caib.comanda.api.controller.v1.avis;

import es.caib.comanda.api.helper.ApiClientHelper;
import es.caib.comanda.api.util.ApiMapper;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.model.v1.avis.Avis;
import es.caib.comanda.model.v1.avis.AvisPage;
import es.caib.comanda.model.v1.avis.AvisTipus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static es.caib.comanda.base.config.Cues.CUA_AVISOS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvisApiV1ControllerTest {

    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private ApiClientHelper apiClientHelper;
    @Mock
    private ApiMapper apiMapper;

    @InjectMocks
    private AvisApiV1Controller controller;

    @Test
    @DisplayName("crearAvis envia missatge a JMS si les dades són vàlides")
    void crearAvis_quanDadesValides_enviaMissatge() throws MalformedURLException {
        // Arrange
        Avis avis = createValidAvis();
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(new App()));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(new Entorn()));

        // Act
        ResponseEntity<String> response = controller.crearAvis(avis);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jmsTemplate).convertAndSend(eq(CUA_AVISOS), eq(avis));
    }

    @Test
    @DisplayName("crearAvis retorna 400 si falten camps obligatoris")
    void crearAvis_quanFaltenCamps_retorna400() {
        // Arrange
        Avis avis = new Avis();

        // Act
        ResponseEntity<String> response = controller.crearAvis(avis);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(jmsTemplate);
    }

    @Test
    @DisplayName("modificarAvis envia missatge si l'avís existeix")
    void modificarAvis_quanExisteix_enviaMissatge() throws MalformedURLException {
        // Arrange
        String id = "A1";
        Avis avis = createValidAvis();
        avis.setIdentificador(id);
        
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(new App()));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(new Entorn()));
        when(apiClientHelper.getAvis(eq(id), any(), any())).thenReturn(Optional.of(new es.caib.comanda.client.model.Avis()));

        // Act
        ResponseEntity<String> response = controller.modificarAvis(id, avis);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jmsTemplate).convertAndSend(eq(CUA_AVISOS), eq(avis));
    }

    @Test
    @DisplayName("eliminarAvis envia missatge amb flag esborrar")
    void eliminarAvis_quanExisteix_enviaMissatgeEsborrar() {
        // Arrange
        String id = "A1";
        String appCodi = "APP1";
        String entornCodi = "ENT1";
        
        when(apiClientHelper.getAppByCodi(appCodi)).thenReturn(Optional.of(new App()));
        when(apiClientHelper.getEntornByCodi(entornCodi)).thenReturn(Optional.of(new Entorn()));
        when(apiClientHelper.getAvis(eq(id), any(), any())).thenReturn(Optional.of(new es.caib.comanda.client.model.Avis()));

        // Act
        ResponseEntity<String> response = controller.eliminarAvis(id, appCodi, entornCodi);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jmsTemplate).convertAndSend(eq(CUA_AVISOS), any(Avis.class));
    }

    @Test
    @DisplayName("modificarAvis retorna 404 si l'avís no existeix")
    void modificarAvis_quanNoExisteix_retorna404() throws MalformedURLException {
        // Arrange
        String id = "A1";
        Avis avis = createValidAvis();
        when(apiClientHelper.getAppByCodi(any())).thenReturn(Optional.of(new App()));
        when(apiClientHelper.getEntornByCodi(any())).thenReturn(Optional.of(new Entorn()));
        when(apiClientHelper.getAvis(eq(id), any(), any())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = controller.modificarAvis(id, avis);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("crearMultiplesAvisos envia missatges i retorna 200")
    void crearMultiplesAvisos_enviaMissatges() throws MalformedURLException {
        // Arrange
        Avis a1 = createValidAvis();
        Avis a2 = createValidAvis();
        a2.setIdentificador("A2");
        
        lenient().when(apiClientHelper.getAppByCodi(any())).thenReturn(Optional.of(new App()));
        lenient().when(apiClientHelper.getEntornByCodi(any())).thenReturn(Optional.of(new Entorn()));

        // Act
        ResponseEntity<String> response = controller.crearMultiplesAvisos(java.util.List.of(a1, a2));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jmsTemplate, times(2)).convertAndSend(eq(CUA_AVISOS), any(Avis.class));
    }

    @Test
    @DisplayName("consultarAvis retorna l'avís si existeix")
    void consultarAvis_quanExisteix_retornaAvis() {
        // Arrange
        String id = "A1";
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        es.caib.comanda.client.model.Avis avisClient = new es.caib.comanda.client.model.Avis();
        Avis avisApi = new Avis();
        
        when(apiClientHelper.getAppByCodi(any())).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi(any())).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getAvis(eq(id), eq(1L), eq(1L))).thenReturn(Optional.of(avisClient));
        when(apiMapper.convert(avisClient)).thenReturn(avisApi);

        // Act
        ResponseEntity<Avis> response = controller.consultarAvis(id, "APP1", "ENT1");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(avisApi);
    }

    @Test
    @DisplayName("consultarAvis retorna 404 si no existeix")
    void consultarAvis_quanNoExisteix_retorna404() {
        // Arrange
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        when(apiClientHelper.getAppByCodi(any())).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi(any())).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getAvis(any(), any(), any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> controller.consultarAvis("A1", "APP1", "ENT1"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException)e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("modificarMultiplesAvisos retorna 400 si llista buida")
    void modificarMultiplesAvisos_quanLlistaBuida_retorna400() {
        // Act
        ResponseEntity<String> response = controller.modificarMultiplesAvisos(new ArrayList<>());

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Llista buida");
    }

    @Test
    @DisplayName("modificarMultiplesAvisos retorna 400 si falten camps")
    void modificarMultiplesAvisos_quanFaltenCamps_retorna400() {
        // Arrange
        Avis avis = new Avis();
        avis.setIdentificador("A1");

        // Act
        ResponseEntity<String> response = controller.modificarMultiplesAvisos(List.of(avis));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("identificador, entornCodi i appCodi informats");
    }

    @Test
    @DisplayName("modificarMultiplesAvisos retorna 400 si diferents entorns o apps")
    void modificarMultiplesAvisos_quanDiferentsEntornsApps_retorna400() throws MalformedURLException {
        // Arrange
        Avis a1 = createValidAvis();
        Avis a2 = createValidAvis();
        a2.setAppCodi("APP2");

        // Act
        ResponseEntity<String> response = controller.modificarMultiplesAvisos(List.of(a1, a2));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("mateix entornCodi i appCodi");
    }

    @Test
    @DisplayName("modificarMultiplesAvisos retorna 404 si cap existent")
    void modificarMultiplesAvisos_quanCapExistent_retorna404() throws MalformedURLException {
        // Arrange
        Avis a1 = createValidAvis();
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getAvisos(anySet(), eq(1L), eq(1L))).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<String> response = controller.modificarMultiplesAvisos(List.of(a1));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("No s'ha trobat cap avís a modificar");
    }

    @Test
    @DisplayName("modificarMultiplesAvisos envia missatges i retorna 200")
    void modificarMultiplesAvisos_quanAlgunsExistents_enviaMissatgesIRetorna200() throws MalformedURLException {
        // Arrange
        Avis a1 = createValidAvis(); a1.setIdentificador("A1");
        Avis a2 = createValidAvis(); a2.setIdentificador("A2");
        
        App app = new App(); ReflectionTestUtils.setField(app, "id", 1L);
        Entorn entorn = new Entorn(); ReflectionTestUtils.setField(entorn, "id", 1L);
        
        es.caib.comanda.client.model.Avis avisClient1 = new es.caib.comanda.client.model.Avis();
        ReflectionTestUtils.setField(avisClient1, "identificador", "A1");
        
        when(apiClientHelper.getAppByCodi("APP1")).thenReturn(Optional.of(app));
        when(apiClientHelper.getEntornByCodi("ENT1")).thenReturn(Optional.of(entorn));
        when(apiClientHelper.getAvisos(anySet(), eq(1L), eq(1L))).thenReturn(List.of(avisClient1));

        // Act
        ResponseEntity<String> response = controller.modificarMultiplesAvisos(List.of(a1, a2));

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("1 avisos modificats");
        assertThat(response.getBody()).contains("1 ignorats");
        verify(jmsTemplate, times(1)).convertAndSend(eq(CUA_AVISOS), eq(a1));
        verify(jmsTemplate, never()).convertAndSend(eq(CUA_AVISOS), eq(a2));
    }

    @Test
    @DisplayName("obtenirLlistatAvisos retorna la pàgina d'avisos")
    void obtenirLlistatAvisos_retornaPagina() {
        // Arrange
        es.caib.comanda.client.model.Avis avisClient = new es.caib.comanda.client.model.Avis();
        Avis avisApi = new Avis();
        EntityModel<es.caib.comanda.client.model.Avis> model = EntityModel.of(avisClient);
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(20, 0, 1, 1);
        PagedModel<EntityModel<es.caib.comanda.client.model.Avis>> pagedModel = PagedModel.of(List.of(model), metadata, Link.of("http://localhost", "self"));

        when(apiClientHelper.getAvisos(anyString(), anyString(), any(), any(), anyString(), anyInt())).thenReturn(pagedModel);
        when(apiMapper.convert(avisClient)).thenReturn(avisApi);

        // Act
        ResponseEntity<AvisPage> response = controller.obtenirLlistatAvisos("", "", null, null, "0", 20);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getPage().getTotalElements()).isEqualTo(1);
        assertThat(response.getBody().getLinks()).hasSize(1);
        assertThat(response.getBody().getLinks().get(0).getRel()).isEqualTo("self");
    }

    private Avis createValidAvis() throws MalformedURLException {
        return Avis.builder()
                .appCodi("APP1")
                .entornCodi("ENT1")
                .identificador("A1")
                .nom("Títol")
                .tipus(AvisTipus.INFO)
                .redireccio(new URL("http://localhost"))
                .build();
    }
}
