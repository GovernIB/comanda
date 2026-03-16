package es.caib.comanda.tasques.back.controller;

import es.caib.comanda.ms.back.error.GlobalExceptionHandler;
import es.caib.comanda.ms.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;
import es.caib.comanda.ms.logic.intf.service.ResourceApiService;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import es.caib.comanda.tasques.logic.intf.service.TascaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.SmartValidator;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TascaController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
        properties = "spring.liquibase.enabled=false")
@AutoConfigureMockMvc(addFilters = false) // Desactiva seguretat per simplificar l'integració
@ContextConfiguration(classes = TascaControllerIT.TestWebMvcApp.class)
class TascaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReadonlyResourceService<Tasca, Long> readonlyResourceService;

    @MockBean
    private ResourceApiService resourceApiService;

    @MockBean
    private SmartValidator validator;

    @MockBean
    private org.springframework.context.MessageSource messageSource;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({TascaController.class, GlobalExceptionHandler.class})
    static class TestWebMvcApp {
    }

    @Test
    @WithMockUser
    void getOne_quanExisteix_retorna200IAmbTasca() throws Exception {
        // Arrange
        Tasca tasca = sampleTasca(1L, 101L);
        when(readonlyResourceService.getOne(1L, null)).thenReturn(tasca);
        when(resourceApiService.permissionsCurrentUser(Tasca.class, 1L)).thenReturn(fullPermissions());
        doNothing().when(resourceApiService).resourceRegister(Tasca.class);

        // Act & Assert
        mockMvc.perform(get("/api/tasques/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Tasca de prova"))
                .andExpect(jsonPath("$.identificador").value("ID1"));
    }

    @Test
    void getOne_quanNoExisteix_retornaNotFound() throws Exception {
        // Arrange
        when(readonlyResourceService.getOne(eq(99L), any()))
                .thenThrow(new es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException(Tasca.class, "99"));
        doNothing().when(resourceApiService).resourceRegister(Tasca.class);

        // Act & Assert
        mockMvc.perform(get("/api/tasques/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void find_quanHiHaPaginacio_retorna200IElementsDeLaPagina() throws Exception {
        Tasca tasca = sampleTasca(8L, 102L);
        when(readonlyResourceService.findPage(null, null, null, null, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(tasca), PageRequest.of(0, 20), 1));
        when(resourceApiService.permissionsCurrentUser(Tasca.class, null)).thenReturn(fullPermissions());
        doNothing().when(resourceApiService).resourceRegister(Tasca.class);

        mockMvc.perform(get("/api/tasques")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tascaList[0].id").value(8L))
                .andExpect(jsonPath("$._embedded.tascaList[0].entornAppId").value(102L))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    private static ResourcePermissions fullPermissions() {
        return ResourcePermissions.builder()
                .readGranted(true)
                .writeGranted(true)
                .createGranted(true)
                .deleteGranted(true)
                .build();
    }

    private static Tasca sampleTasca(Long id, Long entornAppId) {
        Tasca tasca = new Tasca();
        tasca.setId(id);
        tasca.setNom("Tasca de prova");
        tasca.setIdentificador("ID" + id);
        tasca.setEntornAppId(entornAppId);
        tasca.setDataInici(LocalDateTime.now());
        return tasca;
    }
}
