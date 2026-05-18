package es.caib.comanda.alarmes.logic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventPublisher;
import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventTypes;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigCondicio;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigRegla;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaAmbit;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaComparador;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaMetrica;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaOperador;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaTipusNode;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigTipus;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaConfigService;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final AlarmaRepository alarmaRepository;
    private final ComandaSseEventPublisher comandaSseEventPublisher;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        register(AlarmaConfig.ALARMA_CONFIG_DELETE_ACTION, new DeleteAlarmaConfigAction());
    }

    @Override
    protected String additionalSpringFilter(String currentSpringFilter, String[] namedQueries) {
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
    }

    @Override
    protected void beforeUpdateEntity(AlarmaConfigEntity entity, AlarmaConfig resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        if ((entity.isAdmin() || entity.isCorreuGeneric()) && !authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)) {
            throw new ResourceNotUpdatedException(getResourceClass(), String.valueOf(entity.getId()), I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.AlarmaConfigServiceImpl.beforeUpdateEntity.not.admin"));
        }
        applyRuleConfiguration(entity, resource);
    }

    @Override
    protected AlarmaConfig entityToResource(AlarmaConfigEntity entity) {
        AlarmaConfig resource = super.entityToResource(entity);
        AlarmaConfigRegla regla = readRule(entity);
        resource.setRegla(regla);
        resource.setResumRegla(buildRuleSummary(regla));
        return resource;
    }

    private void applyRuleConfiguration(AlarmaConfigEntity entity, AlarmaConfig resource) {
        AlarmaConfigRegla regla = normalizeRule(resource);
        resource.setRegla(regla);
        entity.setRuleVersion(regla != null ? RULE_VERSION : null);
        entity.setRuleJson(writeRule(regla));
    }

    public AlarmaConfigRegla readRule(AlarmaConfigEntity entity) {
        if (entity.getRuleJson() != null && !entity.getRuleJson().isBlank()) {
            try {
                return objectMapper.readValue(entity.getRuleJson(), AlarmaConfigRegla.class);
            } catch (JsonProcessingException ex) {
                log.error("No s'ha pogut deserialitzar la regla de l'alarma {}", entity.getId(), ex);
            }
        }
        return legacyRule(entity);
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
            return legacyRule(resource);
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

    private AlarmaConfigRegla legacyRule(AlarmaConfigEntity entity) {
        if (entity.getTipus() == null) {
            return null;
        }
        if (entity.getTipus() == AlarmaConfigTipus.APP_CAIGUDA) {
            return AlarmaConfigRegla.builder()
                    .tipusNode(AlarmaConfigReglaTipusNode.GRUP)
                    .operador(AlarmaConfigReglaOperador.AND)
                    .fills(Collections.singletonList(
                            AlarmaConfigRegla.builder()
                                    .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                                    .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                                    .metrica(AlarmaConfigReglaMetrica.ESTAT)
                                    .comparador(AlarmaConfigReglaComparador.EN)
                                    .valorsText(Collections.singletonList("DOWN"))
                                    .build()))
                    .build();
        }
        if (entity.getTipus() == AlarmaConfigTipus.APP_LATENCIA) {
            return AlarmaConfigRegla.builder()
                    .tipusNode(AlarmaConfigReglaTipusNode.GRUP)
                    .operador(AlarmaConfigReglaOperador.AND)
                    .fills(Collections.singletonList(
                            AlarmaConfigRegla.builder()
                                    .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                                    .ambit(AlarmaConfigReglaAmbit.SISTEMA)
                                    .metrica(AlarmaConfigReglaMetrica.LATENCIA)
                                    .comparador(mapLegacyComparador(entity.getCondicio()))
                                    .valorNumeric(entity.getValor())
                                    .build()))
                    .build();
        }
        return null;
    }

    private AlarmaConfigRegla legacyRule(AlarmaConfig resource) {
        if (resource.getTipus() == null) {
            return null;
        }
        AlarmaConfigEntity legacyEntity = new AlarmaConfigEntity();
        legacyEntity.setTipus(resource.getTipus());
        legacyEntity.setCondicio(resource.getCondicio());
        legacyEntity.setValor(resource.getValor());
        return legacyRule(legacyEntity);
    }

    private AlarmaConfigReglaComparador mapLegacyComparador(AlarmaConfigCondicio condicio) {
        if (condicio == null) {
            return AlarmaConfigReglaComparador.MAJOR;
        }
        if (condicio == AlarmaConfigCondicio.MAJOR) {
            return AlarmaConfigReglaComparador.MAJOR;
        }
        if (condicio == AlarmaConfigCondicio.MAJOR_IGUAL) {
            return AlarmaConfigReglaComparador.MAJOR_IGUAL;
        }
        if (condicio == AlarmaConfigCondicio.MENOR) {
            return AlarmaConfigReglaComparador.MENOR;
        }
        return AlarmaConfigReglaComparador.MENOR_IGUAL;
    }

    private String buildRuleSummary(AlarmaConfigRegla regla) {
        if (regla == null) {
            return "";
        }
        if (regla.getTipusNode() == AlarmaConfigReglaTipusNode.GRUP) {
            if (regla.getFills() == null || regla.getFills().isEmpty()) {
                return "";
            }
            return regla.getFills().stream()
                    .map(this::buildRuleSummary)
                    .filter(text -> text != null && !text.isBlank())
                    .reduce((left, right) -> left + " " + regla.getOperador() + " " + right)
                    .orElse("");
        }

        String subject = "";
        if (regla.getAmbit() == AlarmaConfigReglaAmbit.APLICACIO) {
            subject = "Aplicacio";
        } else if (regla.getAmbit() == AlarmaConfigReglaAmbit.SUBSISTEMA) {
            subject = "Subsistema " + regla.getCodiObjecte();
        } else if (regla.getAmbit() == AlarmaConfigReglaAmbit.INTEGRACIO) {
            subject = "Integracio " + regla.getCodiObjecte();
        } else if (regla.getAmbit() == AlarmaConfigReglaAmbit.SISTEMA) {
            subject = "Sistema";
        }
        String metric = "";
        if (regla.getMetrica() == AlarmaConfigReglaMetrica.ESTAT) {
            metric = "estat";
        } else if (regla.getMetrica() == AlarmaConfigReglaMetrica.LATENCIA) {
            metric = "latencia";
        } else if (regla.getMetrica() == AlarmaConfigReglaMetrica.CARREGA_SISTEMA) {
            metric = "carrega";
        } else if (regla.getMetrica() == AlarmaConfigReglaMetrica.MEMORIA_DISPONIBLE) {
            metric = "memoria lliure";
        } else if (regla.getMetrica() == AlarmaConfigReglaMetrica.ESPAI_DISC_LLIURE) {
            metric = "disc lliure";
        }
        if (regla.getComparador() == AlarmaConfigReglaComparador.EN) {
            return subject + " " + metric + " en " + String.join(", ", regla.getValorsText());
        }
        BigDecimal valor = regla.getValorNumeric();
        return subject + " " + metric + " " + regla.getComparador() + " " + (valor != null ? valor.toPlainString() : "");
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

}
