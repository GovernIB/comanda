package es.caib.comanda.ms.salut.controller;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.salut.back.controller.SalutController;
import es.caib.comanda.salut.logic.service.SalutSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class SalutControllerTest {

    @Mock
    private SalutSchedulerService schedulerService;

    @InjectMocks
    private SalutController salutController;

    private MockMvc mockMvc;
    private EntornApp entornApp;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(salutController).build();

        // Setup EntornApp
        entornApp = EntornApp.builder()
                .id(1L)
                .app(new AppRef(1L, "Test App"))
                .entorn(new EntornRef(1L, "Test Entorn"))
                .salutUrl("http://test.com/health")
                .salutInterval(15)
                .activa(true)
                .build();
    }

    @Test
    void testCreate() throws Exception {
        // Convert EntornApp to JSON
        String entornAppJson = "{"
                + "\"id\": 1,"
                + "\"app\": {\"id\": 1, \"nom\": \"Test App\"},"
                + "\"entorn\": {\"id\": 1, \"nom\": \"Test Entorn\"},"
                + "\"salutUrl\": \"http://test.com/health\","
                + "\"salutInterval\": 15,"
                + "\"activa\": true"
                + "}";

        // Act & Assert
        mockMvc.perform(post(BaseConfig.API_PATH + "/saluts/programar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(entornAppJson))
                .andExpect(status().isOk());

        // Verify that schedulerService.programarTasca was called with the EntornApp
        verify(schedulerService).programarTasca(any(EntornApp.class));
    }
}