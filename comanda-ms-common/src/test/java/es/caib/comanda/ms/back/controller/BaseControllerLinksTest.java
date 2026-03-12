package es.caib.comanda.ms.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.OnChangeEvent;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;
import es.caib.comanda.ms.logic.intf.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaseControllerLinksTest {

    @Test
    void readonlyController_buildsSingleAndCollectionLinks() {
        // Comprova que el controller readonly construeix els enllaços HAL per recurs i col·lecció.
        RealReadonlyController controller = new RealReadonlyController();
        @SuppressWarnings("unchecked")
        ReadonlyResourceService<LinkResource, Long> service = Mockito.mock(ReadonlyResourceService.class);
        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "validator", mock(SmartValidator.class));

        when(service.artifactFindAll(null)).thenReturn(List.of(
                new ResourceArtifact(ResourceArtifactType.REPORT, "REP", true, FormResource.class),
                new ResourceArtifact(ResourceArtifactType.REPORT, "REP_LIST", false, null)
        ));

        List<Link> single = controller.callBuildSingleResourceLinks(5L, ResourcePermissions.readOnly());
        assertThat(single).extracting(l -> l.getRel().value())
                .contains("self", "fieldDownload", "generate_REP");

        Page<LinkResource> page = new PageImpl<>(List.of(resource(1L), resource(2L)), PageRequest.of(1, 2), 6);
        List<Link> paged = controller.callBuildResourceCollectionLinks(PageRequest.of(1, 2), page, ResourcePermissions.readOnly());
        assertThat(paged).extracting(l -> l.getRel().value())
                .contains("self", "first", "previous", "next", "last", "toPageNumber");

        List<Link> empty = controller.callBuildResourceCollectionLinks(null, null, ResourcePermissions.readOnly());
        assertThat(empty).extracting(l -> l.getRel().value())
                .contains("self", "getOne", "find", "export", "artifacts", "generate_REP_LIST");
    }

    @Test
    void readonlyController_buildsArtifactLinksAndAuxMethods() throws Exception {
        // Verifica els enllaços d'artefacte i els helpers de formulari i onChange del controller readonly.
        RealReadonlyController controller = new RealReadonlyController();
        @SuppressWarnings("unchecked")
        ReadonlyResourceService<LinkResource, Long> service = Mockito.mock(ReadonlyResourceService.class);
        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());

        ResourceArtifact filterArtifact = new ResourceArtifact(ResourceArtifactType.FILTER, "FILTER1", false, FormResource.class);
        ResourceArtifact reportArtifact = new ResourceArtifact(ResourceArtifactType.REPORT, "REPORT1", false, FormResource.class);
        when(service.artifactGetOne(ResourceArtifactType.FILTER, "FILTER1")).thenReturn(filterArtifact);

        JsonUtil jsonUtil = new JsonUtil();
        ReflectionTestUtils.setField(jsonUtil, "objectMapper", new ObjectMapper());
        ApplicationContext appContext = mock(ApplicationContext.class);
        when(appContext.getBean(JsonUtil.class)).thenReturn(jsonUtil);
        jsonUtil.setApplicationContext(appContext);

        Link[] links = controller.callBuildSingleArtifactLinks(filterArtifact);
        assertThat(links).extracting(l -> l.getRel().value())
                .contains("self", "artifactFieldOptionsFind", "formValidate", "formOnChange", "filter_FILTER1");

        Link[] reportLinks = controller.callBuildSingleArtifactLinks(reportArtifact);
        assertThat(reportLinks).extracting(l -> l.getRel().value()).contains("generate_REPORT1");

        assertThat(controller.callGetArtifactFormClass(ResourceArtifactType.FILTER, "FILTER1")).isEqualTo(FormResource.class);

        SmartValidator validator = mock(SmartValidator.class);
        ReflectionTestUtils.setField(controller, "validator", validator);
        ObjectNode params = new ObjectMapper().createObjectNode().put("name", "abc");
        Serializable paramsObject = controller.callGetArtifactParamsAsObjectWithFormClass(FormResource.class, params);
        assertThat(paramsObject).isInstanceOf(FormResource.class);

        OnChangeEvent<Long> event = new OnChangeEvent<>(1L, new ObjectMapper().createObjectNode().put("name", "old"), "name", new ObjectMapper().createObjectNode().put("name", "new").get("name"), Map.of());
        FormResource previous = controller.callGetOnChangePrevious(event, FormResource.class);
        Object value = controller.callGetOnChangeFieldValue(event, FormResource.class);
        assertThat(previous.getName()).isEqualTo("old");
        assertThat(value).isEqualTo("new");
    }

    @Test
    void mutableController_buildsActionAndMutationLinks() {
        // Comprova els enllaços d'acció i les affordances de mutació del controller mutable.
        RealMutableController controller = new RealMutableController();
        @SuppressWarnings("unchecked")
        MutableResourceService<LinkResource, Long> service = Mockito.mock(MutableResourceService.class);
        ReflectionTestUtils.setField(controller, "readonlyResourceService", service);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "validator", mock(SmartValidator.class));

        when(service.artifactFindAll(null)).thenReturn(List.of(
                new ResourceArtifact(ResourceArtifactType.ACTION, "ACT1", true, FormResource.class),
                new ResourceArtifact(ResourceArtifactType.ACTION, "ACT_LIST", false, null)
        ));

        List<Link> single = controller.callBuildSingleResourceLinks(4L, ResourcePermissions.builder().readGranted(true).writeGranted(true).deleteGranted(true).build());
        assertThat(single).extracting(l -> l.getRel().value()).contains("self", "exec_ACT1");
        assertThat(single.get(0).getAffordances()).hasSizeGreaterThanOrEqualTo(3);

        List<Link> collection = controller.callBuildResourceCollectionLinks(null, null, ResourcePermissions.builder().readGranted(true).writeGranted(true).createGranted(true).build());
        assertThat(collection).extracting(l -> l.getRel().value()).contains("self", "fieldOptionsFind", "exec_ACT_LIST");

        ResourceArtifact actionArtifact = new ResourceArtifact(ResourceArtifactType.ACTION, "RUN", false, FormResource.class);
        Link[] artifactLinks = controller.callBuildSingleArtifactLinks(actionArtifact);
        assertThat(artifactLinks).extracting(l -> l.getRel().value()).contains("exec_RUN");
    }

    @Test
    void controllerPrivateHelpers_coverFilteringReferenceAndValidationPaths() throws Exception {
        // Exercita helpers interns de filtratge, ordenació, referències i validació de formularis.
        RealReadonlyController readonly = new RealReadonlyController();
        ReflectionTestUtils.setField(readonly, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(readonly, "validator", mock(SmartValidator.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("name", "abc");
        request.addParameter("count1", "1");
        request.addParameter("count2", "2");
        request.addParameter("ref", "9");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String merged = ReflectionTestUtils.invokeMethod(readonly, "filterWithFieldParameters", "status:'X'", FilterResource.class);
        assertThat(merged).contains("status:'X'").contains("name:'abc'").contains("count>:1").contains("count<:2").contains("ref.id:9");

        String likeExpr = ReflectionTestUtils.invokeMethod(readonly, "requestParamResourceFieldToSpringFilter", FilterResource.class, "name", request);
        assertThat(likeExpr).isEqualTo("(name:'abc')");

        PageRequest pageRequest = PageRequest.of(0, 5);
        Object sortedUnpaged = ReflectionTestUtils.invokeMethod(readonly, "fieldOptionsProcessedPageableWithResourceAnnotation", pageRequest, SortedResource.class);
        assertThat(sortedUnpaged).isInstanceOf(PageRequest.class);
        assertThat(((PageRequest) sortedUnpaged).getSort().isSorted()).isTrue();

        Optional<?> refField = ReflectionTestUtils.invokeMethod(readonly, "findReferenceFieldAndClass", NestedResource.class, "nested");
        assertThat(refField).isPresent();

        RealMutableController mutable = new RealMutableController();
        SmartValidator validator = mock(SmartValidator.class);
        ReflectionTestUtils.setField(mutable, "validator", validator);
        FormResource form = new FormResource();
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(form, "form");
        mutable.callFillResourceWithFieldsMap(form, Map.of("name", "filled"));
        assertThat(form.getName()).isEqualTo("filled");

        Mockito.doAnswer(inv -> {
            Errors errors = inv.getArgument(1);
            errors.rejectValue("name", "invalid");
            return null;
        }).when(validator).validate(any(), any(Errors.class), any());
        assertThatThrownBy(() -> mutable.callValidateResource(form, binding)).isInstanceOf(MethodArgumentNotValidException.class);
    }

    @Test
    void controllerHelperArtifactAndPermissionBranches_coverElsePaths() {
        // Cobreix camins alternatius d'artefactes sense formulari i permisos parcials sobre els links.
        RealReadonlyController readonly = new RealReadonlyController();
        @SuppressWarnings("unchecked")
        ReadonlyResourceService<LinkResource, Long> service = Mockito.mock(ReadonlyResourceService.class);
        ReflectionTestUtils.setField(readonly, "readonlyResourceService", service);

        when(service.artifactGetOne(ResourceArtifactType.REPORT, "NOFORM"))
                .thenReturn(new ResourceArtifact(ResourceArtifactType.REPORT, "NOFORM", false, null));

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(readonly, "getArtifactAwareResourceClass", ResourceArtifactType.REPORT, "NOFORM"))
                .isInstanceOf(es.caib.comanda.ms.logic.intf.exception.ArtifactFormNotFoundException.class);

        RealMutableController mutable = new RealMutableController();
        ReflectionTestUtils.setField(mutable, "readonlyResourceService", Mockito.mock(MutableResourceService.class));
        ReflectionTestUtils.setField(mutable, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(mutable, "validator", mock(SmartValidator.class));

        List<Link> deleteOnly = mutable.callBuildSingleResourceLinks(7L, ResourcePermissions.builder().readGranted(true).deleteGranted(true).build());
        assertThat(deleteOnly.get(0).getAffordances()).hasSizeGreaterThanOrEqualTo(2);

        List<Link> createOnly = mutable.callBuildResourceCollectionLinks(null, null, ResourcePermissions.builder().readGranted(true).createGranted(true).build());
        assertThat(createOnly.get(0).getAffordances()).hasSizeGreaterThanOrEqualTo(1);
    }

    private static LinkResource resource(Long id) {
        LinkResource r = new LinkResource();
        r.setId(id);
        r.setName("r" + id);
        return r;
    }

    @ResourceConfig(name = "linkResource")
    public static class LinkResource extends BaseResource<Long> {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class FormResource extends BaseResource<Long> {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class RefResource extends BaseResource<Long> {
        private String code;
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    public static class NestedResource extends BaseResource<Long> {
        private es.caib.comanda.ms.logic.intf.model.ResourceReference<RefResource, Long> nested;
        public es.caib.comanda.ms.logic.intf.model.ResourceReference<RefResource, Long> getNested() { return nested; }
        public void setNested(es.caib.comanda.ms.logic.intf.model.ResourceReference<RefResource, Long> nested) { this.nested = nested; }
    }

    @ResourceConfig(defaultSortFields = {
            @ResourceConfig.ResourceSort(field = "name", direction = Sort.Direction.DESC)
    })
    public static class SortedResource extends BaseResource<Long> {
        private String name;
    }

    public static class FilterResource extends BaseResource<Long> {
        private String name;
        private Integer count;
        private ResourceArtifactType status;
        private es.caib.comanda.ms.logic.intf.model.ResourceReference<RefResource, Long> ref;
    }

    static class RealReadonlyController extends BaseReadonlyResourceController<LinkResource, Long> {
        @Override protected Link getIndexLink() { return Link.of("/link").withRel("link"); }
        @Override public Class<LinkResource> getResourceClass() { return LinkResource.class; }

        List<Link> callBuildSingleResourceLinks(Long id, ResourcePermissions permissions) {
            return super.buildSingleResourceLinks(id, null, true, null, permissions);
        }

        List<Link> callBuildResourceCollectionLinks(org.springframework.data.domain.Pageable pageable, Page<?> page, ResourcePermissions permissions) {
            return super.buildResourceCollectionLinks(null, null, null, null, pageable, page, null, permissions);
        }

        Link[] callBuildSingleArtifactLinks(ResourceArtifact artifact) {
            return super.buildSingleArtifactLinks(artifact);
        }

        Class<? extends Serializable> callGetArtifactFormClass(ResourceArtifactType type, String code) {
            return super.getArtifactFormClass(type, code);
        }

        Serializable callGetArtifactParamsAsObjectWithFormClass(Class<?> formClass, ObjectNode params) throws Exception {
            return super.getArtifactParamsAsObjectWithFormClass(formClass, params, new BeanPropertyBindingResult(params, "params"));
        }

        <P extends Serializable> P callGetOnChangePrevious(OnChangeEvent<Long> event, Class<P> resourceClass) throws Exception {
            return super.getOnChangePrevious(event, resourceClass);
        }

        <P extends Serializable> Object callGetOnChangeFieldValue(OnChangeEvent<Long> event, Class<P> resourceClass) {
            return super.getOnChangeFieldValue(event, resourceClass);
        }
    }

    static class RealMutableController extends BaseMutableResourceController<LinkResource, Long> {
        @Override protected Link getIndexLink() { return Link.of("/link").withRel("link"); }
        @Override public Class<LinkResource> getResourceClass() { return LinkResource.class; }

        List<Link> callBuildSingleResourceLinks(Long id, ResourcePermissions permissions) {
            return super.buildSingleResourceLinks(id, null, true, null, permissions);
        }

        List<Link> callBuildResourceCollectionLinks(org.springframework.data.domain.Pageable pageable, Page<?> page, ResourcePermissions permissions) {
            return super.buildResourceCollectionLinks(null, null, null, null, pageable, page, null, permissions);
        }

        Link[] callBuildSingleArtifactLinks(ResourceArtifact artifact) {
            return super.buildSingleArtifactLinks(artifact);
        }

        void callFillResourceWithFieldsMap(Object target, Map<String, Object> fields) {
            super.fillResourceWithFieldsMap(target, fields);
        }

        <T extends es.caib.comanda.ms.logic.intf.model.Resource<?>> void callValidateResource(T resource, BeanPropertyBindingResult bindingResult) throws MethodArgumentNotValidException {
            super.validateResource(resource, bindingResult, 0);
        }
    }
}
