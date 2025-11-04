package es.caib.comanda.acl.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.acl.logic.intf.dto.AclCheckRequest;
import es.caib.comanda.acl.logic.intf.dto.AclCheckResponse;
import es.caib.comanda.acl.logic.intf.service.AclEntryService;
import es.caib.comanda.acl.persist.enums.AclAction;
import es.caib.comanda.acl.persist.enums.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AclControllerTest {

    private MockMvc mockMvc;
    private AclEntryService aclEntryService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        aclEntryService = Mockito.mock(AclEntryService.class);
        AclController controller = new AclController(aclEntryService);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void check_returnsAllowedTrue_whenServiceGrants() throws Exception {
        when(aclEntryService.checkPermission(anyString(), anyList(), any(), anyLong(), any()))
                .thenReturn(true);

        AclCheckRequest req = new AclCheckRequest();
        req.setUser("user1");
        req.setRoles(Arrays.asList("ROLE_A"));
        req.setResourceType(ResourceType.ENTORN_APP);
        req.setResourceId(11L);
        req.setAction(AclAction.READ);

        String json = objectMapper.writeValueAsString(req);

        String response = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AclCheckResponse body = objectMapper.readValue(response, AclCheckResponse.class);
        assertThat(body.isAllowed()).isTrue();

        // Verify arguments passed to service
        ArgumentCaptor<String> userCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<java.util.List<String>> rolesCap = ArgumentCaptor.forClass(java.util.List.class);
        ArgumentCaptor<ResourceType> typeCap = ArgumentCaptor.forClass(ResourceType.class);
        ArgumentCaptor<Long> idCap = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<AclAction> actionCap = ArgumentCaptor.forClass(AclAction.class);
        Mockito.verify(aclEntryService).checkPermission(
                userCap.capture(), rolesCap.capture(), typeCap.capture(), idCap.capture(), actionCap.capture());
        assertThat(userCap.getValue()).isEqualTo("user1");
        assertThat(rolesCap.getValue()).containsExactly("ROLE_A");
        assertThat(typeCap.getValue()).isEqualTo(ResourceType.ENTORN_APP);
        assertThat(idCap.getValue()).isEqualTo(11L);
        assertThat(actionCap.getValue()).isEqualTo(AclAction.READ);
    }

    @Test
    void check_returnsAllowedFalse_whenServiceDenies() throws Exception {
        when(aclEntryService.checkPermission(anyString(), anyList(), any(), anyLong(), any()))
                .thenReturn(false);

        AclCheckRequest req = new AclCheckRequest();
        req.setRoles(Arrays.asList("ROLE_X")); // sense user
        req.setResourceType(ResourceType.DASHBOARD);
        req.setResourceId(7L);
        req.setAction(AclAction.WRITE);

        String response = mockMvc.perform(post("/api/acl/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AclCheckResponse body = objectMapper.readValue(response, AclCheckResponse.class);
        assertThat(body.isAllowed()).isFalse();
    }
}
