package es.caib.comanda.ms.back.config;

import com.turkraft.springfilter.shaded.org.antlr.v4.runtime.CommonToken;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceField;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.springfilter.ExpressionGenerator;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaseHalFormsAndExpressionGeneratorTest {

    @Test
    void baseHalFormsConfig_resolOpcionsEnumIPromptFields() throws Exception {
        // Comprova que la configuració HAL-FORMS resol opcions inline d'enums i el camp prompt correcte.
        installI18nUtil();
        TestHalFormsConfig config = new TestHalFormsConfig();

        Field statusField = HalResource.class.getDeclaredField("status");
        BaseHalFormsConfig.FieldOption[] options = ReflectionTestUtils.invokeMethod(config, "getInlineOptionsEnumConstants", statusField);
        assertThat(options).extracting(BaseHalFormsConfig.FieldOption::getId).containsExactly("OPEN", "CLOSED");
        assertThat(options).extracting(BaseHalFormsConfig.FieldOption::getDescription).contains("Desc-OPEN", "Desc-CLOSED");

        Field annotatedRef = HalResource.class.getDeclaredField("annotatedRef");
        String annotatedPrompt = ReflectionTestUtils.invokeMethod(config, "getRemoteOptionsPromptField", annotatedRef);
        assertThat(annotatedPrompt).isEqualTo("customLabel");

        Field plainRef = HalResource.class.getDeclaredField("plainRef");
        String defaultPrompt = ReflectionTestUtils.invokeMethod(config, "getRemoteOptionsPromptField", plainRef);
        assertThat(defaultPrompt).isEqualTo("label");
    }

    @Test
    void baseHalFormsConfig_generaLinksPerOpcionsICampsEnum() throws Exception {
        // Verifica que la configuració genera els links remots correctes per opcions de referència i enums.
        TestHalFormsConfig config = new TestHalFormsConfig();
        ResourceArtifact[] artifacts = HalResource.class.getAnnotation(ResourceConfig.class).artifacts();

        Link mutableFind = ReflectionTestUtils.invokeMethod(config, "getFindLinkWithSelfRel", HalMutableController.class, null, "plainRef");
        assertThat(mutableFind.getHref()).contains("/fields/plainRef/options");

        Link actionFind = ReflectionTestUtils.invokeMethod(config, "getFindLinkWithSelfRel", HalMutableController.class, artifacts[0], "plainRef");
        assertThat(actionFind.getHref()).contains("/artifacts/action/ACT/fields/plainRef/options");

        Link reportFind = ReflectionTestUtils.invokeMethod(config, "getFindLinkWithSelfRel", HalReadonlyController.class, artifacts[1], "plainRef");
        assertThat(reportFind.getHref()).contains("/artifacts/report/REP/fields/plainRef/options");

    }

    @Test
    void baseHalFormsConfig_cobreixCaminsNullINoEnum() throws Exception {
        // Exercita els ramals on el camp no és enum o no es pot construir un link remot vàlid.
        installI18nUtil();
        TestHalFormsConfig config = new TestHalFormsConfig();

        Field plainField = NonEnumHolder.class.getDeclaredField("plainText");
        BaseHalFormsConfig.FieldOption[] options = ReflectionTestUtils.invokeMethod(config, "getInlineOptionsEnumConstants", plainField);
        assertThat(options).isEmpty();

        Field plainRef = HalResource.class.getDeclaredField("plainRef");
        Link missingRemoteOptions = ReflectionTestUtils.invokeMethod(
                config,
                "getRemoteOptionsLink",
                HalResource.class,
                null,
                plainRef,
                Set.of((Class<es.caib.comanda.ms.back.controller.ReadonlyResourceController>) (Class<?>) HalReadonlyController.class));
        assertThat(missingRemoteOptions).isNull();

        Link missingFindLink = ReflectionTestUtils.invokeMethod(config, "getFindLinkWithSelfRel", HalReadonlyController.class, null, "plainRef");
        assertThat(missingFindLink).isNull();
    }

    @Test
    void expressionGenerator_helpers_autoritzenIdsICamins() throws Exception {
        // Exercita els helpers privats que detecten ids, autoritzen camins i obtenen subpaths.
        com.turkraft.springfilter.shaded.org.antlr.v4.runtime.ParserRuleContext parent = mock(com.turkraft.springfilter.shaded.org.antlr.v4.runtime.ParserRuleContext.class);
        com.turkraft.springfilter.shaded.org.antlr.v4.runtime.tree.ParseTree first = mock(com.turkraft.springfilter.shaded.org.antlr.v4.runtime.tree.ParseTree.class);
        com.turkraft.springfilter.shaded.org.antlr.v4.runtime.tree.ParseTree last = mock(com.turkraft.springfilter.shaded.org.antlr.v4.runtime.tree.ParseTree.class);
        com.turkraft.springfilter.parser.FilterParser.InputContext input = mock(com.turkraft.springfilter.parser.FilterParser.InputContext.class);
        when(input.getParent()).thenReturn(parent);
        when(parent.getChildCount()).thenReturn(2);
        when(parent.getChild(0)).thenReturn(first);
        when(parent.getChild(1)).thenReturn(last);
        when(first.getText()).thenReturn("entity.id");
        when(last.getText()).thenReturn("'1'");

        Boolean endsWithId = ReflectionTestUtils.invokeMethod(ExpressionGenerator.class, "inputContextIsOrEndsWithId", input);
        assertThat(endsWithId).isTrue();

        @SuppressWarnings("unchecked")
        Path<Object> path = mock(Path.class);
        when(path.get("child")).thenReturn(path);
        Path<?> samePath = ReflectionTestUtils.invokeMethod(ExpressionGenerator.class, "authorize", (java.util.function.BiFunction<Path<?>, Object, Boolean>) (p, payload) -> true, path, null, "field");
        assertThat(samePath).isSameAs(path);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(ExpressionGenerator.class, "authorize", (java.util.function.BiFunction<Path<?>, Object, Boolean>) (p, payload) -> false, path, null, "field"))
                .isInstanceOf(com.turkraft.springfilter.exception.UnauthorizedFilterPathException.class);

        Path<?> childPath = ReflectionTestUtils.invokeMethod(ExpressionGenerator.class, "getPathFromEntityOrEmbeddedResource", path, "child");
        assertThat(childPath).isSameAs(path);
    }

    @Test
    void expressionGenerator_helpers_detectenJoinColumnsIValidenArguments() throws Exception {
        // Comprova la detecció de JoinColumnsOrFormulas i les precondicions del mètode run.
        @SuppressWarnings("unchecked")
        javax.persistence.criteria.From<JoinedEntity, JoinedEntity> from = mock(javax.persistence.criteria.From.class);
        when(from.getJavaType()).thenReturn((Class) JoinedEntity.class);

        Boolean joined = ReflectionTestUtils.invokeMethod(ExpressionGenerator.class, "hasJoinColumnsOrFormulasAnnotation", from, "joined");
        Boolean plain = ReflectionTestUtils.invokeMethod(ExpressionGenerator.class, "hasJoinColumnsOrFormulasAnnotation", from, "plain");
        assertThat(joined).isTrue();
        assertThat(plain).isFalse();

        com.turkraft.springfilter.parser.Filter filter = mock(com.turkraft.springfilter.parser.Filter.class);
        Root<?> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);

        assertThatThrownBy(() -> ExpressionGenerator.run((com.turkraft.springfilter.parser.Filter) null, root, query, builder, Map.of(), null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExpressionGenerator.run(filter, null, query, builder, Map.of(), null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExpressionGenerator.run(filter, root, null, builder, Map.of(), null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExpressionGenerator.run(filter, root, query, null, Map.of(), null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExpressionGenerator.run(filter, root, query, builder, null, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void expressionGenerator_helpers_processenDatesIClausPrimaries() {
        // Comprova l'ajust de dates segons l'operador i la resolució del tipus de PK quan el camí és "id".
        Date original = new Date(1_710_000_000_000L);

        Date greaterThanDate = ReflectionTestUtils.invokeMethod(ExpressionGenerator.class, "processInputValue", inputContextWithOperator(com.turkraft.springfilter.parser.FilterParser.GREATER_THAN), original);
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(greaterThanDate);
        assertThat(startOfDay.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(startOfDay.get(Calendar.MINUTE)).isEqualTo(0);

        Date lowerThanDate = ReflectionTestUtils.invokeMethod(ExpressionGenerator.class, "processInputValue", inputContextWithOperator(com.turkraft.springfilter.parser.FilterParser.LESS_THAN), original);
        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTime(lowerThanDate);
        assertThat(endOfDay.get(Calendar.HOUR_OF_DAY)).isEqualTo(23);
        assertThat(endOfDay.get(Calendar.MINUTE)).isEqualTo(59);

        Date equalDate = ReflectionTestUtils.invokeMethod(ExpressionGenerator.class, "processInputValue", inputContextWithOperator(com.turkraft.springfilter.parser.FilterParser.EQUAL), original);
        assertThat(equalDate).isEqualTo(original);

        com.turkraft.springfilter.parser.FilterParser.InputContext input = mock(com.turkraft.springfilter.parser.FilterParser.InputContext.class);
        com.turkraft.springfilter.shaded.org.antlr.v4.runtime.ParserRuleContext parent = mock(com.turkraft.springfilter.shaded.org.antlr.v4.runtime.ParserRuleContext.class);
        com.turkraft.springfilter.shaded.org.antlr.v4.runtime.tree.ParseTree first = mock(com.turkraft.springfilter.shaded.org.antlr.v4.runtime.tree.ParseTree.class);
        Root<PersistableEntity> root = mock(Root.class);
        when(input.getParent()).thenReturn(parent);
        when(parent.getChild(0)).thenReturn(first);
        when(first.getText()).thenReturn("id");
        when(root.getJavaType()).thenReturn((Class) PersistableEntity.class);

        Class<?> pkType = ReflectionTestUtils.invokeMethod(ExpressionGenerator.class, "getPkPathType", input, root, Map.of(), null);
        assertThat(pkType).isEqualTo(Long.class);
    }

    private static void installI18nUtil() {
        I18nUtil i18nUtil = new I18nUtil();
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(any(String.class), eq(null), any(java.util.Locale.class)))
                .thenAnswer(inv -> "Desc-" + ((String) inv.getArgument(0)).replaceAll("^.*\\.", ""));
        ReflectionTestUtils.setField(i18nUtil, "messageSource", messageSource);
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        when(applicationContext.getBean(I18nUtil.class)).thenReturn(i18nUtil);
        i18nUtil.setApplicationContext(applicationContext);
    }

    private static com.turkraft.springfilter.parser.FilterParser.InputContext inputContextWithOperator(int tokenType) {
        com.turkraft.springfilter.parser.FilterParser.InputContext input = mock(com.turkraft.springfilter.parser.FilterParser.InputContext.class);
        com.turkraft.springfilter.parser.FilterParser.InfixContext parent = mock(com.turkraft.springfilter.parser.FilterParser.InfixContext.class);
        ReflectionTestUtils.setField(parent, "operator", new CommonToken(tokenType));
        when(input.getParent()).thenReturn(parent);
        return input;
    }

    enum Status {
        OPEN, CLOSED
    }

    @ResourceConfig(descriptionField = "label")
    public static class ReferencedResource extends BaseResource<Long> {
        private String label;
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    @ResourceConfig(
            artifacts = {
                    @ResourceArtifact(type = ResourceArtifactType.ACTION, code = "ACT", formClass = Serializable.class),
                    @ResourceArtifact(type = ResourceArtifactType.REPORT, code = "REP", formClass = Serializable.class)
            }
    )
    public static class HalResource extends BaseResource<Long> {
        private Status status;
        @ResourceField(enumType = true)
        private Status typedEnum;
        @ResourceField(descriptionField = "customLabel")
        private ResourceReference<ReferencedResource, Long> annotatedRef;
        private ResourceReference<ReferencedResource, Long> plainRef;

        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
        public Status getTypedEnum() { return typedEnum; }
        public void setTypedEnum(Status typedEnum) { this.typedEnum = typedEnum; }
        public ResourceReference<ReferencedResource, Long> getAnnotatedRef() { return annotatedRef; }
        public void setAnnotatedRef(ResourceReference<ReferencedResource, Long> annotatedRef) { this.annotatedRef = annotatedRef; }
        public ResourceReference<ReferencedResource, Long> getPlainRef() { return plainRef; }
        public void setPlainRef(ResourceReference<ReferencedResource, Long> plainRef) { this.plainRef = plainRef; }
    }

    @Relation(collectionRelation = "halResources")
    static class HalReadonlyController extends BaseReadonlyResourceController<HalResource, Long> {
        @Override protected Link getIndexLink() { return Link.of("/hal"); }
        @Override public Class<HalResource> getResourceClass() { return HalResource.class; }
    }

    @Relation(collectionRelation = "halResources")
    static class HalMutableController extends BaseMutableResourceController<HalResource, Long> {
        @Override protected Link getIndexLink() { return Link.of("/hal"); }
        @Override public Class<HalResource> getResourceClass() { return HalResource.class; }
    }

    static class TestHalFormsConfig extends BaseHalFormsConfig {
        @Override
        protected String[] getControllerPackages() {
            return new String[0];
        }
    }

    static class JoinedEntity {
        @JoinColumnsOrFormulas({
                @JoinColumnOrFormula(formula = @JoinFormula(value = "1", referencedColumnName = "id"))
        })
        private String joined;
        private String plain;
    }

    static class NonEnumHolder {
        private String plainText;
    }

    static class PersistableEntity implements Persistable<Long> {
        @Override
        public Long getId() {
            return 1L;
        }

        @Override
        public boolean isNew() {
            return false;
        }
    }
}
