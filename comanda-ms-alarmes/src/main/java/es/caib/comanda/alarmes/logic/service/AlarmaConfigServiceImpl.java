package es.caib.comanda.alarmes.logic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.alarmes.logic.helper.AlarmaClientHelper;
import es.caib.comanda.alarmes.logic.helper.UserInformationHelper;
import es.caib.comanda.alarmes.logic.intf.model.*;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaConfigService;
import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventPublisher;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaConfigRepository;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.alarmes.persist.repository.AlarmaUsuariRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.AppIntegracio;
import es.caib.comanda.client.model.AppSubsistema;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.IntegracioRef;
import es.caib.comanda.client.model.Usuari;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.*;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.intf.util.ThreadLocalUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementació del servei de gestió d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmaConfigServiceImpl extends BaseMutableResourceService<AlarmaConfig, Long, AlarmaConfigEntity> implements AlarmaConfigService {
    private static final int RULE_VERSION = 1;

    private final AuthenticationHelper authenticationHelper;
    private final AlarmaConfigRepository alarmaConfigRepository;
    private final AlarmaRepository alarmaRepository;
    private final AlarmaUsuariRepository alarmaUsuariRepository;
    private final ComandaSseEventPublisher comandaSseEventPublisher;
    private final ObjectMapper objectMapper;
    private final UserInformationHelper userInformationHelper;
    private final AlarmaClientHelper alarmaClientHelper;

    @Override
    @Transactional
    public void netejaPerEntornApp(Long entornAppId) {
        alarmaUsuariRepository.deleteByAlarmaEntornAppId(entornAppId);
        alarmaRepository.deleteByEntornAppId(entornAppId);
        alarmaConfigRepository.deleteByEntornAppId(entornAppId);
    }

    @PostConstruct
    public void init() {
        register(AlarmaConfig.ALARMA_CONFIG_DELETE_ACTION, new DeleteAlarmaConfigAction());
        register(AlarmaConfig.PERSPECTIVE_AUDIT_CODE, new AuditoriaPerspectiveApplicator());
    }

    @Override
    protected String additionalSpringFilter(String currentSpringFilter, String[] namedQueries, boolean isSingleResult) {
        String baseFilter = "esborrat:false";
        String currentUser = authenticationHelper.getCurrentUserName();
        boolean isAdmin = authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN);
        if (isAdmin) return baseFilter;
        return baseFilter + " and createdBy:'" + currentUser + "'";
    }

    @Override
    protected void beforeCreateEntity(AlarmaConfigEntity entity, AlarmaConfig resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {
        if ((resource.isAdmin() || resource.isCorreuGeneric()) && !authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)){
            throw new ResourceNotCreatedException(resource.getClass(), I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.AlarmaConfigServiceImpl.beforeCreateEntity.not.admin"));
        }
        applyRuleConfiguration(entity, resource);
        if (!resource.isAdmin() && resource.isCorreuGeneric()) {
            resource.setCorreuGeneric(false);
        }
    }

    @Override
    protected void beforeUpdateEntity(AlarmaConfigEntity entity, AlarmaConfig resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        if ((entity.isAdmin() || entity.isCorreuGeneric()) && !authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)) {
            throw new ResourceNotUpdatedException(getResourceClass(), String.valueOf(entity.getId()), I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.AlarmaConfigServiceImpl.beforeUpdateEntity.not.admin"));
        }
        applyRuleConfiguration(entity, resource);
        logicBeforeUpdateEntityAlarmaConfig(entity, resource);
    }

    @Override
    protected AlarmaConfig entityToResource(AlarmaConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        AlarmaConfig resource = super.entityToResource(entity);
        if (resource == null) {
            return null;
        }
        AlarmaConfigRegla regla = readRule(entity);
        resource.setRegla(regla);
        EntornApp entornApp = entity.getEntornAppId() != null
                ? alarmaClientHelper.entornAppFindByIdWithIntegracionsSubsistemesContexts(entity.getEntornAppId())
                : null;
        resource.setResumRegla(buildRuleSummary(regla, entornApp));
        return resource;
    }

    private void applyRuleConfiguration(AlarmaConfigEntity entity, AlarmaConfig resource) {
        AlarmaConfigRegla regla = normalizeRule(resource);
        resource.setRegla(regla);
        entity.setRuleVersion(regla != null ? RULE_VERSION : null);
        entity.setRuleJson(writeRule(regla));
    }

    /** Evita que alguns atributs canviï de valor, sense ús d'anotació. Perquè des del frontal es limitaran les opcions de forma més visual. **/
    private void logicBeforeUpdateEntityAlarmaConfig(AlarmaConfigEntity entity, AlarmaConfig resource) {
        resource.setEntornAppId(entity.getEntornAppId());
        boolean isCurrentUserAdmin = authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN);
        if (!isCurrentUserAdmin) {
            resource.setAdmin(entity.isAdmin());
            if (!entity.isAdmin() && resource.isCorreuGeneric()) {
                throw new ResourceNotUpdatedException(getResourceClass(), String.valueOf(entity.getId()), I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.AlarmaConfigServiceImpl.beforeUpdateEntity.correuGeneric.not.admin"));
            }
        } else {
            if (entity.isAdmin() != resource.isAdmin()) {
                // Quan el flag d'admin canvia, col·locar l'alarma al final de l'ordenació del nou grup
                resource.setOrdre(null);
            }
            if (!resource.isAdmin() && resource.isCorreuGeneric()) {
                resource.setCorreuGeneric(false);
            }
        }
    }

    public AlarmaConfigRegla readRule(AlarmaConfigEntity entity) {
        if (entity.getRuleJson() != null && !entity.getRuleJson().isBlank()) {
            try {
                return objectMapper.readValue(entity.getRuleJson(), AlarmaConfigRegla.class);
            } catch (JsonProcessingException ex) {
                log.error("No s'ha pogut deserialitzar la regla de l'alarma {}", entity.getId(), ex);
            }
        }
        return null;
    }

    private String writeRule(AlarmaConfigRegla regla) {
        if (regla == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(regla);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("No s'ha pogut serialitzar la regla de l'alarma", ex);
        }
    }

    private AlarmaConfigRegla normalizeRule(AlarmaConfig resource) {
        AlarmaConfigRegla regla = resource.getRegla();
        if (regla == null) {
            return null;
        }
        if (regla.getTipusNode() == null) {
            regla.setTipusNode(AlarmaConfigReglaTipusNode.GRUP);
        }
        if (regla.getTipusNode() == AlarmaConfigReglaTipusNode.GRUP) {
            if (regla.getOperador() == null) {
                regla.setOperador(AlarmaConfigReglaOperador.AND);
            }
            regla.setFills(regla.getFills() == null
                    ? Collections.emptyList()
                    : regla.getFills().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        } else {
            regla.setValorsText(regla.getValorsText() == null
                    ? Collections.emptyList()
                    : regla.getValorsText().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return regla;
    }

    private String buildRuleSummary(AlarmaConfigRegla regla) {
        return buildRuleSummary(regla, null);
    }

    private String buildRuleSummary(AlarmaConfigRegla regla, EntornApp entornApp) {
        if (regla == null) {
            return "";
        }
        if (regla.getTipusNode() == AlarmaConfigReglaTipusNode.GRUP) {
            if (regla.getFills() == null || regla.getFills().isEmpty()) {
                return "";
            }
            String operadorText = "";
            if (regla.getOperador() != null) {
                String key = "es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.operador." + regla.getOperador().name();
                operadorText = I18nUtil.getInstance().getI18nMessage(key);
                if (operadorText == null || operadorText.equals(key)) {
                    operadorText = regla.getOperador().name();
                }
            }
            String sep = operadorText.isBlank() ? " " : " " + operadorText + " ";
            return regla.getFills().stream()
                    .map(fill -> buildRuleSummary(fill, entornApp))
                    .filter(text -> text != null && !text.isBlank())
                    .reduce((left, right) -> left + sep + right)
                    .orElse("");
        }

        String subject = "";
        if (regla.getAmbit() == AlarmaConfigReglaAmbit.APLICACIO) {
            subject = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.ambit.APLICACIO");
        } else if (regla.getAmbit() == AlarmaConfigReglaAmbit.SUBSISTEMA) {
            String nom = resolveSubsystemName(entornApp, regla.getCodiObjecte());
            subject = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.ambit.SUBSISTEMA", nom);
        } else if (regla.getAmbit() == AlarmaConfigReglaAmbit.INTEGRACIO) {
            String nom = resolveIntegrationName(entornApp, regla.getCodiObjecte());
            subject = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.ambit.INTEGRACIO", nom);
        } else if (regla.getAmbit() == AlarmaConfigReglaAmbit.SISTEMA) {
            subject = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.ambit.SISTEMA");
        }

        String metric = "";
        if (regla.getMetrica() == AlarmaConfigReglaMetrica.ESTAT) {
            metric = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.metrica.ESTAT");
        } else if (regla.getMetrica() == AlarmaConfigReglaMetrica.LATENCIA) {
            metric = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.metrica.LATENCIA");
        } else if (regla.getMetrica() == AlarmaConfigReglaMetrica.CARREGA_MITJANA_SISTEMA) {
            metric = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.metrica.CARREGA_MITJANA_SISTEMA");
        } else if (regla.getMetrica() == AlarmaConfigReglaMetrica.MEMORIA_DISPONIBLE) {
            metric = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.metrica.MEMORIA_DISPONIBLE");
        } else if (regla.getMetrica() == AlarmaConfigReglaMetrica.ESPAI_DISC_LLIURE) {
            metric = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.metrica.ESPAI_DISC_LLIURE");
        }

        String comparador = regla.getComparador() == null ? "" : I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.comparador." + regla.getComparador().name());

        if (regla.getMetrica() == AlarmaConfigReglaMetrica.ESTAT) {
            String valorsTranslated = regla.getValorsText() == null ? "" : regla.getValorsText().stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> I18nUtil.getInstance().getI18nMessage("es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.estat." + v))
                .collect(Collectors.joining(", "));
            return Stream.of(subject, metric, comparador, valorsTranslated)
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" "));
        }

        BigDecimal valor = regla.getValorNumeric();
        String formattedValue = formatValorNumeric(valor, regla.getMetrica());
        return Stream.of(subject, metric, comparador, formattedValue)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    private String resolveSubsystemName(EntornApp entornApp, String codi) {
        if (entornApp != null && entornApp.getSubsistemes() != null && codi != null) {
            return entornApp.getSubsistemes().stream()
                    .filter(s -> s != null && codi.trim().equalsIgnoreCase(s.getCodi() != null ? s.getCodi().trim() : ""))
                    .map(AppSubsistema::getNom)
                    .filter(nom -> nom != null && !nom.isBlank())
                    .findFirst()
                    .orElse(codi.trim());
        }
        return codi != null ? codi.trim() : "";
    }

    private String resolveIntegrationName(EntornApp entornApp, String codi) {
        if (entornApp != null && entornApp.getIntegracions() != null && codi != null) {
            return entornApp.getIntegracions().stream()
                    .filter(i -> i != null && codi.trim().equalsIgnoreCase(i.getCodi() != null ? i.getCodi().trim() : ""))
                    .map(AppIntegracio::getIntegracio)
                    .filter(Objects::nonNull)
                    .map(IntegracioRef::getNom)
                    .filter(nom -> nom != null && !nom.isBlank())
                    .findFirst()
                    .orElse(codi.trim());
        }
        return codi != null ? codi.trim() : "";
    }

    private String formatValorNumeric(BigDecimal valor, AlarmaConfigReglaMetrica metrica) {
        if (valor == null) return "";
        Locale locale = LocaleContextHolder.getLocale();
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        nf.setGroupingUsed(true);
        int scale = Math.max(0, valor.stripTrailingZeros().scale());
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(scale);
        String formattedValue = nf.format(valor);
        String unit = getUnitForMetric(metrica);
        return unit.isEmpty() ? formattedValue : formattedValue + " " + unit;
    }

    private String getUnitForMetric(AlarmaConfigReglaMetrica metrica) {
        if (metrica == AlarmaConfigReglaMetrica.LATENCIA) {
            String key = "es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.unitat.ms";
            String unit = I18nUtil.getInstance().getI18nMessage(key);
            return (unit != null && !unit.equals(key)) ? unit : "ms";
        } else if (metrica == AlarmaConfigReglaMetrica.MEMORIA_DISPONIBLE || metrica == AlarmaConfigReglaMetrica.ESPAI_DISC_LLIURE) {
            String key = "es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig.unitat.MB";
            String unit = I18nUtil.getInstance().getI18nMessage(key);
            return (unit != null && !unit.equals(key)) ? unit : "MB";
        }
        return "";
    }

    private class DeleteAlarmaConfigAction implements ActionExecutor<AlarmaConfigEntity, String, AlarmaConfig> {

        @Override
        public AlarmaConfig exec(String code, AlarmaConfigEntity entity, String params) throws ActionExecutionException {
            entity.setEsborrat(true);

            alarmaRepository.deleteByAlarmaConfigAndEstat(entity, AlarmaEstat.ESBORRANY);
            alarmaRepository.finalizeByAlarmaConfig(entity, LocalDateTime.now());
            comandaSseEventPublisher.publish(ComandaSseEventTypes.ACTIVE_ALARMS_CHANGED);

            return null;
        }

        @Override
        public void onChange(Serializable id, String previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, String target) {
        }
    }

	@Override
	protected void beforeCreateSave(AlarmaConfigEntity entity, AlarmaConfig resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
		ThreadLocalUtil.setAttribute(ThreadLocalUtil.REORDER_ADDITIONAL_PROPS_KEY, entity);
	}

    public class AuditoriaPerspectiveApplicator implements PerspectiveApplicator<AlarmaConfigEntity, AlarmaConfig> {
        @Override
        public void applySingle(String code, AlarmaConfigEntity entity, AlarmaConfig resource) throws PerspectiveApplicationException {
            if (entity.getCreatedBy()!=null) {
                Usuari usuari = userInformationHelper.usuariFindByUsername(entity.getCreatedBy());
                if (usuari!=null) {
                    resource.setCreatedByFullName(usuari.getNom() + " (" + usuari.getCodi() + ")");
                }
            }
            if (entity.getLastModifiedBy()!=null) {
                Usuari usuari = userInformationHelper.usuariFindByUsername(entity.getLastModifiedBy());
                resource.setLastModifiedByFullName(usuari.getNom() + " (" + usuari.getCodi() + ")");
            }
        }
    }

	// L'ordenació és individual de cada usuari i entornApp,
	// menys quan no es tracta d'una alarma d'administrador, en aquell cas
	// l'ordenació és individual de cada entornApp però global per a tots els administradors.
	@Override
	protected List<AlarmaConfigEntity> reorderFindLinesWithParentAndSorted(Serializable selfIdSerializable) {
        boolean isCurrentRowAdmin;
        Long currentRowEntornAppId;
        String currentRowCreatedBy;

		if (selfIdSerializable != null) {
			// El parentId fa referència al mateix AlarmaConfigEntity per a poder
			// recuperar la row actual al mètode reorderFindLinesWithParentAndSorted
			Long selfId = (Long) selfIdSerializable;
            AlarmaConfigEntity currentRow = alarmaConfigRepository.findById(selfId).orElseThrow();
			isCurrentRowAdmin = currentRow.isAdmin();
            currentRowEntornAppId = currentRow.getEntornAppId();
            currentRowCreatedBy = currentRow.getCreatedBy();
		} else {
			// Al fer una creació, el selfId no s'ha emplenat encara i es recupera la row actual a través del ThreadLocal
            AlarmaConfigEntity currentRow = ThreadLocalUtil.getAttribute(ThreadLocalUtil.REORDER_ADDITIONAL_PROPS_KEY, AlarmaConfigEntity.class);
			isCurrentRowAdmin = currentRow.isAdmin();
            currentRowEntornAppId = currentRow.getEntornAppId();
            currentRowCreatedBy = authenticationHelper.getCurrentUserName();
        }

		if (isCurrentRowAdmin) {
			return alarmaConfigRepository.findByEntornAppIdAndEsborratFalseAndAdminTrueOrderByOrdre(
				currentRowEntornAppId
			);
		} else {
			return alarmaConfigRepository.findByEntornAppIdAndEsborratFalseAndAdminFalseAndCreatedByOrderByOrdre(
				currentRowEntornAppId,
				currentRowCreatedBy
			);
		}
	}
}
