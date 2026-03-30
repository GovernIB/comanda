package es.caib.comanda.api.controller.v1.log;

import es.caib.comanda.model.v1.log.FitxerContingut;
import es.caib.comanda.model.v1.log.FitxerInfo;
import es.caib.comanda.ms.log.helper.LogFileStream;
import es.caib.comanda.ms.log.helper.LogHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogApiV1ControllerTest {

    @Mock
    private ParametresHelper parametresHelper;

    @InjectMocks
    private LogApiV1Controller controller;

    private MockedStatic<LogHelper> logHelperMockedStatic;

    @BeforeEach
    void setUp() {
        logHelperMockedStatic = mockStatic(LogHelper.class);
        when(parametresHelper.getParametreText(any())).thenReturn("/tmp/logs");
    }

    @AfterEach
    void tearDown() {
        if (logHelperMockedStatic != null) {
            logHelperMockedStatic.close();
        }
    }

    @Test
    @DisplayName("getFitxers retorna llista de logs mapejada")
    void getFitxers_retornaLlista() {
        // Arrange
        es.caib.comanda.model.server.monitoring.FitxerInfo info = new es.caib.comanda.model.server.monitoring.FitxerInfo();
        info.setNom("test.log");
        info.setMida(100L);
        logHelperMockedStatic.when(() -> LogHelper.llistarFitxers(anyString(), anyString()))
                .thenReturn(Collections.singletonList(info));

        // Act
        List<FitxerInfo> result = controller.getFitxers();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("test.log");
        assertThat(result.get(0).getMida()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getFitxerByNom retorna contingut si existeix")
    void getFitxerByNom_quanExisteix_retornaContingut() {
        // Arrange
        es.caib.comanda.model.server.monitoring.FitxerContingut fitxer = new es.caib.comanda.model.server.monitoring.FitxerContingut();
        fitxer.setNom("test.log");
        fitxer.setContingut("contingut".getBytes());
        fitxer.setMida(9L);
        logHelperMockedStatic.when(() -> LogHelper.getFitxerByNom(anyString(), eq("test.log")))
                .thenReturn(fitxer);

        // Act
        FitxerContingut result = controller.getFitxerByNom("test.log");

        // Assert
        assertThat(result.getNom()).isEqualTo("test.log");
        assertThat(result.getContingut()).isEqualTo("contingut".getBytes());
        assertThat(result.getMida()).isEqualTo(9L);
    }

    @Test
    @DisplayName("getFitxerByNom retorna 404 si no existeix")
    void getFitxerByNom_quanNoExisteix_retorna404() {
        // Arrange
        logHelperMockedStatic.when(() -> LogHelper.getFitxerByNom(anyString(), anyString()))
                .thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> controller.getFitxerByNom("no.log"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("descarregarFitxerDirecte retorna StreamingResponseBody")
    void descarregarFitxerDirecte_retornaBody() throws IOException {
        // Arrange
        LogFileStream stream = mock(LogFileStream.class);
        when(stream.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        when(stream.getFileName()).thenReturn("test.log");
        when(stream.getSize()).thenReturn(4L);

        logHelperMockedStatic.when(() -> LogHelper.getFileStreamByNom(anyString(), eq("test.log")))
                .thenReturn(stream);

        // Act
        ResponseEntity<StreamingResponseBody> response = controller.descarregarFitxerDirecte("test.log");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentLength()).isEqualTo(4L);

        // Simular escriptura del body
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.getBody().writeTo(out);
        assertThat(out.toString()).isEqualTo("data");
    }

    @Test
    @DisplayName("getFitxerLinies retorna darreres línies")
    void getFitxerLinies_retornaLinies() {
        // Arrange
        List<String> linies = Collections.singletonList("line 1");
        logHelperMockedStatic.when(() -> LogHelper.readLastNLines(anyString(), eq("test.log"), eq(10L)))
                .thenReturn(linies);

        // Act
        List<String> result = controller.getFitxerLinies("test.log", 10L);

        // Assert
        assertThat(result).isEqualTo(linies);
    }
}
