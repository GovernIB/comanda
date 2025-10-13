package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.EstadisticaServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.ParamTipus;
import es.caib.comanda.configuracio.logic.intf.model.Parametre;
import es.caib.comanda.configuracio.logic.intf.service.ParametreService;
import es.caib.comanda.configuracio.persist.entity.ParametreEntity;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotDeletedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.Objects;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.PARAMETRE_CACHE;

/**
 * Implementació del servei de gestió de paràmetres.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ParametreServiceImpl extends BaseMutableResourceService<Parametre, Long, ParametreEntity> implements ParametreService {

    private final MonitorServiceClient monitorServiceClient;
    private final EstadisticaServiceClient estadisticaServiceClient;
    private final CacheHelper cacheHelper;
    private final AuthenticationHelper authenticationHelper;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final ApplicationEventPublisher eventPublisher;
    private final Environment environment;

    private static final String PASSWORD_LABEL = "********";

    @Override
    protected void afterUpdateSave(ParametreEntity entity, Parametre resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        cacheHelper.evictCacheItem(PARAMETRE_CACHE, entity.getId().toString());

//        Els mètodes de programar tasques es fan a través de cridades REST, per tant, no s'inclouen dins la transacció de base de dades actual.
//        Per a evitar que es programin les tasques usant les dades antigues dels paràmetres, s'usa un eventPublisher que
//        espera a que es faci commit de la transacció i després executam les cridades corresponents.
        eventPublisher.publishEvent(new ParametreInfoUpdatedEvent(entity));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSalutInfoUpdated(ParametreInfoUpdatedEvent event) {
        switch (event.getEntity().getCodi()) {
            case BaseConfig.PROP_MONITOR_BUIDAT_PERIODE_MINUTS:
            case BaseConfig.PROP_MONITOR_BUIDAT_RETENCIO_DIES:
                monitorServiceClient.programarBorrat(httpAuthorizationHeaderHelper.getAuthorizationHeader());
                break;
            case BaseConfig.PROP_STATS_COMPACTAR_ACTIU:
            case BaseConfig.PROP_STATS_COMPACTAR_CRON:
                estadisticaServiceClient.programarTot(httpAuthorizationHeaderHelper.getAuthorizationHeader());
                break;
        }
    }

    @Override
    protected void afterConversion(ParametreEntity entity, Parametre resource) {
        switch (resource.getTipus()){
            case BOOLEAN:
                resource.setValorBoolean(Objects.isNull(resource.getValor()) ? null : resource.getValor().equalsIgnoreCase("true"));
                break;
            case PASSWORD:
                if (!Objects.equals(httpAuthorizationHeaderHelper.getAuthUsername(), authenticationHelper.getCurrentUserName())) {
                    resource.setValor(PASSWORD_LABEL);
                }
                break;
        }

        // Els paràmetres no editables, s'agafen del sistema
        if (!entity.isEditable() && !ParamTipus.PASSWORD.equals(entity.getTipus())) {
            var valor = environment.getProperty(entity.getCodi());
            if (valor != null) {
                resource.setValor(valor);
            }
        }
    }

    @Override
    protected void completeResource(Parametre resource) {
        switch (resource.getTipus()) {
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

    /**
     * Esdeveniment que indica que s'ha actualitzat la informació d'un paràmetre.
     */
    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class ParametreInfoUpdatedEvent {
        private final ParametreEntity entity;
    }

}
