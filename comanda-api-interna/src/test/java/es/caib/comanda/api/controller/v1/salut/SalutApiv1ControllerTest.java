package es.caib.comanda.api.controller.v1.salut;

import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.EstatSalutEnum;
import es.caib.comanda.model.v1.salut.SalutInfo;
import es.caib.comanda.ms.salut.helper.MonitorHelper;
import es.caib.comanda.ms.salut.helper.SalutHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SalutApiV1ControllerTest {

    @InjectMocks
    private SalutApiV1Controller controller;

    private MockedStatic<SalutHelper> salutHelperMock;
    private MockedStatic<MonitorHelper> monitorHelperMock;

    @BeforeEach
    void setUp() {
        salutHelperMock = mockStatic(SalutHelper.class);
        monitorHelperMock = mockStatic(MonitorHelper.class);
    }

    @AfterEach
    void tearDown() {
        salutHelperMock.close();
        monitorHelperMock.close();
    }

    @Test
    @DisplayName("salutInfo retorna informació de l'aplicació")
    void salutInfo_retornaInformacio() throws IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/comandaapi/interna/salut/v1/info");

        SalutHelper.BuildInfo buildInfo = SalutHelper.BuildInfo.builder()
                .version("1.0.0")
                .buildDate(OffsetDateTime.now())
                .build();
        salutHelperMock.when(SalutHelper::getBuildInfo).thenReturn(buildInfo);
        monitorHelperMock.when(MonitorHelper::getApplicationServerInfo).thenReturn("WildFly");

        // Act
        AppInfo info = controller.salutInfo(request);

        // Assert
        assertThat(info.getCodi()).isEqualTo("COM");
        assertThat(info.getVersio()).isEqualTo("1.0.0");
        assertThat(info.getVersioJboss()).isEqualTo("WildFly");
    }

    @Test
    @DisplayName("salut retorna l'estat de salut de l'aplicació")
    void salut_retornaEstatSalut() throws IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();

        SalutHelper.BuildInfo buildInfo = SalutHelper.BuildInfo.builder()
                .version("1.0.0")
                .buildDate(OffsetDateTime.now())
                .build();
        salutHelperMock.when(SalutHelper::getBuildInfo).thenReturn(buildInfo);
        monitorHelperMock.when(MonitorHelper::getInfoSistema).thenReturn(null);

        // Act
        SalutInfo salut = controller.salut(request, null, null);

        // Assert
        assertThat(salut.getCodi()).isEqualTo("COM");
        assertThat(salut.getEstatGlobal().getEstat()).isEqualTo(EstatSalutEnum.UP);
    }

    @Test
    @DisplayName("salut retorna informació del sistema si està disponible")
    void salut_retornaInfoSistema() throws IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();

        SalutHelper.BuildInfo buildInfo = SalutHelper.BuildInfo.builder()
                .version("1.0.0")
                .buildDate(OffsetDateTime.now())
                .build();
        salutHelperMock.when(SalutHelper::getBuildInfo).thenReturn(buildInfo);

        es.caib.comanda.model.server.monitoring.InformacioSistema infoServer = new es.caib.comanda.model.server.monitoring.InformacioSistema();
        infoServer.setProcessadors(8);
        infoServer.setMemoriaTotal("16000 MB");
        infoServer.setSistemaOperatiu("Linux");
        monitorHelperMock.when(MonitorHelper::getInfoSistema).thenReturn(infoServer);

        // Act
        SalutInfo salut = controller.salut(request, null, null);

        // Assert
        assertThat(salut.getInformacioSistema()).isNotNull();
        assertThat(salut.getInformacioSistema().getProcessadors()).isEqualTo(8);
        assertThat(salut.getInformacioSistema().getSistemaOperatiu()).isEqualTo("Linux");
    }
}
