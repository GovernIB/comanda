package es.caib.comanda.alarmes.logic.service;

import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventPublisher;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.alarmes.logic.helper.AlarmaComprovacioHelper;
import es.caib.comanda.alarmes.logic.helper.AlarmaMailHelper;
import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.model.Alarma.EsborrarActionParams;
import es.caib.comanda.alarmes.logic.intf.model.Alarma.AlarmaReduidaResource;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaService;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaConfigRepository;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.ms.logic.intf.exception.ArtifactNotFoundException;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementació del servei de gestió d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmaServiceImpl extends BaseMutableResourceService<Alarma, Long, AlarmaEntity> implements AlarmaService {

	private final AlarmaComprovacioHelper alarmaComprovacioHelper;
	private final AlarmaConfigRepository alarmaConfigRepository;
	private final AlarmaRepository alarmaRepository;
	private final AlarmaMailHelper alarmaMailHelper;
	private final AuthenticationHelper authenticationHelper;
    private final EntityManager entityManager;
    private final ComandaSseEventPublisher comandaSseEventPublisher;
    private final ParametresHelper parametresHelper;

	@Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{true}}")
	private Boolean schedulerLeader;
	@Value("${" + BaseConfig.PROP_SCHEDULER_BACK + ":#{false}}")
	private Boolean schedulerBack;

	@PostConstruct
	public void init() {
		register(Alarma.ESBORRAR_ACTION, new EsborrarActionExecutor());
        register(Alarma.ESBORRAR_MULTIPLE_ACTION, new EsborrarMultipleActionExecutor());
//		register(Alarma.ESBORRAR_TOTES_ACTION, new EsborrarActionExecutor());
        register(Alarma.REACTIVAR_ACTION, new ReactivarActionExecutor());
        register(Alarma.FIND_ACTIVES_REPORT, new ReportLlistatIdAlarmaActiva());
	}

	@Override
	@Transactional
	public void comprovacioScheduledTask() {
		if (!isLeader()) {
			return;
		}
		log.debug("Iniciant comprovació d'alarmes...");
		List<AlarmaConfigEntity> alarms = alarmaConfigRepository.findAllByEsborratFalse();
		Map<GroupKey, List<AlarmaConfigEntity>> alarmaConfigGroups =
			alarms.stream()
				.collect(Collectors.groupingBy(alarmConfig ->
					alarmConfig.isAdmin()
						? new GroupKey(true, alarmConfig.getEntornAppId(), null)
						: new GroupKey(false, alarmConfig.getEntornAppId(), alarmConfig.getCreatedBy())));
		boolean logActivacio = isLogActivacio();
		log.debug("{} grups d'alarmes generats", alarmaConfigGroups.size());
		if (logActivacio) {
			log.info("[ALARMA] Inici comprovació: {} grups, {} configs totals", alarmaConfigGroups.size(), alarms.size());
		}
		long activadesCount = 0;
		for (Map.Entry<GroupKey, List<AlarmaConfigEntity>> entry : alarmaConfigGroups.entrySet()) {
			List<AlarmaConfigEntity> sortedAlarms = entry.getValue().stream()
					.sorted(Comparator.comparing(AlarmaConfigEntity::getOrdre, Comparator.nullsLast(Comparator.naturalOrder())))
					.collect(Collectors.toList());
			if (logActivacio) {
				log.info("[ALARMA] Grup {} -> {} alarmes: {}",
						entry.getKey(),
						sortedAlarms.size(),
						sortedAlarms.stream()
								.map(a -> "configId=" + a.getId() + "('" + a.getNom() + "'" + (a.isAturarAvaluacioPosteriors() ? ",ATURA" : "") + ")")
								.collect(Collectors.joining(", ")));
			}
			for (AlarmaConfigEntity alarmaConfig : sortedAlarms) {
				boolean alarmaActivada = alarmaComprovacioHelper.comprovar(alarmaConfig);
				if (!alarmaActivada) continue;

				activadesCount++;
				if (alarmaConfig.isAturarAvaluacioPosteriors()) {
					log.debug("L'alarma {} s'ha activat i ha aturat l'execució del grup {}", alarmaConfig.getId(), entry.getKey());
					if (logActivacio) {
						log.info("[ALARMA] configId={} ('{}'): activa + aturarAvaluacioPosteriors=true -> s'atura el grup {}",
								alarmaConfig.getId(), alarmaConfig.getNom(), entry.getKey());
					}
					break;
				}
			}
		}
		log.debug("...comprovació d'alarmes finalitzada ({} alarmes activades)", activadesCount);
		if (logActivacio) {
			log.info("[ALARMA] Fi comprovació: {} alarmes actives/en curs", activadesCount);
		}
	}

	@Override
	@Transactional
	public void enviamentsAgrupatsScheduledTask() {
		if (!isLeader()) {
			return;
		}
		log.debug("Iniciant enviaments agrupats d'alarmes...");
		long mailCount = alarmaMailHelper.sendAlarmesAgrupades();
		log.debug("...enviaments agrupats d'alarmes finalitzat ({} correus enviats)", mailCount);
	}

    @Override
    @Transactional(readOnly = true)
    public List<AlarmaReduidaResource> findActiveAlarmIdsForSubscriber(String currentUser, boolean isAdmin) {
        List<AlarmaEntity> activeAlarms = new ArrayList<>(
                alarmaRepository.findByEstatAndAlarmaConfigAdminFalseAndAlarmaConfigCreatedBy(
                        AlarmaEstat.ACTIVA,
                        currentUser));
        if (isAdmin) {
            activeAlarms.addAll(alarmaRepository.findByEstatAndAlarmaConfigAdminTrue(AlarmaEstat.ACTIVA));
        }
        return activeAlarms.stream()
                .collect(Collectors.toMap(
                        AlarmaEntity::getId,
                        entity -> new AlarmaReduidaResource(entity.getId(), entity.getEntornAppId()),
                        (first, ignored) -> first,
                        LinkedHashMap::new))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public <P extends Serializable> Serializable artifactActionExec(
            Long id,
            String code,
            P params) throws ArtifactNotFoundException, ActionExecutionException {
        Serializable response = super.artifactActionExec(id, code, params);
        if (Alarma.ESBORRAR_ACTION.equals(code) || Alarma.REACTIVAR_ACTION.equals(code)) {
            publishActiveAlarmsChangedEvent();
        }
        return response;
    }

	@Override
	protected String additionalSpringFilter(
			String currentSpringFilter,
			String[] namedQueries) {
		String currentUser = authenticationHelper.getCurrentUserName();
		boolean isAdmin = authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN);
		if (!isAdmin) {
			return "alarmaConfig.admin:false and alarmaConfig.createdBy:'" + currentUser + "'";
		} else {
			return "alarmaConfig.admin:true or (alarmaConfig.admin:false and alarmaConfig.createdBy:'" + currentUser + "')";
		}
	}

	public class EsborrarActionExecutor implements ActionExecutor<AlarmaEntity, Serializable, Serializable> {
		@Override
		public Serializable exec(String code, AlarmaEntity entity, Serializable params) {
			if (Alarma.ESBORRAR_ACTION.equals(code) && entity != null) {
                logicEsborrarAction(entity, code, false);
                publishActiveAlarmsChangedEvent();
            }
//            else if (Alarma.ESBORRAR_TOTES_ACTION.equals(code)) {
//				alarmaRepository.updateAllEstatEsborradaNoAdmin(
//						currentUser,
//						AlarmaEstat.ACTIVA,
//						AlarmaEstat.ESBORRADA);
//				if (isCurrentUserAdmin) {
//					alarmaRepository.updateAllEstatEsborradaAdmin(
//							AlarmaEstat.ACTIVA,
//							AlarmaEstat.ESBORRADA);
//				}
//			}
			return null;
		}
		@Override
		public void onChange(Serializable id, Serializable previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Serializable target) {
		}
	}

    public class EsborrarMultipleActionExecutor implements ActionExecutor<AlarmaEntity, EsborrarActionParams, Serializable> {
        @Override
        public Serializable exec(String code, AlarmaEntity entity, EsborrarActionParams params) {
            if (params == null || params.getIds() == null || params.getIds().isEmpty()) {
                throw new ActionExecutionException(Alarma.class, null, code, "No hi ha elements que processar");
            }
            List<AlarmaEntity> alarmes = alarmaRepository.findAllById(params.getIds());
            for (AlarmaEntity alarma : alarmes) {
                logicEsborrarAction(alarma, code, true);
            }
            publishActiveAlarmsChangedEvent();
            return null;
        }
        @Override
        public void onChange(Serializable id, EsborrarActionParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, EsborrarActionParams target) {
        }
    }

    /** Lògica per a marcar com a llegit una alarma, per a les accions
     *  {@link es.caib.comanda.alarmes.logic.service.AlarmaServiceImpl.EsborrarActionExecutor EsborrarActionExecutor} i
     *  {@link es.caib.comanda.alarmes.logic.service.AlarmaServiceImpl.EsborrarMultipleActionExecutor EsborrarMultipleActionExecutor} **/
    private void logicEsborrarAction(AlarmaEntity entity, String code, boolean ignoreEstat) {
        if (usuariSensePermisosActionEsborrar(
                entity,
                authenticationHelper.getCurrentUserName(),
                authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN))) {
            throw new ActionExecutionException(
                    Alarma.class,
                    entity.getId(),
                    code,
                    "Sense permisos per executar l'acció");
        }
        if (entity.getEstat() != AlarmaEstat.ACTIVA) {
            if (ignoreEstat) {return;}
            throw new ActionExecutionException(
                    Alarma.class,
                    entity.getId(),
                    code,
                    "L'alarma ha d'estar sense llegir");
        }
        entity.setEstat(AlarmaEstat.ESBORRADA);
        entity.setDataEsborrat(LocalDateTime.now());
    }

    public class ReactivarActionExecutor implements ActionExecutor<AlarmaEntity, Serializable, Serializable> {
        @Override
        public Serializable exec(String code, AlarmaEntity entity, Serializable params) {
            if (Alarma.REACTIVAR_ACTION.equals(code) && entity != null) {
                if (usuariSensePermisosActionEsborrar(
                        entity,
                        authenticationHelper.getCurrentUserName(),
                        authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN))) {
                    throw new ActionExecutionException(
                            Alarma.class,
                            entity.getId(),
                            code,
                            "Sense permisos per a reactivar l'alarma");
                }
                if (entity.getEstat() != AlarmaEstat.ESBORRADA) {
                    throw new ActionExecutionException(
                            Alarma.class,
                            entity.getId(),
                            code,
                            "Només es poden reactivar alarmes esborrades");
                }
                entity.setEstat(AlarmaEstat.ACTIVA);
                entity.setDataEsborrat(null);
                publishActiveAlarmsChangedEvent();
            }
            return null;
        }
        @Override
        public void onChange(Serializable id, Serializable previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Serializable target) {
        }
    }

    private boolean usuariSensePermisosActionEsborrar(AlarmaEntity entity, String currentUser, boolean isCurrentUserAdmin) {
        boolean alarmaIsAdmin = entity.getAlarmaConfig().isAdmin();
        String alarmaCreatedBy = entity.getAlarmaConfig().getCreatedBy();
        return (!alarmaIsAdmin || !isCurrentUserAdmin) && (alarmaIsAdmin || !currentUser.equals(alarmaCreatedBy));
    }

    @Override
    protected void afterCreateSave(AlarmaEntity entity, Alarma resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        publishActiveAlarmsChangedEvent();
    }

    @Override
    protected void afterUpdateSave(AlarmaEntity entity, Alarma resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        publishActiveAlarmsChangedEvent();
    }

    @Override
    protected void afterDelete(AlarmaEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
        publishActiveAlarmsChangedEvent();
    }

    private void publishActiveAlarmsChangedEvent() {
        comandaSseEventPublisher.publish(ComandaSseEventTypes.ACTIVE_ALARMS_CHANGED);
    }

    private class ReportLlistatIdAlarmaActiva implements ReportGenerator<AlarmaEntity, Serializable, AlarmaReduidaResource> {
        @Override
        public List<AlarmaReduidaResource> generateData(String code, AlarmaEntity alarmaEntity, Serializable params) throws ReportGenerationException {
            Specification<AlarmaEntity> spec = toFindProcessedSpecification(
                    null,
                    Alarma.Fields.estat + ":'" + AlarmaEstat.ACTIVA.name() + "'",
                    null
            );
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
            Root<AlarmaEntity> root = query.from(AlarmaEntity.class);

            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
            query.multiselect(root.get("id"), root.get("entornAppId"));
            List<Object[]> rows = entityManager.createQuery(query).getResultList();
            List<AlarmaReduidaResource> recursos = rows.stream()
                    .map(row -> new AlarmaReduidaResource((Long) row[0], (Long) row[1]))
                    .collect(Collectors.toList());
            return recursos;
        }

        @Override
        public void onChange(Serializable id, Serializable previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Serializable target) {
        }
    }

	@lombok.Value
	private static class GroupKey {
		boolean isAdmin;
		Long entornAppId;
		String createdBy;
	}

	private boolean isLogActivacio() {
		return Boolean.TRUE.equals(parametresHelper.getParametreBoolean(BaseConfig.PROP_ALARMA_LOG_ACTIVACIO, false));
	}

	private boolean isLeader() {
		// TODO: Implementar per microserveis
		return schedulerLeader && schedulerBack;
	}
}
