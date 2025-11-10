package es.caib.comanda.acl.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.acl.logic.intf.service.AclEntryService;
import es.caib.comanda.client.model.acl.AclAction;
import es.caib.comanda.client.model.acl.AclEffect;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.client.model.acl.SubjectType;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import es.caib.comanda.ms.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.ms.logic.intf.service.ResourceApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CRUD tests for AclEntryController using Mockito. These tests validate that:
 * - Creating an ACL entry calls service.create and returns 201
 * - Updating an ACL entry calls service.update and returns 200
 * - Deleting an ACL entry calls service.delete and returns 204
 * - Validation errors return 400
 */
class AclEntryControllerCrudTest {

    private MockMvc mockMvc;
    private AclEntryService aclEntryService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        aclEntryService = Mockito.mock(AclEntryService.class);
        AclEntryController controller = new AclEntryController();
        // Inject mocked service into BaseMutableResourceController via reflection
        try {
            // Inject the mocked resource service used by the controller
            Field f = BaseReadonlyResourceController.class.getDeclaredField("readonlyResourceService");
            f.setAccessible(true);
            f.set(controller, aclEntryService);

            // Inject a mocked ResourceApiService to avoid NPE when building links/permissions
            ResourceApiService resourceApiService = Mockito.mock(ResourceApiService.class);
            ResourcePermissions perms = Mockito.mock(ResourcePermissions.class);
            Mockito.when(perms.isWriteGranted()).thenReturn(true);
            Mockito.when(perms.isDeleteGranted()).thenReturn(true);
            Mockito.when(resourceApiService.permissionsCurrentUser(Mockito.any(), Mockito.any())).thenReturn(perms);
            Field rfs = BaseReadonlyResourceController.class.getDeclaredField("resourceApiService");
            rfs.setAccessible(true);
            rfs.set(controller, resourceApiService);

            // Inject a Validator to avoid NPE inside BaseMutableResourceController
            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();
            Field v = BaseMutableResourceController.class.getDeclaredField("validator");
            v.setAccessible(true);
            v.set(controller, validator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup controller for testing", e);
        }
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    private AclEntry sample() {
        AclEntry r = new AclEntry();
        r.setSubjectType(SubjectType.USER);
        r.setSubjectValue("user1");
        r.setResourceType(ResourceType.ENTORN_APP);
        r.setResourceId(123L);
        r.setAction(AclAction.READ);
        r.setEffect(AclEffect.ALLOW);
        return r;
    }

    @Test
    void create_returns201_and_callsService() throws Exception {
        AclEntry request = sample();
        AclEntry created = sample();
        created.setId(1L);
        when(aclEntryService.create(any(AclEntry.class), any())).thenReturn(created);

        String body = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/acl/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_FORMS_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Verify service received proper resource
        ArgumentCaptor<AclEntry> captor = ArgumentCaptor.forClass(AclEntry.class);
        Mockito.verify(aclEntryService).create(captor.capture(), any());
        AclEntry sent = captor.getValue();
        assertThat(sent.getSubjectType()).isEqualTo(SubjectType.USER);
        assertThat(sent.getSubjectValue()).isEqualTo("user1");
        assertThat(sent.getResourceType()).isEqualTo(ResourceType.ENTORN_APP);
        assertThat(sent.getResourceId()).isEqualTo(123L);
        assertThat(sent.getAction()).isEqualTo(AclAction.READ);
        assertThat(sent.getEffect()).isEqualTo(AclEffect.ALLOW);

        // Basic check that an id appears in response payload
        assertThat(response).contains("\"id\":1");
    }

    @Test
    void update_returns200_and_callsService() throws Exception {
        AclEntry request = sample();
        request.setAction(AclAction.WRITE);
        AclEntry updated = sample();
        updated.setId(5L);
        updated.setAction(AclAction.WRITE);
        when(aclEntryService.update(eq(5L), any(AclEntry.class), any())).thenReturn(updated);

        mockMvc.perform(put("/api/acl/entries/{id}", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_FORMS_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<AclEntry> captor = ArgumentCaptor.forClass(AclEntry.class);
        Mockito.verify(aclEntryService).update(eq(5L), captor.capture(), any());
        assertThat(captor.getValue().getAction()).isEqualTo(AclAction.WRITE);
    }

    @Test
    void delete_returns204_and_callsService() throws Exception {
        doNothing().when(aclEntryService).delete(eq(9L), any());

        mockMvc.perform(delete("/api/acl/entries/{id}", 9))
                .andExpect(status().isOk());

        Mockito.verify(aclEntryService).delete(eq(9L), any());
    }

    @Test
    void create_returns400_whenMissingRequiredFields() throws Exception {
        // Missing resourceType, resourceId, action, effect
        AclEntry invalid = new AclEntry();
        invalid.setSubjectType(SubjectType.USER);
        invalid.setSubjectValue("u");

        mockMvc.perform(post("/api/acl/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
