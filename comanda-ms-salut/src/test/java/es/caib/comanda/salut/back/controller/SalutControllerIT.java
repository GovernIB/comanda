package es.caib.comanda.salut.back.controller;

import es.caib.comanda.ms.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;
import es.caib.comanda.ms.logic.intf.service.ResourceApiService;
import es.caib.comanda.salut.logic.intf.model.Salut;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.SmartValidator;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SalutController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
        properties = "spring.liquibase.enabled=false")
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = SalutControllerIT.TestWebMvcApp.class)
class SalutControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReadonlyResourceService<Salut, Long> readonlyResourceService;

    @MockBean
    private ResourceApiService resourceApiService;

    @MockBean
    private SmartValidator validator;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(SalutController.class)
    static class TestWebMvcApp {
    }

    @Test
    void getOne_quanExisteixSalut_retorna200IAmbElRecursSerialitzat() throws Exception {
        Salut salut = sampleSalut(7L, 101L);
        when(readonlyResourceService.getOne(7L, null)).thenReturn(salut);
        when(resourceApiService.permissionsCurrentUser(Salut.class, 7L)).thenReturn(fullPermissions());
        doNothing().when(resourceApiService).resourceRegister(Salut.class);

        mockMvc.perform(get("/api/saluts/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.entornAppId").value(101L))
                .andExpect(jsonPath("$.appEstat").value("UP"))
                .andExpect(jsonPath("$.bdEstat").value("WARN"));
    }

    @Test
    void find_quanHiHaPaginacio_retorna200IElementsDeLaPagina() throws Exception {
        Salut salut = sampleSalut(8L, 102L);
        when(readonlyResourceService.findPage(null, null, null, null, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(salut), PageRequest.of(0, 20), 1));
        when(resourceApiService.permissionsCurrentUser(Salut.class, null)).thenReturn(fullPermissions());
        doNothing().when(resourceApiService).resourceRegister(Salut.class);

        mockMvc.perform(get("/api/saluts")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.salutList[0].id").value(8L))
                .andExpect(jsonPath("$._embedded.salutList[0].entornAppId").value(102L))
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

    private static Salut sampleSalut(Long id, Long entornAppId) {
        Salut salut = new Salut();
        salut.setId(id);
        salut.setEntornAppId(entornAppId);
        salut.setData(LocalDateTime.of(2026, 3, 16, 8, 0));
        salut.setVersio("1.0.0");
        salut.setAppEstat(SalutEstat.UP);
        salut.setBdEstat(SalutEstat.WARN);
        salut.setAppLatencia(120);
        salut.setBdLatencia(45);
        return salut;
    }
}
