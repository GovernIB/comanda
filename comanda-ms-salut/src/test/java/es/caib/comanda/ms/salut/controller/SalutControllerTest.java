package es.caib.comanda.ms.salut.controller;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.salut.back.controller.SalutController;
import es.caib.comanda.salut.logic.service.SalutSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
                .activa(true)
                .build();
    }
}