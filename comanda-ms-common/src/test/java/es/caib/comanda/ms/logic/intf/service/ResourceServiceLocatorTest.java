package es.caib.comanda.ms.logic.intf.service;

import es.caib.comanda.ms.logic.intf.exception.ComponentNotFoundException;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceServiceLocatorTest {

    @Test
    void resourceServiceLocator_findReadOnlyAndMutable() {
        // Verifica la localització de serveis readonly i mutable a partir de la classe de resource.
        ResourceServiceLocator locator = new ResourceServiceLocator();
        TestMutableService service = new TestMutableService();
        org.springframework.test.util.ReflectionTestUtils.setField(locator, "resourceServices", List.of(service));

        assertThat(locator.getReadOnlyEntityResourceServiceForResourceClass(TestResource.class)).isSameAs(service);
        assertThat(locator.getMutableEntityResourceServiceForResourceClass(TestResource.class)).isSameAs(service);
        assertThatThrownBy(() -> locator.getReadOnlyEntityResourceServiceForResourceClass(OtherResource.class))
                .isInstanceOf(ComponentNotFoundException.class);
    }

    static class TestResource extends BaseResource<Long> {}
    static class OtherResource extends BaseResource<Long> {}

    static class TestMutableService implements MutableResourceService<TestResource, Long> {
        @Override public TestResource newResourceInstance() { return new TestResource(); }
        @Override public TestResource create(TestResource resource, Map<String, es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue> answers) { return resource; }
        @Override public TestResource update(Long id, TestResource resource, Map<String, es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue> answers) { return resource; }
        @Override public void delete(Long id, Map<String, es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue> answers) {}
        @Override public Map<String, Object> onChange(Long id, TestResource previous, String fieldName, Object fieldValue, Map<String, es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue> answers) { return Map.of(); }
        @Override public <P extends Serializable> Serializable artifactActionExec(Long id, String code, P params) { return null; }
        @Override public List<es.caib.comanda.ms.logic.intf.model.FieldOption> fieldEnumOptions(String fieldName, Map<String, String[]> requestParameterMap) { return List.of(); }
        @Override public TestResource getOne(Long id, String[] perspectives) { return new TestResource(); }
        @Override public Page<TestResource> findPage(String quickFilter, String filter, String[] namedQueries, String[] perspectives, Pageable pageable) { return Page.empty(); }
        @Override public es.caib.comanda.ms.logic.intf.model.DownloadableFile export(String quickFilter, String filter, String[] namedQueries, String[] perspectives, Pageable pageable, es.caib.comanda.ms.logic.intf.model.ExportField[] fields, es.caib.comanda.ms.logic.intf.model.ReportFileType fileType, OutputStream out) { return null; }
        @Override public es.caib.comanda.ms.logic.intf.model.DownloadableFile fieldDownload(Long id, String fieldName, OutputStream out) { return null; }
        @Override public List<es.caib.comanda.ms.logic.intf.model.ResourceArtifact> artifactFindAll(es.caib.comanda.ms.logic.intf.model.ResourceArtifactType type) { return List.of(); }
        @Override public es.caib.comanda.ms.logic.intf.model.ResourceArtifact artifactGetOne(es.caib.comanda.ms.logic.intf.model.ResourceArtifactType type, String code) { return null; }
        @Override public <P extends Serializable> Map<String, Object> artifactOnChange(es.caib.comanda.ms.logic.intf.model.ResourceArtifactType type, String code, Long id, P previous, String fieldName, Object fieldValue, Map<String, es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue> answers) { return Map.of(); }
        @Override public List<es.caib.comanda.ms.logic.intf.model.FieldOption> artifactFieldEnumOptions(es.caib.comanda.ms.logic.intf.model.ResourceArtifactType type, String code, String fieldName, Map<String, String[]> requestParameterMap) { return List.of(); }
        @Override public <P extends Serializable> List<?> artifactReportGenerateData(Long id, String code, P params) { return List.of(); }
        @Override public es.caib.comanda.ms.logic.intf.model.DownloadableFile artifactReportGenerateFile(String code, List<?> data, es.caib.comanda.ms.logic.intf.model.ReportFileType fileType, OutputStream out) { return null; }
    }
}
