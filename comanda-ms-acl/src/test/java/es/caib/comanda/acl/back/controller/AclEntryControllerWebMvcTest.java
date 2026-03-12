package es.caib.comanda.acl.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.acl.logic.intf.model.ResourceType;
import es.caib.comanda.acl.logic.intf.model.SubjectType;
import es.caib.comanda.acl.logic.intf.service.AclEntryService;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import es.caib.comanda.ms.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.ms.logic.intf.service.ResourceApiService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.SmartValidator;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AclEntryController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
        properties = "spring.liquibase.enabled=false")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("back")
@ContextConfiguration(classes = AclEntryControllerWebMvcTest.TestWebMvcApp.class)
class AclEntryControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AclEntryService aclEntryService;

    @MockBean
    private ResourceApiService resourceApiService;

    @MockBean
    private SmartValidator validator;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(AclEntryController.class)
    static class TestWebMvcApp {
    }

    @Test
    void getOne_quanExisteixRecurs_retorna200IAclSerialitzat() throws Exception {
        // Comprova que el GET heretat resol un recurs ACL i el serialitza dins la resposta HAL.
        AclEntry resource = sampleResource("pk-1", "anna");
        when(aclEntryService.getOne("pk-1", null)).thenReturn(resource);
        when(resourceApiService.permissionsCurrentUser(AclEntry.class, "pk-1")).thenReturn(fullPermissions());
        doNothing().when(resourceApiService).resourceRegister(AclEntry.class);

        mockMvc.perform(get("/api/aclEntries/{id}", "pk-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pk-1"))
                .andExpect(jsonPath("$.subjectValue").value("anna"));
    }

    @Test
    void find_quanEsConsultaPaginat_retorna200IAElementsDeLaPagina() throws Exception {
        // Verifica que el GET col·lecció heretat delega la paginació al servei i retorna els elements.
        AclEntry resource = sampleResource("pk-2", "beta");
        when(aclEntryService.findPage(null, null, null, null, PageRequest.of(0, 20))).thenReturn(new PageImpl<>(List.of(resource), PageRequest.of(0, 20), 1));
        when(resourceApiService.permissionsCurrentUser(AclEntry.class, null)).thenReturn(fullPermissions());
        doNothing().when(resourceApiService).resourceRegister(AclEntry.class);

        mockMvc.perform(get("/api/aclEntries")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.aclEntryList[0].id").value("pk-2"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void create_quanRecursValid_retorna201ILocalitzacioDelNouRecurs() throws Exception {
        // Comprova que el POST heretat crea el recurs ACL i publica la capçalera Location.
        AclEntry request = sampleResource(null, "gamma");
        AclEntry created = sampleResource("pk-3", "gamma");
        when(aclEntryService.create(any(AclEntry.class), any())).thenReturn(created);
        when(resourceApiService.permissionsCurrentUser(AclEntry.class, "pk-3")).thenReturn(fullPermissions());
        doNothing().when(resourceApiService).resourceRegister(AclEntry.class);

        mockMvc.perform(post("/api/aclEntries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/aclEntries/pk-3"))
                .andExpect(jsonPath("$.id").value("pk-3"))
                .andExpect(jsonPath("$.subjectValue").value("gamma"));
    }

    @Test
    void update_quanRecursValid_retorna200IRecursActualitzat() throws Exception {
        // Verifica que el PUT heretat actualitza el recurs i retorna l'estat final.
        AclEntry request = sampleResource(null, "delta");
        AclEntry updated = sampleResource("pk-4", "delta");
        when(aclEntryService.update(eq("pk-4"), any(AclEntry.class), any())).thenReturn(updated);
        when(resourceApiService.permissionsCurrentUser(AclEntry.class, "pk-4")).thenReturn(fullPermissions());
        doNothing().when(resourceApiService).resourceRegister(AclEntry.class);

        mockMvc.perform(put("/api/aclEntries/{id}", "pk-4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pk-4"))
                .andExpect(jsonPath("$.subjectValue").value("delta"));
    }

    @Test
    void delete_quanExisteixRecurs_retorna200IDelegaLEsborrat() throws Exception {
        // Comprova que el DELETE heretat delega l'esborrat al servei mutable ACL.
        doNothing().when(resourceApiService).resourceRegister(AclEntry.class);

        mockMvc.perform(delete("/api/aclEntries/{id}", "pk-5"))
                .andExpect(status().isOk());

        verify(aclEntryService).delete(eq("pk-5"), any());
    }

    @Test
    void anyPermissionGranted_quanElServeiConcedeixPermis_retornaTrue() throws Exception {
        // Verifica el endpoint específic que consulta si hi ha algun permís concedit sobre un recurs.
        when(aclEntryService.anyPermissionGranted(
                eq(ResourceType.ENTORN_APP),
                argThat(id -> "7".equals(String.valueOf(id))),
                eq(List.of(PermissionEnum.READ)))).thenReturn(true);
        doNothing().when(resourceApiService).resourceRegister(AclEntry.class);

        mockMvc.perform(get("/api/aclEntries/anyPermissionGranted")
                        .param("resourceType", "ENTORN_APP")
                        .param("resourceId", "7")
                        .param("permissions", "READ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void findIdsWithAnyPermission_quanHiHaCoincidencies_retornaLaLlistaDids() throws Exception {
        // Comprova el endpoint específic que retorna els ids permesos per a un conjunt de permisos.
        when(aclEntryService.findIdsWithAnyPermission(eq(ResourceType.ENTORN_APP), eq(List.of(PermissionEnum.WRITE)))).thenReturn(Set.of(3L, 5L));
        doNothing().when(resourceApiService).resourceRegister(AclEntry.class);

        mockMvc.perform(get("/api/aclEntries/findIdsWithAnyPermission")
                        .param("resourceType", "ENTORN_APP")
                        .param("permissions", "WRITE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    private static ResourcePermissions fullPermissions() {
        return ResourcePermissions.builder()
                .readGranted(true)
                .writeGranted(true)
                .createGranted(true)
                .deleteGranted(true)
                .build();
    }

    private static AclEntry sampleResource(String id, String subjectValue) {
        AclEntry resource = new AclEntry();
        resource.setId(id);
        resource.setSubjectType(SubjectType.USER);
        resource.setSubjectValue(subjectValue);
        resource.setResourceType(ResourceType.ENTORN_APP);
        resource.setResourceId(11L);
        resource.setReadAllowed(true);
        return resource;
    }
}
