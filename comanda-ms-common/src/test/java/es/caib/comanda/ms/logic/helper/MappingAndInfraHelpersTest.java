package es.caib.comanda.ms.logic.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.map.IMap;
import com.hazelcast.spring.cache.HazelcastCache;
import es.caib.comanda.client.ParametreServiceClient;
import es.caib.comanda.client.model.ParamTipus;
import es.caib.comanda.client.model.Parametre;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.exception.ParametreTipusException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.ExportField;
import es.caib.comanda.ms.logic.intf.model.FileReference;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.persist.entity.ResourceEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Persistable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MappingAndInfraHelpersTest {

    @Test
    void objectMappingHelper_newInstanceMap_iClone_funcionen() {
        // Verifica el mapeig a nova instància i la clonació superficial d'objectes simples.
        ObjectMappingHelper helper = new ObjectMappingHelper();
        SourceSimple source = new SourceSimple();
        source.setId(7L);
        source.setName("abc");

        TargetSimple mapped = helper.newInstanceMap(source, TargetSimple.class);
        SourceSimple cloned = helper.clone(source);

        assertThat(mapped.getId()).isEqualTo(7L);
        assertThat(mapped.getName()).isEqualTo("abc");
        assertThat(cloned.getId()).isEqualTo(7L);
        assertThat(cloned.getName()).isEqualTo("abc");
    }

    @Test
    void objectMappingHelper_map_entitatAReferencia() {
        // Comprova la conversió d'una entitat persistible a una ResourceReference durant el mapatge.
        ObjectMappingHelper helper = new ObjectMappingHelper();
        SourceWithEntity source = new SourceWithEntity();
        source.setRel(new TestEntity(5L, "desc"));
        TargetWithReference target = new TargetWithReference();

        helper.map(source, target);

        assertThat(target.getRel()).isNotNull();
        assertThat(target.getRel().getId()).isEqualTo(5L);
    }

    @Test
    void resourceEntityMappingHelper_resourceEntityUpdateIEntityToResource() {
        // Exercita el pas de resource a entity, l'actualització i el mapatge invers cap al resource.
        ObjectMappingHelper objectMappingHelper = new ObjectMappingHelper();
        ResourceEntityMappingHelper helper = new ResourceEntityMappingHelper(objectMappingHelper);

        TestResource resource = new TestResource();
        resource.setId(99L);
        resource.setName("n1");
        TestEntity ref = new TestEntity(11L, "r1");
        TestEntity built = helper.resourceToEntity(resource, 99L, TestEntity.class, Map.of("related", ref));
        assertThat(built.getId()).isEqualTo(99L);

        TestEntity target = new TestEntity();
        target.setAttachment(new byte[]{1});
        target.setRelated(ref);
        helper.updateEntityWithResource(target, resource, Map.of("related", ref));
        assertThat(target.getRelated()).isEqualTo(ref);

        TestResource back = helper.entityToResource(target, TestResource.class);
        assertThat(back.getName()).isEqualTo(target.getName());
    }

    @Test
    void cacheHelper_operacionsFuncionalitat() {
        // Valida les operacions principals de buidat i consulta de caches Hazelcast.
        CacheManager cacheManager = mock(CacheManager.class);
        Config config = new Config();
        config.getMapConfigs().put("cacheA", new com.hazelcast.config.MapConfig("cacheA"));
        CacheHelper helper = new CacheHelper(cacheManager, config);

        HazelcastCache cache = mock(HazelcastCache.class);
        @SuppressWarnings("unchecked")
        IMap<Object, Object> nativeMap = mock(IMap.class);
        when(nativeMap.keySet()).thenReturn(Set.of("p1-1", "p2-2"));
        when(cache.getNativeCache()).thenReturn(nativeMap);
        when(cacheManager.getCache("cacheA")).thenReturn(cache);
        when(cacheManager.getCacheNames()).thenReturn(Set.of("cacheA", "cacheB"));

        helper.evictCache("cacheA");
        helper.evictAllCaches();
        helper.evictCacheByPrefix("cache");
        helper.evictCacheItem("cacheA", "k1");
        helper.evictCacheItemByPrefix("cacheA", "p1");

        assertThat(helper.getCache("cacheA")).isSameAs(cache);
        assertThat(helper.getCacheNames()).contains("cacheA");
        verify(nativeMap).remove("k1");
        verify(nativeMap).remove("p1-1");
    }

    @Test
    void httpAuthorizationHeaderHelper_basicIBearer() {
        // Comprova la generació de capçaleres Authorization en mode Basic i Bearer.
        RestTemplate restTemplate = mock(RestTemplate.class);
        HttpAuthorizationHeaderHelper helper = new HttpAuthorizationHeaderHelper(restTemplate);

        ReflectionTestUtils.setField(helper, "authUsername", "user");
        ReflectionTestUtils.setField(helper, "authPassword", "pwd");
        assertThat(helper.getAuthorizationHeader()).startsWith("Basic ");

        ReflectionTestUtils.setField(helper, "providerBaseUrl", "http://kc");
        ReflectionTestUtils.setField(helper, "providerRealm", "realm");
        ReflectionTestUtils.setField(helper, "providerClientId", "client");
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("access_token", "tkn")));
        assertThat(helper.getAuthorizationHeader()).isEqualTo("Bearer tkn");
    }

    @Test
    void jasperReportsHelper_exportIGenerate_errorPaths() throws Exception {
        // Cobreix camins d'error de JasperReportsHelper quan falten recursos o plantilles vàlides.
        JasperReportsHelper helper = new JasperReportsHelper();
        ExportField[] fields = {new ExportField("unknown", "Unknown")};

        assertThatThrownBy(() -> helper.export(TestResource.class, List.of(), fields, ReportFileType.PDF, new ByteArrayOutputStream()))
                .isInstanceOf(NullPointerException.class);

        URL missing = new URL("file:/definitely-missing-report.jrxml");
        assertThatThrownBy(() -> helper.generate(TestResource.class, "CODE", missing, List.of(), null, null, ReportFileType.PDF, new ByteArrayOutputStream()))
                .isInstanceOf(ReportGenerationException.class);
    }

    @Test
    void jasperReportsHelper_generateDownloadableFile_quanTipusNoSuportat_falla() {
        // Comprova el ramal d'error quan es demana un tipus d'exportació no suportat.
        JasperReportsHelper helper = new JasperReportsHelper();
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(
                helper,
                "generateDownloadableFile",
                TestResource.class,
                null,
                ReportFileType.JSON,
                new ByteArrayOutputStream()))
                .isInstanceOf(ReportGenerationException.class);
    }

    @Test
    void jasperReportsHelper_reportFieldsIColumns_resolenFieldsIGetters() {
        // Comprova que els helpers interns del report resolen tant camps directes com getters derivats.
        JasperReportsHelper helper = new JasperReportsHelper();
        ExportField[] fields = {
                new ExportField("name", "Nom"),
                new ExportField("code", "Codi")
        };

        Object[] reportFields = ReflectionTestUtils.invokeMethod(helper, "getReportFields", GetterResource.class, fields);
        Object[] reportColumns = ReflectionTestUtils.invokeMethod(helper, "getReportColumns", GetterResource.class, fields);

        assertThat(reportFields).hasSize(2);
        assertThat(reportColumns).hasSize(2);
    }

    @Test
    void jasperReportsHelper_toColumn_cobreixTipusEspecials() {
        // Exercita els ramals de conversió de columnes per dates, enums, referències i recursos.
        JasperReportsHelper helper = new JasperReportsHelper();

        Object localDateColumn = ReflectionTestUtils.invokeMethod(helper, "toColumn", SpecialTypesResource.class, new ExportField("createdDate", "Data"), LocalDate.class);
        Object localDateTimeColumn = ReflectionTestUtils.invokeMethod(helper, "toColumn", SpecialTypesResource.class, new ExportField("createdDateTime", "DataHora"), LocalDateTime.class);
        Object enumColumn = ReflectionTestUtils.invokeMethod(helper, "toColumn", SpecialTypesResource.class, new ExportField("status", "Estat"), Status.class);
        Object referenceColumn = ReflectionTestUtils.invokeMethod(helper, "toColumn", SpecialTypesResource.class, new ExportField("related", "Relacionat"), ResourceReference.class);
        Object serializableColumn = ReflectionTestUtils.invokeMethod(helper, "toColumn", SpecialTypesResource.class, new ExportField("payload", "Payload"), Serializable.class);
        Object resourceColumn = ReflectionTestUtils.invokeMethod(helper, "toColumn", SpecialTypesResource.class, new ExportField("nested", "Recurs"), NestedResource.class);

        assertThat(localDateColumn).isNotNull();
        assertThat(localDateTimeColumn).isNotNull();
        assertThat(enumColumn).isNotNull();
        assertThat(referenceColumn).isNotNull();
        assertThat(serializableColumn).isNotNull();
        assertThat(resourceColumn).isNotNull();
    }

    @Test
    void parametresHelper_conversions_iDefaults() {
        // Verifica conversions de paràmetres, valors per defecte i errors de tipus.
        ParametreServiceClient client = mock(ParametreServiceClient.class);
        HttpAuthorizationHeaderHelper auth = mock(HttpAuthorizationHeaderHelper.class);
        when(auth.getAuthorizationHeader()).thenReturn("Basic x");
        ParametresHelper helper = new ParametresHelper(client, auth);
        ParametresHelper spy = Mockito.spy(helper);
        ReflectionTestUtils.setField(spy, "self", spy);

        doReturn(Parametre.builder().codi("num").tipus(ParamTipus.NUMERIC).valor("12.5").build()).when(spy).perametreFindByCodi("num");
        doReturn(Parametre.builder().codi("int").tipus(ParamTipus.NUMERIC).valor("7").build()).when(spy).perametreFindByCodi("int");
        doReturn(Parametre.builder().codi("bool").tipus(ParamTipus.BOOLEAN).valor("true").build()).when(spy).perametreFindByCodi("bool");
        doReturn(Parametre.builder().codi("text").tipus(ParamTipus.TEXT).valor(" hello ").build()).when(spy).perametreFindByCodi("text");
        doReturn(Parametre.builder().codi("badBool").tipus(ParamTipus.BOOLEAN).valor("x").build()).when(spy).perametreFindByCodi("badBool");
        doReturn(null).when(spy).perametreFindByCodi("missing");

        assertThat(spy.getParametreNumeric("num")).isEqualTo(12.5d);
        assertThat(spy.getParametreEnter("int")).isEqualTo(7);
        assertThat(spy.getParametreBoolean("bool")).isTrue();
        assertThat(spy.getParametreText("text")).isEqualTo("hello");
        assertThat(spy.getParametreNumeric("missing", 3.0)).isEqualTo(3.0);
        assertThat(spy.getParametreText("missing", "d")).isEqualTo("d");
        assertThatThrownBy(() -> spy.getParametreBoolean("badBool")).isInstanceOf(ParametreTipusException.class);
    }

    @Test
    void parametresHelper_perametreFindByCodi_cercaClient() {
        // Comprova la cerca d'un paràmetre concret a través del client remot.
        ParametreServiceClient client = mock(ParametreServiceClient.class);
        HttpAuthorizationHeaderHelper auth = mock(HttpAuthorizationHeaderHelper.class);
        when(auth.getAuthorizationHeader()).thenReturn("Basic x");
        ParametresHelper helper = new ParametresHelper(client, auth);

        Parametre p = Parametre.builder().codi("c1").tipus(ParamTipus.TEXT).valor("v").build();
        PagedModel<EntityModel<Parametre>> paged = PagedModel.of(List.of(EntityModel.of(p)), new PagedModel.PageMetadata(1, 0, 1));
        when(client.find(any(), any(), any(), any(), any(), any(), any())).thenReturn(paged);

        Parametre found = helper.perametreFindByCodi("c1");
        assertThat(found.getCodi()).isEqualTo("c1");
    }

    public static class SourceSimple {
        private Long id;
        private String name;
        public SourceSimple() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class TargetSimple {
        private Long id;
        private String name;
        public TargetSimple() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class SourceWithEntity {
        private TestEntity rel;
        public SourceWithEntity() {}
        public TestEntity getRel() { return rel; }
        public void setRel(TestEntity rel) { this.rel = rel; }
    }

    public static class TargetWithReference {
        private ResourceReference<TestResource, Long> rel;
        public TargetWithReference() {}
        public ResourceReference<TestResource, Long> getRel() { return rel; }
        public void setRel(ResourceReference<TestResource, Long> rel) { this.rel = rel; }
    }

    @ResourceConfig(name = "test", descriptionField = "name")
    public static class TestResource extends BaseResource<Long> {
        private String name;
        public TestResource() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class TestEntity implements ResourceEntity<TestResource, Long>, Persistable<Long> {
        private Long id;
        private String name;
        private TestEntity related;
        private byte[] attachment;
        private FileReference attachmentRef;

        public TestEntity() {
        }

        public TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public boolean isNew() {
            return false;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TestEntity getRelated() {
            return related;
        }

        public void setRelated(TestEntity related) {
            this.related = related;
        }

        public byte[] getAttachment() {
            return attachment;
        }

        public void setAttachment(byte[] attachment) {
            this.attachment = attachment;
        }

        public FileReference getAttachmentRef() {
            return attachmentRef;
        }

        public void setAttachmentRef(FileReference attachmentRef) {
            this.attachmentRef = attachmentRef;
        }

        public static class Builder {
            private final TestEntity entity = new TestEntity();
            public Builder testResource(TestResource r) { entity.setId(r.getId()); entity.setName(r.getName()); return this; }
            public Builder related(Persistable<?> p) { entity.setRelated((TestEntity) p); return this; }
            public TestEntity build() { return entity; }
        }
    }

    public static class GetterResource extends BaseResource<Long> {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return "CODE"; }
    }

    enum Status {
        OPEN, CLOSED
    }

    public static class NestedResource extends BaseResource<Long> {
    }

    public static class SpecialTypesResource extends BaseResource<Long> {
        private LocalDate createdDate;
        private LocalDateTime createdDateTime;
        private Status status;
        private ResourceReference<TestResource, Long> related;
        private Serializable payload;
        private NestedResource nested;

        public LocalDate getCreatedDate() { return createdDate; }
        public LocalDateTime getCreatedDateTime() { return createdDateTime; }
        public Status getStatus() { return status; }
        public ResourceReference<TestResource, Long> getRelated() { return related; }
        public Serializable getPayload() { return payload; }
        public NestedResource getNested() { return nested; }
    }
}
