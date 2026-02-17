package es.caib.comanda.alarmes.logic.service;

import es.caib.comanda.alarmes.logic.helper.AlarmaComprovacioHelper;
import es.caib.comanda.alarmes.logic.helper.AlarmaMailHelper;
import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.model.Alarma.AlarmaReduidaResource;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaService;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaConfigRepository;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
	private final AlarmaMailHelper alarmaMailHelper;
	private final AuthenticationHelper authenticationHelper;
    private final EntityManager entityManager;

	@PostConstruct
	public void init() {
		register(
				Alarma.ESBORRAR_ACTION,
				new EsborrarActionExecutor(authenticationHelper, (AlarmaRepository)entityRepository));
		register(
				Alarma.ESBORRAR_TOTES_ACTION,
				new EsborrarActionExecutor(authenticationHelper, (AlarmaRepository)entityRepository));
        register(
                Alarma.FIND_ACTIVES_REPORT,
                new ReportLlistatIdAlarmaActiva(entityManager));
	}

	@Override
	@Transactional
	public void comprovacioScheduledTask() {
		log.debug("Iniciant comprovació d'alarmes...");
		long activadesCount = alarmaConfigRepository.findAll().stream()
				.filter(alarmaComprovacioHelper::comprovar)
				.count();
		log.debug("...comprovació d'alarmes finalitzada ({} alarmes activades)", activadesCount);
	}

	@Override
	@Transactional
	public void enviamentsAgrupatsScheduledTask() {
		log.debug("Iniciant enviaments agrupats d'alarmes...");
		long mailCount = alarmaMailHelper.sendAlarmesAgrupades();
		log.debug("...enviaments agrupats d'alarmes finalitzat ({} correus enviats)", mailCount);
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

	@RequiredArgsConstructor
	public static class EsborrarActionExecutor implements ActionExecutor<AlarmaEntity, Serializable, Serializable> {
		private final AuthenticationHelper authenticationHelper;
		private final AlarmaRepository alarmaRepository;
		@Override
		public Serializable exec(String code, AlarmaEntity entity, Serializable params) {
			String currentUser = authenticationHelper.getCurrentUserName();
			boolean isCurrentUserAdmin = authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN);
			if (Alarma.ESBORRAR_ACTION.equals(code) && entity != null) {
				boolean alarmaIsAdmin = entity.getAlarmaConfig().isAdmin();
				String alarmaCreatedBy = entity.getAlarmaConfig().getCreatedBy();
				boolean tePermisos = (alarmaIsAdmin && isCurrentUserAdmin) || (!alarmaIsAdmin && currentUser.equals(alarmaCreatedBy));
				if (tePermisos) {
					entity.setEstat(AlarmaEstat.ESBORRADA);
					entity.setDataEsborrat(LocalDateTime.now());
				} else {
					throw new ActionExecutionException(
							Alarma.class,
							entity.getId(),
							code,
							"Sense permisos per a esborrar l'alarma");
				}
			} else if (Alarma.ESBORRAR_TOTES_ACTION.equals(code)) {
				alarmaRepository.updateAllEstatEsborradaNoAdmin(
						currentUser,
						AlarmaEstat.ACTIVA,
						AlarmaEstat.ESBORRADA);
				if (isCurrentUserAdmin) {
					alarmaRepository.updateAllEstatEsborradaAdmin(
							AlarmaEstat.ACTIVA,
							AlarmaEstat.ESBORRADA);
				}
			}
			return null;
		}
		@Override
		public void onChange(Serializable id, Serializable previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Serializable target) {
		}
	}

    @RequiredArgsConstructor
    private class ReportLlistatIdAlarmaActiva implements ReportGenerator<AlarmaEntity, Serializable, AlarmaReduidaResource> {
        private final EntityManager entityManager;

        @Override
        public List<AlarmaReduidaResource> generateData(String code, AlarmaEntity alarmaEntity, Serializable params) throws ReportGenerationException {
            Specification<AlarmaEntity> spec = toFindProcessedSpecification(
                    null,
                    Alarma.Fields.estat + ":'" + AlarmaEstat.ACTIVA.name() + "'",
                    null
            );
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<AlarmaEntity> root = query.from(AlarmaEntity.class);

            Predicate predicate = spec.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
            query.select(root.get("id"));
            List<Long> ids = entityManager.createQuery(query).getResultList();
            List<AlarmaReduidaResource> recursos = ids.stream()
                    .map(AlarmaReduidaResource::new)
                    .collect(Collectors.toList());
            return recursos;
        }

        @Override
        public void onChange(Serializable id, Serializable previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Serializable target) {
        }
    }
}
