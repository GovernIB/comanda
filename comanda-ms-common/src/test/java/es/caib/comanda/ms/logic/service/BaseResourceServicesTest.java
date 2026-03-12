package es.caib.comanda.ms.logic.service;

import es.caib.comanda.ms.logic.helper.BasePermissionHelper;
import es.caib.comanda.ms.logic.helper.JasperReportsHelper;
import es.caib.comanda.ms.logic.helper.ObjectMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceReferenceToEntityHelper;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceField;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.ArtifactNotFoundException;
import es.caib.comanda.ms.logic.intf.exception.FieldArtifactNotFoundException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.intf.exception.ResourceAlreadyExistsException;
import es.caib.comanda.ms.logic.intf.exception.ResourceFieldNotFoundException;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.FieldOption;
import es.caib.comanda.ms.logic.intf.model.FileReference;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.ms.persist.entity.ReorderableEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseResourceServicesTest {

    @Test
    void readonlyService_getOneIPage_apliquenMapeigIPerspectives() {
        // Comprova que el servei readonly obté i pagina recursos aplicant el mapeig i la perspectiva registrada.
        TestReadonlyService service = new TestReadonlyService();
        @SuppressWarnings("unchecked")
        BaseRepository<ReadonlyEntity, Long> repository = Mockito.mock(BaseRepository.class);
        ReflectionTestUtils.setField(service, "entityRepository", repository);

        ReadonlyEntity one = entity(1L, "one");
        ReadonlyEntity two = entity(2L, "two");
        when(repository.findOne(any(Specification.class))).thenReturn(Optional.of(one));
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(one, two), PageRequest.of(0, 2, Sort.by("name")), 2));

        ReadonlyResource single = service.getOne(1L, new String[]{"DETAIL"});
        assertThat(single.getId()).isEqualTo(1L);
        assertThat(single.getName()).isEqualTo("one");
        assertThat(single.getView()).isEqualTo("DETAIL-1");

        Page<ReadonlyResource> page = service.findPage("o", "name:'o'", new String[]{"NQ"}, new String[]{"DETAIL"}, PageRequest.of(0, 2, Sort.by("name")));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(ReadonlyResource::getView).containsExactly("DETAIL-1", "DETAIL-2");
    }

    @Test
    void readonlyService_fieldDownloadIArtifacts_cobreixenCaminsPrincipals() throws Exception {
        // Valida la descàrrega de camps i la descoberta d'artefactes, incloent casos d'error controlats.
        TestReadonlyService service = new TestReadonlyService();
        @SuppressWarnings("unchecked")
        BaseRepository<ReadonlyEntity, Long> repository = Mockito.mock(BaseRepository.class);
        ReflectionTestUtils.setField(service, "entityRepository", repository);
        ReflectionTestUtils.setField(service, "permissionHelper", allowAllPermissions());

        ReadonlyEntity entity = entity(7L, "doc");
        when(repository.findOne(any(Specification.class))).thenReturn(Optional.of(entity));

        DownloadableFile downloadableFile = service.fieldDownload(7L, "attachment", OutputStream.nullOutputStream());
        assertThat(downloadableFile.getName()).isEqualTo("attachment.txt");

        List<es.caib.comanda.ms.logic.intf.model.ResourceArtifact> artifacts = service.artifactFindAll(null);
        assertThat(artifacts).extracting(es.caib.comanda.ms.logic.intf.model.ResourceArtifact::getCode)
                .contains("DETAIL", "REP", "FIL");

        var artifact = service.artifactGetOne(ResourceArtifactType.REPORT, "REP");
        assertThat(artifact.getType()).isEqualTo(ResourceArtifactType.REPORT);
        assertThat(artifact.getFormClass()).isEqualTo(ReportParams.class);

        List<FieldOption> options = service.artifactFieldEnumOptions(ResourceArtifactType.FILTER, "FIL", "name", Map.of("scope", new String[]{"all"}));
        assertThat(options).extracting(FieldOption::getValue).containsExactly("OPT");

        assertThatThrownBy(() -> service.fieldDownload(7L, "plain", OutputStream.nullOutputStream()))
                .isInstanceOf(FieldArtifactNotFoundException.class);
        assertThatThrownBy(() -> service.fieldDownload(7L, "missing", OutputStream.nullOutputStream()))
                .isInstanceOf(ResourceFieldNotFoundException.class);
        assertThatThrownBy(() -> service.artifactGetOne(ResourceArtifactType.REPORT, "MISSING"))
                .isInstanceOf(ArtifactNotFoundException.class);
    }

    @Test
    void readonlyService_artifactOnChangeIReports_cobreixenGeneracio() throws Exception {
        // Verifica l'onChange dels formularis d'artefacte i la generació de dades i fitxers d'informes.
        TestReadonlyService service = new TestReadonlyService();
        @SuppressWarnings("unchecked")
        BaseRepository<ReadonlyEntity, Long> repository = Mockito.mock(BaseRepository.class);
        JasperReportsHelper jasperReportsHelper = mock(JasperReportsHelper.class);
        ReflectionTestUtils.setField(service, "entityRepository", repository);
        ReflectionTestUtils.setField(service, "jasperReportsHelper", jasperReportsHelper);
        ReflectionTestUtils.setField(service, "permissionHelper", allowAllPermissions());

        ReadonlyEntity entity = entity(9L, "report");
        when(repository.findOne(any(Specification.class))).thenReturn(Optional.of(entity));
        when(jasperReportsHelper.generate(eq(ReadonlyResource.class), eq("REPJ"), any(URL.class), any(), any(), eq(null), eq(ReportFileType.PDF), any()))
                .thenReturn(new DownloadableFile("fallback.pdf", "application/pdf", "pdf".getBytes(StandardCharsets.UTF_8)));

        Map<String, Object> onchange = service.artifactOnChange(
                ResourceArtifactType.FILTER,
                "FIL",
                9L,
                filterParams("old"),
                "name",
                "new",
                Map.of());
        assertThat(onchange).containsEntry("name", "new-filtered");

        List<?> data = service.artifactReportGenerateData(9L, "REP", reportParams("abc"));
        assertThat(data).hasSize(1);
        assertThat(data.get(0)).isEqualTo("REP-abc-9");

        DownloadableFile generated = service.artifactReportGenerateFile("REP", List.of("x"), ReportFileType.CSV, OutputStream.nullOutputStream());
        assertThat(generated.getName()).isEqualTo("REP.csv");

        DownloadableFile fallback = service.artifactReportGenerateFile("REPJ", List.of("x"), ReportFileType.PDF, OutputStream.nullOutputStream());
        assertThat(fallback.getName()).isEqualTo("fallback.pdf");

        assertThatThrownBy(() -> service.artifactReportGenerateData(9L, "ERR", reportParams("x")))
                .isInstanceOf(ReportGenerationException.class);
    }

    @Test
    void mutableService_createUpdateDeleteGestionenMapeigFitxersIRepositori() {
        // Exercita els fluxos CRUD del servei mutable amb mapeig, fitxers associats i operacions de repositori.
        TestMutableService service = new TestMutableService();
        @SuppressWarnings("unchecked")
        BaseRepository<MutableEntity, Long> repository = Mockito.mock(BaseRepository.class);
        ResourceReferenceToEntityHelper referenceHelper = mock(ResourceReferenceToEntityHelper.class);
        ReflectionTestUtils.setField(service, "entityRepository", repository);
        ReflectionTestUtils.setField(service, "permissionHelper", allowAllPermissions());
        ReflectionTestUtils.setField(service, "resourceReferenceToEntityHelper", referenceHelper);

        MutableEntity existing = mutableEntity(5L, "before", 10L);
        when(referenceHelper.getReferencedEntitiesForResource(any(), eq(MutableEntity.class))).thenReturn(Collections.emptyMap());
        when(repository.findOne(any(Specification.class))).thenReturn(Optional.of(existing), Optional.of(existing));
        when(repository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.merge(any())).thenAnswer(inv -> inv.getArgument(0));

        MutableResource create = mutableResource(null, "created", 15L);
        create.setAttachment(new FileReference("upload.txt", "x".getBytes(StandardCharsets.UTF_8), "text/plain", 1));
        MutableResource created = service.create(create, Map.of());
        assertThat(created.getName()).isEqualTo("created");
        assertThat(service.fileManager.savedNames).contains("attachment");

        MutableResource update = mutableResource(5L, "updated", 25L);
        update.setAttachment(new FileReference("upload2.txt", "y".getBytes(StandardCharsets.UTF_8), "text/plain", 1));
        MutableResource updated = service.update(5L, update, Map.of());
        assertThat(updated.getName()).isEqualTo("updated");
        assertThat(existing.getName()).isEqualTo("updated");

        service.delete(5L, Map.of());
        verify(repository).delete(existing);
        verify(repository).flush();
        assertThat(service.fileManager.deletedNames).contains("attachment");
    }

    @Test
    void mutableService_onChangeActionsOpcionsIArtifacts_funcionen() {
        // Comprova l'onChange, les accions, les opcions enumerades i els artefactes del servei mutable.
        TestMutableService service = new TestMutableService();
        @SuppressWarnings("unchecked")
        BaseRepository<MutableEntity, Long> repository = Mockito.mock(BaseRepository.class);
        ReflectionTestUtils.setField(service, "entityRepository", repository);
        ReflectionTestUtils.setField(service, "permissionHelper", allowAllPermissions());

        MutableEntity entity = mutableEntity(4L, "entity", 10L);
        when(repository.findOne(any(Specification.class))).thenReturn(Optional.of(entity));

        Map<String, Object> changes = service.onChange(4L, mutableResource(4L, "old", 10L), "name", "new", Map.of());
        assertThat(changes).containsEntry("name", "new-processed");

        Serializable result = service.artifactActionExec(4L, "ACT", actionParams("go"));
        assertThat(result).isEqualTo("ACT-go-4");

        List<FieldOption> options = service.fieldEnumOptions("status", Map.of());
        assertThat(options).extracting(FieldOption::getValue).containsExactly("READY");

        List<es.caib.comanda.ms.logic.intf.model.ResourceArtifact> artifacts = service.artifactFindAll(ResourceArtifactType.ACTION);
        assertThat(artifacts).extracting(es.caib.comanda.ms.logic.intf.model.ResourceArtifact::getCode).containsExactly("ACT");

        assertThat(service.artifactGetOne(ResourceArtifactType.ACTION, "ACT").getFormClass()).isEqualTo(ActionParams.class);
        assertThatThrownBy(() -> service.artifactActionExec(4L, "ACT", actionParams("boom")))
                .isInstanceOf(ActionExecutionException.class);
    }

    @Test
    void mutableService_reorderIClausDuplicades_cobreixenBranques() {
        // Valida la reordenació d'entitats i la detecció de claus duplicades en creació.
        TestMutableService service = new TestMutableService();
        @SuppressWarnings("unchecked")
        BaseRepository<MutableEntity, Long> repository = Mockito.mock(BaseRepository.class);
        ReflectionTestUtils.setField(service, "entityRepository", repository);
        ReflectionTestUtils.setField(service, "permissionHelper", allowAllPermissions());
        ReflectionTestUtils.setField(service, "resourceReferenceToEntityHelper", mock(ResourceReferenceToEntityHelper.class));

        MutableEntity first = mutableEntity(1L, "a", 10L);
        MutableEntity second = mutableEntity(2L, "b", 20L);
        service.reorderableLines = new ArrayList<>(List.of(first, second));

        MutableEntity inserted = mutableEntity(3L, "c", null);
        boolean reordered = service.callReorder(inserted, 15L, null, true, false);
        assertThat(reordered).isTrue();
        assertThat(first.getOrder()).isEqualTo(10L);
        assertThat(inserted.getOrder()).isEqualTo(20L);
        assertThat(second.getOrder()).isEqualTo(30L);

        when(repository.findOne(any(Specification.class))).thenReturn(Optional.of(mutableEntity(8L, "dup", 5L)));
        assertThatThrownBy(() -> service.create(mutableResource(8L, "dup", 5L), Map.of()))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    private static BasePermissionHelper allowAllPermissions() {
        BasePermissionHelper permissionHelper = mock(BasePermissionHelper.class);
        when(permissionHelper.checkResourceArtifactPermission(any(), any(), any())).thenReturn(true);
        when(permissionHelper.checkResourcePermission(any(), any(), any())).thenReturn(true);
        when(permissionHelper.checkResourcePermission(any(), any(), any(), any())).thenReturn(true);
        return permissionHelper;
    }

    private static ReadonlyEntity entity(Long id, String name) {
        ReadonlyEntity entity = new ReadonlyEntity();
        entity.setId(id);
        entity.setName(name);
        return entity;
    }

    private static MutableEntity mutableEntity(Long id, String name, Long order) {
        MutableEntity entity = new MutableEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setOrder(order);
        return entity;
    }

    private static MutableResource mutableResource(Long id, String name, Long order) {
        MutableResource resource = new MutableResource();
        resource.setId(id);
        resource.setName(name);
        resource.setSequence(order);
        return resource;
    }

    private static ReportParams reportParams(String name) {
        ReportParams params = new ReportParams();
        params.setName(name);
        return params;
    }

    private static FilterParams filterParams(String name) {
        FilterParams params = new FilterParams();
        params.setName(name);
        return params;
    }

    private static ActionParams actionParams(String name) {
        ActionParams params = new ActionParams();
        params.setName(name);
        return params;
    }

    @ResourceConfig(
            name = "readonlyResource",
            quickFilterFields = {"name"},
            artifacts = {
                    @ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = "DETAIL"),
                    @ResourceArtifact(type = ResourceArtifactType.REPORT, code = "REP", formClass = ReportParams.class, requiresId = true),
                    @ResourceArtifact(type = ResourceArtifactType.REPORT, code = "REPJ", formClass = ReportParams.class),
                    @ResourceArtifact(type = ResourceArtifactType.REPORT, code = "ERR", formClass = ReportParams.class),
                    @ResourceArtifact(type = ResourceArtifactType.FILTER, code = "FIL", formClass = FilterParams.class)
            }
    )
    public static class ReadonlyResource extends BaseResource<Long> {
        private String name;
        private String view;
        private String plain;
        private FileReference attachment;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getView() { return view; }
        public void setView(String view) { this.view = view; }
        public String getPlain() { return plain; }
        public void setPlain(String plain) { this.plain = plain; }
        public FileReference getAttachment() { return attachment; }
        public void setAttachment(FileReference attachment) { this.attachment = attachment; }
    }

    @ResourceConfig(
            name = "mutableResource",
            orderField = "sequence",
            quickFilterFields = {"name"},
            artifacts = {
                    @ResourceArtifact(type = ResourceArtifactType.ACTION, code = "ACT", formClass = ActionParams.class, requiresId = true)
            }
    )
    public static class MutableResource extends BaseResource<Long> {
        private String name;
        private Long sequence;
        private FileReference attachment;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getSequence() { return sequence; }
        public void setSequence(Long sequence) { this.sequence = sequence; }
        public FileReference getAttachment() { return attachment; }
        public void setAttachment(FileReference attachment) { this.attachment = attachment; }
    }

    public static class ReportParams implements Serializable {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class ActionParams implements Serializable {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class FilterParams implements Serializable {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class ReadonlyEntity extends BaseEntity<ReadonlyResource> {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class MutableEntity extends BaseEntity<MutableResource> implements ReorderableEntity<Long> {
        private String name;
        private Long order;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        @Override public Long getOrder() { return order; }
        @Override public void setOrder(Long order) { this.order = order; }
    }

    static class TestReadonlyService extends BaseReadonlyResourceService<ReadonlyResource, Long, ReadonlyEntity> {
        TestReadonlyService() {
            ObjectMappingHelper objectMappingHelper = new ObjectMappingHelper();
            ReflectionTestUtils.setField(this, "objectMappingHelper", objectMappingHelper);
            ReflectionTestUtils.setField(this, "resourceEntityMappingHelper", new ResourceEntityMappingHelper(objectMappingHelper));
            ReflectionTestUtils.setField(this, "jasperReportsHelper", mock(JasperReportsHelper.class));
            ReflectionTestUtils.setField(this, "permissionHelper", allowAllPermissions());

            register("DETAIL", new PerspectiveApplicator<>() {
                @Override
                public void applySingle(String code, ReadonlyEntity entity, ReadonlyResource resource) {
                    resource.setView(code + "-" + entity.getId());
                }
            });
            register("REP", new ReportGenerator<ReadonlyEntity, ReportParams, String>() {
                @Override
                public List<String> generateData(String code, ReadonlyEntity entity, ReportParams params) {
                    return List.of(code + "-" + params.getName() + "-" + entity.getId());
                }

                @Override
                public DownloadableFile generateFile(String code, List<?> data, ReportFileType fileType, OutputStream out) {
                    return new DownloadableFile("REP.csv", "text/csv", "rep".getBytes(StandardCharsets.UTF_8));
                }

                @Override
                public void onChange(Serializable id, ReportParams previous, String fieldName, Object fieldValue, Map<String, es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, ReportParams target) {
                    target.setName(String.valueOf(fieldValue));
                }
            });
            register("REPJ", new ReportGenerator<ReadonlyEntity, ReportParams, String>() {
                @Override
                public List<String> generateData(String code, ReadonlyEntity entity, ReportParams params) {
                    return List.of("jasper");
                }

                @Override
                public URL getJasperReportUrl(String code, ReportFileType fileType) {
                    try {
                        return new URL("file:/tmp/report.jasper");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public void onChange(Serializable id, ReportParams previous, String fieldName, Object fieldValue, Map<String, es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, ReportParams target) {
                    target.setName(String.valueOf(fieldValue));
                }
            });
            register("ERR", new ReportGenerator<ReadonlyEntity, ReportParams, String>() {
                @Override
                public List<String> generateData(String code, ReadonlyEntity entity, ReportParams params) {
                    throw new IllegalStateException("boom");
                }

                @Override
                public void onChange(Serializable id, ReportParams previous, String fieldName, Object fieldValue, Map<String, es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, ReportParams target) {
                    target.setName(String.valueOf(fieldValue));
                }
            });
            register("FIL", new FilterProcessor<FilterParams>() {
                @Override
                public void onChange(Serializable id, FilterParams previous, String fieldName, Object fieldValue, Map<String, es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, FilterParams target) {
                    target.setName(fieldValue + "-filtered");
                }

                @Override
                public List<FieldOption> getOptions(String fieldName, Map<String, String[]> requestParameterMap) {
                    return List.of(new FieldOption("OPT", "Option"));
                }
            });
            register("attachment", (FieldDownloader<ReadonlyEntity>) (entity, fieldName, out) ->
                    new DownloadableFile("attachment.txt", "text/plain", "file".getBytes(StandardCharsets.UTF_8)));
        }
    }

    static class TestMutableService extends BaseMutableResourceService<MutableResource, Long, MutableEntity> {
        private final RecordingFieldFileManager fileManager = new RecordingFieldFileManager();
        private List<MutableEntity> reorderableLines = new ArrayList<>();

        TestMutableService() {
            ObjectMappingHelper objectMappingHelper = new ObjectMappingHelper();
            ReflectionTestUtils.setField(this, "objectMappingHelper", objectMappingHelper);
            ReflectionTestUtils.setField(this, "resourceEntityMappingHelper", new ResourceEntityMappingHelper(objectMappingHelper));
            ReflectionTestUtils.setField(this, "resourceReferenceToEntityHelper", mock(ResourceReferenceToEntityHelper.class));
            ReflectionTestUtils.setField(this, "jasperReportsHelper", mock(JasperReportsHelper.class));
            ReflectionTestUtils.setField(this, "permissionHelper", allowAllPermissions());

            register("name", (OnChangeLogicProcessor<MutableResource>) (id, previous, fieldName, fieldValue, answers, previousFieldNames, target) ->
                    target.setName(fieldValue + "-processed"));
            register("status", (FieldOptionsProvider) (fieldName, requestParameterMap) ->
                    List.of(new FieldOption("READY", "Ready")));
            register("attachment", fileManager);
            register("ACT", new ActionExecutor<MutableEntity, ActionParams, String>() {
                @Override
                public String exec(String code, MutableEntity entity, ActionParams params) {
                    if ("boom".equals(params.getName())) {
                        throw new IllegalStateException("explode");
                    }
                    return code + "-" + params.getName() + "-" + entity.getId();
                }

                @Override
                public void onChange(Serializable id, ActionParams previous, String fieldName, Object fieldValue, Map<String, es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, ActionParams target) {
                    target.setName(String.valueOf(fieldValue));
                }
            });
        }

        boolean callReorder(MutableEntity entity, Long sequenceForEntity, Long previousParentId, boolean sameSequenceInsertBefore, boolean isDelete) {
            return reorderIfReorderable(entity, sequenceForEntity, previousParentId, sameSequenceInsertBefore, isDelete);
        }

        @Override
        protected List<MutableEntity> reorderFindLinesWithParent(Serializable parentId) {
            return reorderableLines;
        }

        @Override
        protected Integer reorderGetIncrement() {
            return 10;
        }
    }

    static class RecordingFieldFileManager implements BaseMutableResourceService.FieldFileManager<MutableEntity> {
        private final List<String> savedNames = new ArrayList<>();
        private final List<String> deletedNames = new ArrayList<>();

        @Override
        public FileReference read(MutableEntity entity, String fieldName) {
            return new FileReference("existing.txt", "z".getBytes(StandardCharsets.UTF_8), "text/plain", 1);
        }

        @Override
        public void save(MutableEntity entity, String fieldName, FileReference fileReference) {
            savedNames.add(fieldName);
        }

        @Override
        public void delete(MutableEntity entity, String fieldName) {
            deletedNames.add(fieldName);
        }
    }
}
