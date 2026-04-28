package es.caib.comanda.ms.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.FieldOption;
import es.caib.comanda.ms.logic.intf.model.OnChangeEvent;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import es.caib.comanda.ms.logic.intf.model.Resource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;
import es.caib.comanda.ms.logic.intf.service.ResourceApiService;
import es.caib.comanda.ms.logic.intf.service.ResourceServiceLocator;
import es.caib.comanda.ms.logic.intf.util.JsonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseControllersTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
        ReflectionTestUtils.setField(ResourceServiceLocator.class, "applicationContext", null);
    }

    @Test
    void baseReadonly_getOne_quanExisteix_retornOk() {
        // Verifica que el controller readonly retorna un recurs existent amb resposta HTTP 200.
        TestReadonlyController controller = new TestReadonlyController();
        ReadonlyResourceService<TestResource, Long> service = mockReadonlyService();
        ResourceApiService resourceApiService = Mockito.mock(ResourceApiService.class);
        SmartValidator validator = Mockito.mock(SmartValidator.class);

        TestResource resource = new TestResource();
        resource.setId(10L);
        when(service.getOne(10L, null)).thenReturn(resource);
        when(resourceApiService.permissionsCurrentUser(TestResource.class, 10L)).thenReturn(ResourcePermissions.readOnly());

        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "resourceApiService", resourceApiService);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "validator", validator);

        var response = controller.getOne(10L, null);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().getId()).isEqualTo(10L);
    }

    @Test
    void baseReadonly_find_quanPageableNull_retornBuit() {
        // Comprova que la cerca sense paginació retorna només l'estructura i els enllaços bàsics.
        TestReadonlyController controller = new TestReadonlyController();
        ReadonlyResourceService<TestResource, Long> service = mockReadonlyService();
        ResourceApiService resourceApiService = Mockito.mock(ResourceApiService.class);
        SmartValidator validator = Mockito.mock(SmartValidator.class);

        when(resourceApiService.permissionsCurrentUser(TestResource.class, null)).thenReturn(ResourcePermissions.readOnly());
        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "resourceApiService", resourceApiService);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "validator", validator);

        var response = controller.find(null, null, null, null, null);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
    }

    @Test
    void baseReadonly_getAnswersFromHeaderOrRequest_quanHeaderIRequest_fusioCorrecta() {
        // Valida la fusió de respostes provinents del header HTTP i del payload de la petició.
        TestReadonlyController controller = new TestReadonlyController();
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "httpHeaderAnswers", "Bb-Answers");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Bb-Answers", "{\"fromHeader\":true,\"fromRequest\":\"override\"}");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Map<String, Object> fromRequest = Map.of("fromRequest", "req", "newReq", "x");
        Map<String, AnswerRequiredException.AnswerValue> answers = controller.getAnswersFromHeaderOrRequest(fromRequest);

        assertThat(answers).isNotNull();
        assertThat(answers.get("fromHeader").valueAsBoolean()).isTrue();
        assertThat(answers.get("fromRequest").getStringValue()).isEqualTo("override");
        assertThat(answers.get("newReq").getStringValue()).isEqualTo("x");
    }

    @Test
    void baseMutable_createUpdateDelete_quanValid_retornEstatsCorrectes() throws Exception {
        // Exercita els fluxos de creació, actualització i esborrat del controller mutable.
        TestMutableController controller = new TestMutableController();
        MutableResourceService<TestResource, Long> service = mockMutableService();
        ResourceApiService resourceApiService = Mockito.mock(ResourceApiService.class);
        SmartValidator validator = Mockito.mock(SmartValidator.class);

        TestResource newResource = new TestResource();
        newResource.setName("n1");
        TestResource created = new TestResource();
        created.setId(55L);
        created.setName("n1");

        when(service.create(any(), any())).thenReturn(created);
        when(service.update(eq(55L), any(), any())).thenAnswer(inv -> inv.getArgument(1));
        when(resourceApiService.permissionsCurrentUser(TestResource.class, 55L)).thenReturn(ResourcePermissions.builder()
                .readGranted(true).writeGranted(true).createGranted(true).deleteGranted(true).build());

        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "resourceApiService", resourceApiService);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "validator", validator);
        ReflectionTestUtils.setField(controller, "httpHeaderAnswers", "Bb-Answers");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        var createResponse = controller.create(newResource, new BeanPropertyBindingResult(newResource, "resource"));
        assertThat(createResponse.getStatusCodeValue()).isEqualTo(201);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getContent().getId()).isEqualTo(55L);

        TestResource update = new TestResource();
        update.setName("updated");
        var updateResponse = controller.update(55L, update, new BeanPropertyBindingResult(update, "resource"));
        assertThat(updateResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(update.getId()).isEqualTo(55L);

        var deleteResponse = controller.delete(55L);
        assertThat(deleteResponse.getStatusCodeValue()).isEqualTo(200);
        verify(service).delete(eq(55L), any());
    }

    @Test
    void baseUtilsController_quanFormatsDiferents_respostaCorrecta() throws Exception {
        // Comprova els endpoints utilitaris de ping, token i configuració runtime per React/Vite.
        TestUtilsController controller = new TestUtilsController();
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("testProps", Map.of(
                "REACT_APP_API", "/api",
                "APP_URL", "http://localhost",
                "VITE_API", "/v-api")));
        ReflectionTestUtils.setField(controller, "env", env);
        ReflectionTestUtils.setField(controller, "servletContext", new MockServletContext());

        assertThat(controller.ping().getStatusCodeValue()).isEqualTo(200);
        assertThat(controller.authToken().getBody()).contains("window.__AUTH_TOKEN__");

        String react = controller.systemEnvironment("reactapp").getBody();
        assertThat(react).contains("window.__RUNTIME_CONFIG__");
        assertThat(react).contains("REACT_APP_API").contains("REACT_APP_APP_URL");

        String vite = controller.systemEnvironment("vite").getBody();
        assertThat(vite).contains("VITE_API");
    }

    @Test
    void baseReadonly_artifactsICampsEspecials_retornenDadesEsperades() throws Exception {
        // Verifica la descàrrega de camps i la consulta d'artefactes publicats pel controller readonly.
        TestReadonlyController controller = new TestReadonlyController();
        ReadonlyResourceService<TestResource, Long> service = mockReadonlyService();
        ResourceApiService resourceApiService = Mockito.mock(ResourceApiService.class);
        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "resourceApiService", resourceApiService);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "validator", Mockito.mock(SmartValidator.class));

        ResourceArtifact reportArtifact = new ResourceArtifact(ResourceArtifactType.REPORT, "REP", true, TestResource.class);
        when(service.fieldDownload(eq(7L), eq("doc"), any()))
                .thenReturn(new DownloadableFile("doc.txt", "text/plain", "file".getBytes(StandardCharsets.UTF_8)));
        when(service.artifactFindAll(null)).thenReturn(List.of(reportArtifact));
        when(service.artifactGetOne(ResourceArtifactType.REPORT, "REP")).thenReturn(reportArtifact);

        var downloadResponse = controller.fieldDownload(7L, "doc");
        assertThat(downloadResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(downloadResponse.getHeaders().getContentDisposition().getFilename()).isEqualTo("doc.txt");

        var artifactsResponse = controller.artifacts();
        assertThat(artifactsResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(artifactsResponse.getBody()).isNotNull();
        assertThat(artifactsResponse.getBody().getContent()).hasSize(1);

        var artifactResponse = controller.artifactGetOne(ResourceArtifactType.REPORT, "REP");
        assertThat(artifactResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(artifactResponse.getBody()).isNotNull();
        assertThat(artifactResponse.getBody().getContent().getCode()).isEqualTo("REP");
    }

    @Test
    void baseReadonly_artifactEndpoints_processenOnChangeValidacioEnumsIReports() throws Exception {
        // Exercita l'onChange, la validació, els enums i la generació d'informes dels artefactes readonly.
        TestReadonlyController controller = new TestReadonlyController();
        ReadonlyResourceService<TestResource, Long> service = mockReadonlyService();
        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "resourceApiService", Mockito.mock(ResourceApiService.class));
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "validator", Mockito.mock(SmartValidator.class));
        installJsonUtil(new ObjectMapper());

        ResourceArtifact filterArtifact = new ResourceArtifact(ResourceArtifactType.FILTER, "FILTER", true, TestResource.class);
        ResourceArtifact reportArtifact = new ResourceArtifact(ResourceArtifactType.REPORT, "REP", true, TestResource.class);
        when(service.artifactGetOne(ResourceArtifactType.FILTER, "FILTER")).thenReturn(filterArtifact);
        when(service.artifactGetOne(ResourceArtifactType.REPORT, "REP")).thenReturn(reportArtifact);
        when(service.artifactOnChange(eq(ResourceArtifactType.FILTER), eq("FILTER"), eq(9L), any(), eq("name"), eq("new"), any()))
                .thenReturn(Map.of("name", "server-value"));
        when(service.artifactFieldEnumOptions(eq(ResourceArtifactType.FILTER), eq("FILTER"), eq("status"), any()))
                .thenReturn(List.of(new FieldOption("OPEN", "Open")));
        Mockito.doReturn(List.of(Map.of("value", 1)))
                .when(service).artifactReportGenerateData(eq(null), eq("REP"), any());
        when(service.artifactReportGenerateFile(eq("REP"), any(), eq(ReportFileType.CSV), any()))
                .thenReturn(new DownloadableFile("report.csv", "text/csv", "report".getBytes(StandardCharsets.UTF_8)));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("scope", "all");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ObjectNode previous = new ObjectMapper().createObjectNode().put("name", "old");
        ObjectNode current = new ObjectMapper().createObjectNode().put("name", "new");
        OnChangeEvent<Long> onChangeEvent = new OnChangeEvent<>(9L, previous, "name", current.get("name"), Map.of());

        var onChangeResponse = controller.artifactFormOnChange(ResourceArtifactType.FILTER, "FILTER", onChangeEvent);
        assertThat(onChangeResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(onChangeResponse.getBody()).contains("server-value");

        var validateResponse = controller.artifactFormValidate(
                ResourceArtifactType.FILTER,
                "FILTER",
                new ObjectMapper().createObjectNode().put("name", "ok"),
                new BeanPropertyBindingResult(new Object(), "params"));
        assertThat(validateResponse.getStatusCodeValue()).isEqualTo(200);

        var enumFindResponse = controller.artifactFieldEnumOptionsFind(ResourceArtifactType.FILTER, "FILTER", "status");
        assertThat(enumFindResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(enumFindResponse.getBody()).isNotNull();
        assertThat(enumFindResponse.getBody().getContent()).hasSize(1);

        var enumOneResponse = controller.artifactFieldEnumOptionsGetOne(ResourceArtifactType.FILTER, "FILTER", "status", "OPEN");
        assertThat(enumOneResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(enumOneResponse.getBody()).isNotNull();
        assertThat(enumOneResponse.getBody().getContent().getValue()).isEqualTo("OPEN");

        var reportJsonResponse = controller.artifactReportGenerate(
                "REP",
                null,
                new ObjectMapper().createObjectNode().put("name", "ok"),
                new BeanPropertyBindingResult(new Object(), "params"));
        assertThat(reportJsonResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(reportJsonResponse.getHeaders().getContentType().toString()).contains("application/json");
        try (InputStream body = reportJsonResponse.getBody().getInputStream()) {
            assertThat(new String(body.readAllBytes(), StandardCharsets.UTF_8)).contains("value");
        }

        var reportFileResponse = controller.artifactReportGenerate(
                "REP",
                ReportFileType.CSV,
                new ObjectMapper().createObjectNode().put("name", "ok"),
                new BeanPropertyBindingResult(new Object(), "params"));
        assertThat(reportFileResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(reportFileResponse.getHeaders().getContentDisposition().getFilename()).isEqualTo("report.csv");
    }

    @Test
    void baseReadonly_artifactEnumEndpoints_cobreixenBuitsINotFound() {
        // Comprova els camins alternatius dels enums d'artefacte quan no hi ha opcions o no existeix el valor demanat.
        TestReadonlyController controller = new TestReadonlyController();
        ReadonlyResourceService<TestResource, Long> service = mockReadonlyService();
        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "resourceApiService", Mockito.mock(ResourceApiService.class));
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "validator", Mockito.mock(SmartValidator.class));

        when(service.artifactFieldEnumOptions(eq(ResourceArtifactType.FILTER), eq("FILTER"), eq("status"), any())).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var emptyResponse = controller.artifactFieldEnumOptionsFind(ResourceArtifactType.FILTER, "FILTER", "status");
        assertThat(emptyResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(emptyResponse.getBody()).isNotNull();
        assertThat(emptyResponse.getBody().getContent()).isEmpty();

        when(service.artifactFieldEnumOptions(eq(ResourceArtifactType.FILTER), eq("FILTER"), eq("status"), any()))
                .thenReturn(List.of(new FieldOption("OPEN", "Open")));
        var missingResponse = controller.artifactFieldEnumOptionsGetOne(ResourceArtifactType.FILTER, "FILTER", "status", "CLOSED");
        assertThat(missingResponse.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void baseReadonly_artifactFieldOptions_retornenPaginacioIElement() {
        // Verifica la resolució d'opcions de ResourceReference per formularis d'informe i filtre.
        TestReadonlyController controller = new TestReadonlyController();
        ReadonlyResourceService<TestResource, Long> service = mockReadonlyService();
        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "resourceApiService", Mockito.mock(ResourceApiService.class));
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "validator", Mockito.mock(SmartValidator.class));

        ResourceArtifact reportArtifact = new ResourceArtifact(ResourceArtifactType.REPORT, "REP_OPTIONS", true, OptionForm.class);
        ResourceArtifact filterArtifact = new ResourceArtifact(ResourceArtifactType.FILTER, "FILTER_OPTIONS", true, OptionForm.class);
        when(service.artifactGetOne(ResourceArtifactType.REPORT, "REP_OPTIONS")).thenReturn(reportArtifact);
        when(service.artifactGetOne(ResourceArtifactType.FILTER, "FILTER_OPTIONS")).thenReturn(filterArtifact);

        OptionValueResource item = new OptionValueResource();
        item.setId(3L);
        item.setLabel("Etiqueta");
        installResourceLocator(new OptionValueReadonlyService(item));

        var reportFindResponse = controller.artifactReportFieldOptionsFind(
                "REP_OPTIONS",
                "related",
                "abc",
                null,
                null,
                null,
                PageRequest.of(0, 10));
        assertThat(reportFindResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(reportFindResponse.getBody()).isNotNull();
        assertThat(reportFindResponse.getBody().getContent()).hasSize(1);

        var filterOneResponse = controller.artifactFilterFieldOptionsGetOne(
                "FILTER_OPTIONS",
                "related",
                3L,
                null);
        assertThat(filterOneResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(filterOneResponse.getBody()).isNotNull();
        assertThat(filterOneResponse.getBody().getContent().getId()).isEqualTo(3L);
    }

    @Test
    void baseMutable_patchOnChangeEnumsIActions_retornenRespostaCorrecta() throws Exception {
        // Valida el patch parcial, l'onChange, les opcions enum i l'execució d'accions del controller mutable.
        TestMutableController controller = new TestMutableController();
        MutableResourceService<TestResource, Long> service = mockMutableService();
        ResourceApiService resourceApiService = Mockito.mock(ResourceApiService.class);
        SmartValidator validator = Mockito.mock(SmartValidator.class);
        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "resourceApiService", resourceApiService);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "validator", validator);
        ReflectionTestUtils.setField(controller, BaseReadonlyResourceController.class, "validator", validator, SmartValidator.class);
        installJsonUtil(new ObjectMapper());

        TestResource existing = new TestResource();
        existing.setId(12L);
        existing.setName("old");
        ResourceArtifact actionArtifact = new ResourceArtifact(ResourceArtifactType.ACTION, "RUN", true, TestResource.class);

        when(service.getOne(12L, null)).thenReturn(existing);
        when(service.update(eq(12L), any(), any())).thenAnswer(inv -> inv.getArgument(1));
        when(service.onChange(eq(12L), any(), eq("name"), eq("new"), any()))
                .thenReturn(Map.of("name", "changed"));
        when(service.fieldEnumOptions(eq("status"), any())).thenReturn(List.of(new FieldOption("DONE", "Done")));
        when(service.artifactGetOne(ResourceArtifactType.ACTION, "RUN")).thenReturn(actionArtifact);
        when(service.artifactActionExec(eq(12L), eq("RUN"), any())).thenReturn("done");
        when(resourceApiService.permissionsCurrentUser(TestResource.class, 12L)).thenReturn(ResourcePermissions.builder()
                .readGranted(true).writeGranted(true).deleteGranted(true).build());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("scope", "all");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        var patchResponse = controller.patch(
                12L,
                new ObjectMapper().createObjectNode().put("name", "patched"),
                new BeanPropertyBindingResult(existing, "resource"));
        assertThat(patchResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(patchResponse.getBody()).isNotNull();
        assertThat(patchResponse.getBody().getContent().getName()).isEqualTo("patched");

        OnChangeEvent<Long> onChangeEvent = new OnChangeEvent<>(
                12L,
                new ObjectMapper().createObjectNode().put("name", "old"),
                "name",
                new ObjectMapper().createObjectNode().put("name", "new").get("name"),
                Map.of());
        var onChangeResponse = controller.onChange(onChangeEvent);
        assertThat(onChangeResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(onChangeResponse.getBody()).contains("changed");

        var enumFindResponse = controller.fieldEnumOptionsFind("status");
        assertThat(enumFindResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(enumFindResponse.getBody()).isNotNull();
        assertThat(enumFindResponse.getBody().getContent()).hasSize(1);

        var enumOneResponse = controller.fieldEnumOptionsGetOne("status", "DONE");
        assertThat(enumOneResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(enumOneResponse.getBody()).isNotNull();
        assertThat(enumOneResponse.getBody().getContent().getValue()).isEqualTo("DONE");

        var actionResponse = controller.artifactActionExec(
                12L,
                "RUN",
                new ObjectMapper().createObjectNode().put("name", "param"),
                new BeanPropertyBindingResult(new Object(), "params"));
        assertThat(actionResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(actionResponse.getBody()).isEqualTo("done");
    }

    private static void installJsonUtil(ObjectMapper objectMapper) {
        JsonUtil jsonUtil = new JsonUtil();
        ReflectionTestUtils.setField(jsonUtil, "objectMapper", objectMapper);
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        when(applicationContext.getBean(JsonUtil.class)).thenReturn(jsonUtil);
        jsonUtil.setApplicationContext(applicationContext);
    }

    private static void installResourceLocator(OptionValueReadonlyService optionService) {
        ResourceServiceLocator locator = new ResourceServiceLocator();
        ReflectionTestUtils.setField(locator, "resourceServices", List.of(optionService));
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        when(applicationContext.getBean(ResourceServiceLocator.class)).thenReturn(locator);
        locator.setApplicationContext(applicationContext);
    }

    @SuppressWarnings("unchecked")
    private static ReadonlyResourceService<TestResource, Long> mockReadonlyService() {
        return Mockito.mock(ReadonlyResourceService.class);
    }

    @SuppressWarnings("unchecked")
    private static MutableResourceService<TestResource, Long> mockMutableService() {
        return Mockito.mock(MutableResourceService.class);
    }

    @ResourceConfig(
            name = "testResource",
            artifacts = {
                    @es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact(type = ResourceArtifactType.REPORT, code = "REP_OPTIONS", formClass = OptionForm.class),
                    @es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact(type = ResourceArtifactType.FILTER, code = "FILTER_OPTIONS", formClass = OptionForm.class)
            })
    public static class TestResource extends BaseResource<Long> {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @ResourceConfig(name = "optionValue", descriptionField = "label")
    public static class OptionValueResource extends BaseResource<Long> {
        private String label;
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    public static class OptionForm implements Serializable {
        private ResourceReference<OptionValueResource, Long> related;
        public ResourceReference<OptionValueResource, Long> getRelated() { return related; }
        public void setRelated(ResourceReference<OptionValueResource, Long> related) { this.related = related; }
    }

    static class TestReadonlyController extends BaseReadonlyResourceController<TestResource, Long> {
        @Override
        protected Link getIndexLink() {
            return Link.of("/test").withRel("test");
        }

        @Override
        public Class<TestResource> getResourceClass() {
            return TestResource.class;
        }

        @Override
        protected List<Link> buildSingleResourceLinks(Serializable id, String[] perspective, boolean withDownloadLink, Link singleResourceSelfLink, ResourcePermissions resourcePermissions) {
            return List.of(Link.of("/test/" + id).withSelfRel());
        }

        @Override
        protected List<Link> buildResourceCollectionLinks(String quickFilter, String filter, String[] namedQuery, String[] perspective, Pageable pageable, org.springframework.data.domain.Page<?> page, Link resourceCollectionBaseSelfLink, ResourcePermissions resourcePermissions) {
            return List.of(Link.of("/test").withSelfRel());
        }
    }

    static class TestMutableController extends BaseMutableResourceController<TestResource, Long> {
        @Override
        protected Link getIndexLink() {
            return Link.of("/test").withRel("test");
        }

        @Override
        public Class<TestResource> getResourceClass() {
            return TestResource.class;
        }

        @Override
        protected List<Link> buildSingleResourceLinks(Serializable id, String[] perspective, boolean withDownloadLink, Link singleResourceSelfLink, ResourcePermissions resourcePermissions) {
            return List.of(Link.of("/test/" + id).withSelfRel());
        }

        @Override
        protected List<Link> buildResourceCollectionLinks(String quickFilter, String filter, String[] namedQuery, String[] perspective, Pageable pageable, org.springframework.data.domain.Page<?> page, Link resourceCollectionBaseSelfLink, ResourcePermissions resourcePermissions) {
            return List.of(Link.of("/test").withSelfRel());
        }
    }

    static class TestUtilsController extends BaseUtilsController {
        @Override
        protected String getAuthToken() {
            return "token123";
        }

        @Override
        protected boolean isReactAppMappedFrontProperty(String propertyName) {
            return "APP_URL".equals(propertyName);
        }

        @Override
        protected String getReactAppMappedFrontProperty(String propertyName) {
            return "REACT_APP_" + propertyName;
        }

        @Override
        protected boolean isViteMappedFrontProperty(String propertyName) {
            return "APP_URL".equals(propertyName);
        }

        @Override
        protected String getViteMappedFrontProperty(String propertyName) {
            return "VITE_" + propertyName;
        }
    }

    static class OptionValueReadonlyService implements ReadonlyResourceService<OptionValueResource, Long> {
        private final OptionValueResource item;

        OptionValueReadonlyService(OptionValueResource item) {
            this.item = item;
        }

        @Override
        public OptionValueResource getOne(Long id, String[] perspectives) {
            return item;
        }

        @Override
        public Page<OptionValueResource> findPage(String quickFilter, String filter, String[] namedQueries, String[] perspectives, Pageable pageable) {
            return new PageImpl<>(List.of(item), pageable, 1);
        }

        @Override
        public DownloadableFile export(String quickFilter, String filter, String[] namedQueries, String[] perspectives, Pageable pageable, es.caib.comanda.ms.logic.intf.model.ExportField[] fields, ReportFileType fileType, java.io.OutputStream out) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DownloadableFile fieldDownload(Long id, String fieldName, java.io.OutputStream out) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ResourceArtifact> artifactFindAll(ResourceArtifactType type) {
            return List.of();
        }

        @Override
        public ResourceArtifact artifactGetOne(ResourceArtifactType type, String code) {
            return null;
        }

        @Override
        public <P extends Serializable> Map<String, Object> artifactOnChange(ResourceArtifactType type, String code, Long id, P previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers) {
            return Map.of();
        }

        @Override
        public List<FieldOption> artifactFieldEnumOptions(ResourceArtifactType type, String code, String fieldName, Map<String, String[]> requestParameterMap) {
            return List.of();
        }

        @Override
        public <P extends Serializable> List<?> artifactReportGenerateData(Long id, String code, P params) {
            return List.of();
        }

        @Override
        public DownloadableFile artifactReportGenerateFile(String code, List<?> data, ReportFileType fileType, java.io.OutputStream out) {
            throw new UnsupportedOperationException();
        }
    }

}
