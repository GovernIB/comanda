package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.ParamTipus;
import es.caib.comanda.configuracio.logic.intf.model.Parametre;
import es.caib.comanda.configuracio.logic.intf.service.ParametreService;
import es.caib.comanda.configuracio.persist.entity.ParametreEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotDeletedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Implementació del servei de gestió de paràmetres.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ParametreServiceImpl extends BaseMutableResourceService<Parametre, Long, ParametreEntity> implements ParametreService {

	private final String PASSWORD_LABEL = "********";

	@Override
	protected void afterConversion(ParametreEntity entity, Parametre resource) {
		switch (resource.getTipus()){
			case NUMERIC:
				resource.setValorNumeric(new BigDecimal(resource.getValor()));
				break;
			case BOOLEAN:
				resource.setValorBoolean(Objects.isNull(resource.getValor()) ? null : resource.getValor().equalsIgnoreCase("true"));
				break;
			case PASSWORD://Si un parametro es de tipo Password no enviaremos ese valor desde el api.
				resource.setValor(PASSWORD_LABEL);
				break;
		}
	}

	@Override
	protected void completeResource(Parametre resource) {
		switch (resource.getTipus()) {
			case NUMERIC:
				resource.setValor(resource.getValorNumeric().toString());
				break;
			case BOOLEAN:
				resource.setValor(Objects.isNull(resource.getValorBoolean()) ? null : resource.getValorBoolean() ? "true" : "false");
				break;
		}
	}

	@Override
	protected void beforeCreateEntity(ParametreEntity entity, Parametre resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {
		throw new ResourceNotCreatedException(getResourceClass(), I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.ParametreServiceImpl.beforeCreateEntity.disabled"));
	}

	@Override
	protected void beforeUpdateEntity(ParametreEntity entity, Parametre resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
		resource.setEditable(entity.isEditable());
		if (!entity.isEditable()) {
			throw new ResourceNotUpdatedException(getResourceClass(), String.valueOf(entity.getId()), I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.ParametreServiceImpl.beforeUpdateEntity.disabled"));
		}
		if (Objects.equals(ParamTipus.PASSWORD, resource.getTipus())) {
			if (Objects.equals(PASSWORD_LABEL, resource.getValor())) {
				resource.setValor(entity.getValor());
			}
		}
	}

	@Override
	protected void beforeDelete(ParametreEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotDeletedException {
		throw new ResourceNotDeletedException(getResourceClass(), String.valueOf(entity.getId()), I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.ParametreServiceImpl.beforeUpdateEntity.disabled"));
	}

//    private final CacheHelper cacheHelper;

//    @Override
//    protected void afterUpdateSave(ParametreEntity entity, Parametre resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
//        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
//        cacheHelper.evictCacheItem(PARAMETRE_CACHE, entity.getId().toString());
//    }

}
