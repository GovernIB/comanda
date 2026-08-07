package es.caib.comanda.alarmes.logic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.caib.comanda.alarmes.logic.helper.UserInformationHelper;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigRegla;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaAmbit;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaComparador;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaMetrica;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaOperador;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaTipusNode;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventPublisher;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaConfigRepository;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.alarmes.persist.repository.AlarmaUsuariRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.Usuari;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.intf.util.ThreadLocalUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a AlarmaConfigServiceImpl")
class AlarmaConfigServiceImplTest {

    @Mock private AuthenticationHelper authenticationHelper;
    @Mock private AlarmaRepository alarmaRepository;
    @Mock private AlarmaConfigRepository alarmaConfigRepository;
    @Mock private AlarmaUsuariRepository alarmaUsuariRepository;
    @Mock private ComandaSseEventPublisher comandaSseEventPublisher;
    @Mock private ObjectMapper objectMapper;
    @Mock private UserInformationHelper userInformationHelper;
    @Mock private I18nUtil i18nUtil;
    @Mock private ApplicationContext applicationContext;

    @InjectMocks
    private AlarmaConfigServiceImpl alarmaConfigService;

    private static final String CURRENT_USER = "user1";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(I18nUtil.class, "applicationContext", applicationContext);
        lenient().when(applicationContext.getBean(I18nUtil.class)).thenReturn(i18nUtil);
        lenient().when(i18nUtil.getI18nMessage(anyString())).thenReturn("Missatge d'error");
    }

    // ========================================================================
    // 1. TESTOS PARAMETritzats EXISTENTS (Matrius de Permisos)
    // ========================================================================

    @Test
    @DisplayName("additionalSpringFilter retorna filtre base per a administrador")
    void additionalSpringFilter_quanAdmin_retornaFiltreBase() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);
        assertThat(alarmaConfigService.additionalSpringFilter("", new String[0])).isEqualTo("esborrat:false");
    }

    @Test
    @DisplayName("additionalSpringFilter retorna filtre base i creatPer per a usuari normal")
    void additionalSpringFilter_quanUsuari_retornaFiltreRestringit() {
        when(authenticationHelper.getCurrentUserName()).thenReturn(CURRENT_USER);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        assertThat(alarmaConfigService.additionalSpringFilter("", new String[0]))
            .isEqualTo("esborrat:false and createdBy:'" + CURRENT_USER + "'");
    }

    @Test
    @DisplayName("DeleteAlarmaConfigAction marca com esborrat i finalitza alarmes")
    void deleteAlarmaConfigAction_exec_marcaEsborratIFinalitza() throws Exception {
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setEsborrat(false);
        alarmaConfigService.init();

        BaseMutableResourceService.ActionExecutor actionExecutor = (BaseMutableResourceService.ActionExecutor)
            ((java.util.Map) ReflectionTestUtils.getField(alarmaConfigService, "actionExecutorMap")).get(AlarmaConfig.ALARMA_CONFIG_DELETE_ACTION);

        actionExecutor.exec(AlarmaConfig.ALARMA_CONFIG_DELETE_ACTION, entity, null);

        assertThat(entity.isEsborrat()).isTrue();
        verify(alarmaRepository).deleteByAlarmaConfigAndEstat(entity, AlarmaEstat.ESBORRANY);
        verify(alarmaRepository).finalizeByAlarmaConfig(eq(entity), any(LocalDateTime.class));
    }

    @ParameterizedTest
    @MethodSource("proporcionarCasosPermisosCreate")
    @DisplayName("beforeCreateEntity: matriu de permisos")
    void beforeCreateEntity_matriuPermisos(boolean resourceIsAdmin, boolean resourceIsCorreuGeneric, boolean userIsAdmin, boolean shouldThrow, String descripcion) {
        AlarmaConfig resource = new AlarmaConfig();
        resource.setAdmin(resourceIsAdmin);
        resource.setCorreuGeneric(resourceIsCorreuGeneric);
        lenient().when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(userIsAdmin);

        if (shouldThrow) {
            assertThatThrownBy(() -> alarmaConfigService.beforeCreateEntity(new AlarmaConfigEntity(), resource, null))
                .as(descripcion).isInstanceOf(ResourceNotCreatedException.class);
        } else {
            assertThatCode(() -> alarmaConfigService.beforeCreateEntity(new AlarmaConfigEntity(), resource, null))
                .as(descripcion).doesNotThrowAnyException();
        }
    }

    private static Stream<Arguments> proporcionarCasosPermisosCreate() {
        return Stream.of(
            arguments(true,  false, true,  false, "Admin crea alarma admin → OK"),
            arguments(true,  false, false, true,  "No-admin crea alarma admin → EXCEPCIÓ"),
            arguments(false, true,  true,  false, "Admin crea alarma genèrica → OK"),
            arguments(false, true,  false, true,  "No-admin crea alarma genèrica → EXCEPCIÓ"),
            arguments(false, false, true,  false, "Admin crea alarma normal → OK"),
            arguments(false, false, false, false, "No-admin crea alarma normal → OK")
        );
    }

    @ParameterizedTest
    @MethodSource("proporcionarCasosPermisosUpdate")
    @DisplayName("beforeUpdateEntity: matriu de permisos")
    void beforeUpdateEntity_matriuPermisos(boolean entityIsAdmin, boolean entityIsCorreuGeneric, boolean userIsAdmin, boolean shouldThrow, String descripcion) {
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setAdmin(entityIsAdmin);
        entity.setCorreuGeneric(entityIsCorreuGeneric);
        lenient().when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(userIsAdmin);

        if (shouldThrow) {
            assertThatThrownBy(() -> alarmaConfigService.beforeUpdateEntity(entity, new AlarmaConfig(), null))
                .as(descripcion).isInstanceOf(ResourceNotUpdatedException.class);
        } else {
            assertThatCode(() -> alarmaConfigService.beforeUpdateEntity(entity, new AlarmaConfig(), null))
                .as(descripcion).doesNotThrowAnyException();
        }
    }

    private static Stream<Arguments> proporcionarCasosPermisosUpdate() {
        return Stream.of(
            arguments(true,  false, true,  false, "Admin actualitza alarma admin → OK"),
            arguments(true,  false, false, true,  "No-admin actualitza alarma admin → EXCEPCIÓ"),
            arguments(false, true,  true,  false, "Admin actualitza alarma genèrica → OK"),
            arguments(false, true,  false, true,  "No-admin actualitza alarma genèrica → EXCEPCIÓ"),
            arguments(false, false, true,  false, "Admin actualitza alarma normal → OK"),
            arguments(false, false, false, false, "No-admin actualitza alarma normal → OK")
        );
    }

    // ========================================================================
    // 2. NOUS TESTOS PARAMETritzats (Simplificació de lògica complexa)
    // ========================================================================

    @ParameterizedTest
    @MethodSource("provideTextRuleSummaryCases")
    @DisplayName("buildRuleSummary: construeix resum correctament per a mètriques de TEXT (ESTAT)")
    void buildRuleSummary_quanMetricaEstat_llavorsConstrueixResumText(
        AlarmaConfigReglaAmbit ambit, String codiObjecte, AlarmaConfigReglaMetrica metrica,
        AlarmaConfigReglaComparador comparador, List<String> valorsText, String expectedSummary) {

        AlarmaConfigRegla regla = new AlarmaConfigRegla();
        regla.setTipusNode(AlarmaConfigReglaTipusNode.CONDICIO);
        regla.setAmbit(ambit);
        regla.setCodiObjecte(codiObjecte);
        regla.setMetrica(metrica);
        regla.setComparador(comparador);
        regla.setValorsText(valorsText);

        String result = (String) ReflectionTestUtils.invokeMethod(alarmaConfigService, "buildRuleSummary", regla);
        assertThat(result).isEqualTo(expectedSummary);
    }

    private static Stream<Arguments> provideTextRuleSummaryCases() {
        return Stream.of(
            arguments(AlarmaConfigReglaAmbit.APLICACIO, null, AlarmaConfigReglaMetrica.ESTAT, AlarmaConfigReglaComparador.IGUAL, Arrays.asList("OK", "WARN"), "Aplicacio estat IGUAL OK, WARN"),
            arguments(AlarmaConfigReglaAmbit.SUBSISTEMA, "SUB1", AlarmaConfigReglaMetrica.ESTAT, AlarmaConfigReglaComparador.EN, Collections.singletonList("ERROR"), "Subsistema SUB1 estat EN ERROR"),
            arguments(AlarmaConfigReglaAmbit.INTEGRACIO, "INT1", AlarmaConfigReglaMetrica.ESTAT, AlarmaConfigReglaComparador.IGUAL, Collections.singletonList("OK"), "Integracio INT1 estat IGUAL OK"),
            arguments(AlarmaConfigReglaAmbit.SISTEMA, null, AlarmaConfigReglaMetrica.ESTAT, AlarmaConfigReglaComparador.IGUAL, Collections.singletonList("OK"), "Sistema estat IGUAL OK")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNumericRuleSummaryCases")
    @DisplayName("buildRuleSummary: construeix resum correctament per a mètriques NUMÈRIQUES")
    void buildRuleSummary_quanMetricaNumerica_llavorsConstrueixResumNumeric(
        AlarmaConfigReglaAmbit ambit, String codiObjecte, AlarmaConfigReglaMetrica metrica,
        AlarmaConfigReglaComparador comparador, BigDecimal valorNumeric, String expectedSummary) {

        AlarmaConfigRegla regla = new AlarmaConfigRegla();
        regla.setTipusNode(AlarmaConfigReglaTipusNode.CONDICIO);
        regla.setAmbit(ambit);
        regla.setCodiObjecte(codiObjecte);
        regla.setMetrica(metrica);
        regla.setComparador(comparador);
        regla.setValorNumeric(valorNumeric);

        String result = (String) ReflectionTestUtils.invokeMethod(alarmaConfigService, "buildRuleSummary", regla);
        assertThat(result).isEqualTo(expectedSummary);
    }

    private static Stream<Arguments> provideNumericRuleSummaryCases() {
        return Stream.of(
            arguments(AlarmaConfigReglaAmbit.SISTEMA, null, AlarmaConfigReglaMetrica.LATENCIA, AlarmaConfigReglaComparador.MAJOR, new BigDecimal("10.5"), "Sistema latència MAJOR 10.5"),
            arguments(AlarmaConfigReglaAmbit.APLICACIO, null, AlarmaConfigReglaMetrica.CARREGA_MITJANA_SISTEMA, AlarmaConfigReglaComparador.MENOR, new BigDecimal("5.0"), "Aplicacio càrrega mitjana MENOR 5.0"),
            arguments(AlarmaConfigReglaAmbit.SUBSISTEMA, "SUB2", AlarmaConfigReglaMetrica.MEMORIA_DISPONIBLE, AlarmaConfigReglaComparador.IGUAL, new BigDecimal("1024"), "Subsistema SUB2 memòria lliure IGUAL 1024"),
            arguments(AlarmaConfigReglaAmbit.INTEGRACIO, "INT2", AlarmaConfigReglaMetrica.ESPAI_DISC_LLIURE, AlarmaConfigReglaComparador.MAJOR_IGUAL, new BigDecimal("50"), "Integracio INT2 disc lliure MAJOR_IGUAL 50")
        );
    }

    @ParameterizedTest
    @MethodSource("provideNormalizeRuleCases")
    @DisplayName("normalizeRule: filtra correctament els valors nuls a les llistes")
    void normalizeRule_quanNodeNoGrup_llavorsFiltraValorsNuls(List<String> inputValors, List<String> expectedValors) {
        AlarmaConfigRegla regla = new AlarmaConfigRegla();
        // Utilitzem CONDICIO (o el nom que tingui l'enum per a node fulla) per evitar la lògica de GRUP
        regla.setTipusNode(AlarmaConfigReglaTipusNode.CONDICIO);
        regla.setValorsText(inputValors);

        AlarmaConfig resource = new AlarmaConfig();
        resource.setRegla(regla);

        AlarmaConfigRegla result = (AlarmaConfigRegla) ReflectionTestUtils.invokeMethod(alarmaConfigService, "normalizeRule", resource);
        assertThat(result.getValorsText()).isEqualTo(expectedValors);
    }

    private static Stream<Arguments> provideNormalizeRuleCases() {
        return Stream.of(
            arguments(Arrays.asList("valor1", null, "valor2"), Arrays.asList("valor1", "valor2")),
            arguments(Arrays.asList(null, null), Collections.emptyList()),
            arguments(Collections.singletonList("unic"), Collections.singletonList("unic")),
            arguments(null, Collections.emptyList())
        );
    }

    // ========================================================================
    // 3. TESTOS UNITARIS ESTÀNDARD (Casos únics o amb lògica complexa de setup)
    // ========================================================================

    @Test
    @DisplayName("netejaPerEntornApp: esborra les dades relacionades amb l'entornApp")
    void netejaPerEntornApp_quanEsCrida_llavorsEsborraDades() {
        Long entornAppId = 10L;
        alarmaConfigService.netejaPerEntornApp(entornAppId);
        verify(alarmaUsuariRepository).deleteByAlarmaEntornAppId(entornAppId);
        verify(alarmaRepository).deleteByEntornAppId(entornAppId);
        verify(alarmaConfigRepository).deleteByEntornAppId(entornAppId);
    }

    @Test
    @DisplayName("beforeCreateEntity: desactiva correuGeneric si no és admin")
    void beforeCreateEntity_quanNoEsAdminICorreuGeneric_llavorsDesactivaCorreuGeneric() {
        AlarmaConfig resource = new AlarmaConfig();
        resource.setAdmin(false);
        resource.setCorreuGeneric(true);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        alarmaConfigService.beforeCreateEntity(new AlarmaConfigEntity(), resource, null);
        assertThat(resource.isCorreuGeneric()).isFalse();
    }

    @Test
    @DisplayName("beforeUpdateEntity: llança excepció si no és admin i intenta activar correuGeneric")
    void beforeUpdateEntity_quanNoEsAdminIActivaCorreuGeneric_llancaExcepcio() {
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setAdmin(false);
        AlarmaConfig resource = new AlarmaConfig();
        resource.setCorreuGeneric(true);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        assertThatThrownBy(() -> alarmaConfigService.beforeUpdateEntity(entity, resource, null))
            .isInstanceOf(ResourceNotUpdatedException.class);
    }

    @Test
    @DisplayName("beforeUpdateEntity: reseteja ordre quan canvia el flag d'admin")
    void beforeUpdateEntity_quanCanviaFlagAdmin_llavorsResetejaOrdre() {
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setAdmin(false);
        AlarmaConfig resource = new AlarmaConfig();
        resource.setAdmin(true);
        resource.setOrdre(5L);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        alarmaConfigService.beforeUpdateEntity(entity, resource, null);
        assertThat(resource.getOrdre()).isNull();
    }

    @Test
    @DisplayName("readRule: retorna null si ruleJson és null o buit")
    void readRule_quanRuleJsonEsNullOBuit_llavorsRetornaNull() {
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setRuleJson(null);
        assertThat(alarmaConfigService.readRule(entity)).isNull();

        entity.setRuleJson("   ");
        assertThat(alarmaConfigService.readRule(entity)).isNull();
    }

    @Test
    @DisplayName("readRule: deserialitza correctament la regla")
    void readRule_quanRuleJsonValida_llavorsRetornaRegla() throws JsonProcessingException {
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setRuleJson("{\"tipusNode\":\"GRUP\"}");
        AlarmaConfigRegla expectedRegla = new AlarmaConfigRegla();
        when(objectMapper.readValue(anyString(), eq(AlarmaConfigRegla.class))).thenReturn(expectedRegla);

        assertThat(alarmaConfigService.readRule(entity)).isSameAs(expectedRegla);
    }

    @Test
    @DisplayName("readRule: retorna null i registra error si la deserialització falla")
    void readRule_quanDeserialitzacioFalla_llavorsRetornaNull() throws JsonProcessingException {
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setRuleJson("invalid json");
        when(objectMapper.readValue(anyString(), eq(AlarmaConfigRegla.class))).thenThrow(new JsonProcessingException("Error") {});

        assertThat(alarmaConfigService.readRule(entity)).isNull();
    }

    @Test
    @DisplayName("writeRule: retorna null si la regla és null")
    void writeRule_quanReglaEsNull_llavorsRetornaNull() {
        String result = (String) ReflectionTestUtils.invokeMethod(alarmaConfigService, "writeRule", (AlarmaConfigRegla) null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("writeRule: serialitza correctament la regla")
    void writeRule_quanReglaValida_llavorsRetornaJson() throws JsonProcessingException {
        AlarmaConfigRegla regla = new AlarmaConfigRegla();
        when(objectMapper.writeValueAsString(regla)).thenReturn("{\"tipusNode\":\"GRUP\"}");

        String result = (String) ReflectionTestUtils.invokeMethod(alarmaConfigService, "writeRule", regla);
        assertThat(result).isEqualTo("{\"tipusNode\":\"GRUP\"}");
    }

    @Test
    @DisplayName("writeRule: llança IllegalArgumentException si la serialització falla")
    void writeRule_quanSerialitzacioFalla_llancaExcepcio() throws JsonProcessingException {
        AlarmaConfigRegla regla = new AlarmaConfigRegla();
        when(objectMapper.writeValueAsString(regla)).thenThrow(new JsonProcessingException("Error") {});

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(alarmaConfigService, "writeRule", regla))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No s'ha pogut serialitzar la regla");
    }

    @Test
    @DisplayName("normalizeRule: retorna null si la regla és null")
    void normalizeRule_quanReglaEsNull_llavorsRetornaNull() {
        AlarmaConfig resource = new AlarmaConfig();
        resource.setRegla(null);
        AlarmaConfigRegla result = (AlarmaConfigRegla) ReflectionTestUtils.invokeMethod(alarmaConfigService, "normalizeRule", resource);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("normalizeRule: estableix valors per defecte per a node GRUP")
    void normalizeRule_quanNodeGrup_llavorsEstableixValorsPerDefecte() {
        AlarmaConfigRegla regla = new AlarmaConfigRegla();
        regla.setTipusNode(AlarmaConfigReglaTipusNode.GRUP);
        regla.setOperador(null);
        regla.setFills(null);
        AlarmaConfig resource = new AlarmaConfig();
        resource.setRegla(regla);

        AlarmaConfigRegla result = (AlarmaConfigRegla) ReflectionTestUtils.invokeMethod(alarmaConfigService, "normalizeRule", resource);

        assertThat(result.getOperador()).isEqualTo(AlarmaConfigReglaOperador.AND);
        assertThat(result.getFills()).isEmpty();
    }

    @Test
    @DisplayName("buildRuleSummary: retorna cadena buida si la regla és null o GRUP sense fills")
    void buildRuleSummary_quanReglaNullOGrupSenseFills_llavorsRetornaBuit() {
        assertThat((String) ReflectionTestUtils.invokeMethod(alarmaConfigService, "buildRuleSummary", (AlarmaConfigRegla) null)).isEmpty();

        AlarmaConfigRegla regla = new AlarmaConfigRegla();
        regla.setTipusNode(AlarmaConfigReglaTipusNode.GRUP);
        regla.setFills(Collections.emptyList());
        assertThat((String) ReflectionTestUtils.invokeMethod(alarmaConfigService, "buildRuleSummary", regla)).isEmpty();
    }

    @Test
    @DisplayName("buildRuleSummary: construeix resum recursiu per a node GRUP amb fills")
    void buildRuleSummary_quanNodeGrupAmbFills_llavorsConstrueixResumRecursiu() {
        AlarmaConfigRegla fill1 = new AlarmaConfigRegla();
        fill1.setTipusNode(AlarmaConfigReglaTipusNode.CONDICIO);
        fill1.setAmbit(AlarmaConfigReglaAmbit.SISTEMA);
        fill1.setMetrica(AlarmaConfigReglaMetrica.ESTAT);
        fill1.setComparador(AlarmaConfigReglaComparador.IGUAL);
        fill1.setValorsText(Collections.singletonList("OK"));

        AlarmaConfigRegla fill2 = new AlarmaConfigRegla();
        fill2.setTipusNode(AlarmaConfigReglaTipusNode.CONDICIO);
        fill2.setAmbit(AlarmaConfigReglaAmbit.APLICACIO);
        fill2.setMetrica(AlarmaConfigReglaMetrica.LATENCIA);
        fill2.setComparador(AlarmaConfigReglaComparador.MAJOR);
        fill2.setValorNumeric(new BigDecimal("100"));

        AlarmaConfigRegla regla = new AlarmaConfigRegla();
        regla.setTipusNode(AlarmaConfigReglaTipusNode.GRUP);
        regla.setOperador(AlarmaConfigReglaOperador.OR);
        regla.setFills(Arrays.asList(fill1, fill2));

        String result = (String) ReflectionTestUtils.invokeMethod(alarmaConfigService, "buildRuleSummary", regla);
        assertThat(result).isEqualTo("Sistema estat IGUAL OK OR Aplicacio latència MAJOR 100");
    }

    @Test
    @DisplayName("beforeCreateSave: estableix atribut al ThreadLocal")
    void beforeCreateSave_quanEsCrida_llavorsEstableixThreadLocal() {
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        alarmaConfigService.beforeCreateSave(entity, new AlarmaConfig(), null);

        assertThat(ThreadLocalUtil.getAttribute(ThreadLocalUtil.REORDER_ADDITIONAL_PROPS_KEY, AlarmaConfigEntity.class)).isSameAs(entity);
        ThreadLocalUtil.setAttribute(ThreadLocalUtil.REORDER_ADDITIONAL_PROPS_KEY, null); // Cleanup
    }

    @Test
    @DisplayName("AuditoriaPerspectiveApplicator: omple noms complets quan els usuaris existeixen")
    void auditoriaPerspectiveApplicator_quanUsuarisExisteixen_llavorsOmpleNomsComplets() throws Exception {
        AlarmaConfigServiceImpl.AuditoriaPerspectiveApplicator applicator = alarmaConfigService.new AuditoriaPerspectiveApplicator();
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setCreatedBy("user1");
        entity.setLastModifiedBy("user2");
        AlarmaConfig resource = new AlarmaConfig();

        Usuari u1 = new Usuari();
        ReflectionTestUtils.setField(u1, "nom", "Nom 1");
        ReflectionTestUtils.setField(u1, "codi", "user1");
        Usuari u2 = new Usuari();
        ReflectionTestUtils.setField(u2, "nom", "Nom 2");
        ReflectionTestUtils.setField(u2, "codi", "user2");

        when(userInformationHelper.usuariFindByUsername("user1")).thenReturn(u1);
        when(userInformationHelper.usuariFindByUsername("user2")).thenReturn(u2);

        applicator.applySingle("CODE", entity, resource);

        assertThat(resource.getCreatedByFullName()).isEqualTo("Nom 1 (user1)");
        assertThat(resource.getLastModifiedByFullName()).isEqualTo("Nom 2 (user2)");
    }

    @Test
    @DisplayName("AuditoriaPerspectiveApplicator: no falla quan l'usuari no existeix")
    void auditoriaPerspectiveApplicator_quanUsuariNoExisteix_llavorsNoFall() throws Exception {
        AlarmaConfigServiceImpl.AuditoriaPerspectiveApplicator applicator = alarmaConfigService.new AuditoriaPerspectiveApplicator();
        AlarmaConfigEntity entity = new AlarmaConfigEntity();
        entity.setCreatedBy("user1");
        AlarmaConfig resource = new AlarmaConfig();

        when(userInformationHelper.usuariFindByUsername("user1")).thenReturn(null);

        applicator.applySingle("CODE", entity, resource);
        assertThat(resource.getCreatedByFullName()).isNull();
    }

    @Test
    @DisplayName("reorderFindLinesWithParent: retorna llista d'admin quan la fila actual és d'admin")
    void reorderFindLinesWithParent_quanFilaActualEsAdmin_llavorsRetornaLlistaAdmin() {
        AlarmaConfigEntity currentRow = new AlarmaConfigEntity();
        currentRow.setAdmin(true);
        currentRow.setEntornAppId(10L);
        when(alarmaConfigRepository.findById(1L)).thenReturn(Optional.of(currentRow));

        alarmaConfigService.reorderFindLinesWithParent(1L);
        verify(alarmaConfigRepository).findByEntornAppIdAndEsborratFalseAndAdminTrueOrderByOrdre(10L);
    }

    @Test
    @DisplayName("reorderFindLinesWithParent: retorna llista d'usuari quan la fila actual no és d'admin")
    void reorderFindLinesWithParent_quanFilaActualNoEsAdmin_llavorsRetornaLlistaUsuari() {
        AlarmaConfigEntity currentRow = new AlarmaConfigEntity();
        currentRow.setAdmin(false);
        currentRow.setEntornAppId(10L);
        currentRow.setCreatedBy("user1");
        when(alarmaConfigRepository.findById(1L)).thenReturn(Optional.of(currentRow));

        alarmaConfigService.reorderFindLinesWithParent(1L);
        verify(alarmaConfigRepository).findByEntornAppIdAndEsborratFalseAndAdminFalseAndCreatedByOrderByOrdre(10L, "user1");
    }

    @Test
    @DisplayName("reorderFindLinesWithParent: utilitza ThreadLocal quan selfId és null (creació)")
    void reorderFindLinesWithParent_quanSelfIdEsNull_llavorsUtilitzaThreadLocal() {
        AlarmaConfigEntity currentRow = new AlarmaConfigEntity();
        currentRow.setAdmin(false);
        currentRow.setEntornAppId(10L);
        ThreadLocalUtil.setAttribute(ThreadLocalUtil.REORDER_ADDITIONAL_PROPS_KEY, currentRow);
        when(authenticationHelper.getCurrentUserName()).thenReturn("user1");

        alarmaConfigService.reorderFindLinesWithParent(null);
        verify(alarmaConfigRepository).findByEntornAppIdAndEsborratFalseAndAdminFalseAndCreatedByOrderByOrdre(10L, "user1");
        ThreadLocalUtil.setAttribute(ThreadLocalUtil.REORDER_ADDITIONAL_PROPS_KEY, null); // Cleanup
    }
}
