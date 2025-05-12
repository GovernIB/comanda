package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.client.EstadisticaServiceClient;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.AppIntegracio;
import es.caib.comanda.configuracio.logic.intf.model.AppSubsistema;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.intf.service.EntornAppService;
import es.caib.comanda.configuracio.persist.entity.AppIntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.AppSubsistemaEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.IntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementació del servei de gestió d'aplicacions per entorn.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EntornAppServiceImpl extends BaseMutableResourceService<EntornApp, Long, EntornAppEntity> implements EntornAppService {

    @Autowired
    private IntegracioRepository integracioRepository;
    @Autowired
    private SubsistemaRepository subsistemaRepository;
    @Autowired
    private EntornAppRepository entornAppRepository;
    @Autowired
    private AppInfoHelper appInfoHelper;
    @Autowired
    private KeycloakHelper keycloakHelper;
    @Autowired
    private SalutServiceClient salutServiceClient;
    @Autowired
    private EstadisticaServiceClient estadisticaServiceClient;

    @Autowired
    private ConfiguracioSchedulerService schedulerService;

    @PostConstruct
    public void init() {
        register(new EntornAppServiceImpl.RefreshAction(entornAppRepository, appInfoHelper));
        register(new EntornAppServiceImpl.ReprogramarAction(entornAppRepository, schedulerService));
    }

    @Override
    protected void afterConversion(EntornAppEntity entity, EntornApp resource) {
        List<AppIntegracioEntity> integracions = integracioRepository.findByEntornApp(entity);
        if (!integracions.isEmpty()) {
            resource.setIntegracions(
                    integracions.stream().map(i -> new AppIntegracio(
                            i.getCodi(),
                            i.getNom(),
                            i.isActiva(),
                            null)).collect(Collectors.toList()));
        }
        List<AppSubsistemaEntity> subsistemes = subsistemaRepository.findByEntornApp(entity);
        if (!integracions.isEmpty()) {
            resource.setSubsistemes(
                    subsistemes.stream().map(s -> new AppSubsistema(
                            s.getCodi(),
                            s.getNom(),
                            s.isActiu(),
                            null)).collect(Collectors.toList()));
        }
    }

    @Override
    protected void afterCreateSave(EntornAppEntity entity, EntornApp resource) {
        super.afterCreateSave(entity, resource);
        schedulerService.programarTasca(entity);
        programarTasquesSalutEstadistica(entity);
    }

    @Override
    protected void afterUpdateSave(EntornAppEntity entity, EntornApp resource) {
        super.afterUpdateSave(entity, resource);
        schedulerService.programarTasca(entity);
        programarTasquesSalutEstadistica(entity);
    }

    private void programarTasquesSalutEstadistica(EntornAppEntity entity) {
        es.caib.comanda.client.model.EntornApp clientEntornApp = toClientEntornApp(entity);
        try {
            salutServiceClient.programar(clientEntornApp, keycloakHelper.getAuthorizationHeader());
        } catch (Exception e) {
            log.error("Error al programar l'actualització d'informació de salut per l'entornApp {}", entity.getId(), e);
        }
        try {
            estadisticaServiceClient.programar(clientEntornApp, keycloakHelper.getAuthorizationHeader());
        } catch (Exception e) {
            log.error("Error al programar l'actualització d'informació estadística per l'entornApp {}", entity.getId(), e);
        }
    }

    private es.caib.comanda.client.model.EntornApp toClientEntornApp(EntornAppEntity entity) {
        return es.caib.comanda.client.model.EntornApp.builder()
                .id(entity.getId())
                .entorn(EntornRef.builder().id(entity.getEntorn().getId()).nom(entity.getEntorn().getNom()).build())
                .app(AppRef.builder().id(entity.getApp().getId()).nom(entity.getApp().getNom()).build())
                .infoUrl(entity.getInfoUrl())
                .infoInterval(entity.getInfoInterval())
                .salutUrl(entity.getSalutUrl())
                .salutInterval(entity.getSalutInterval())
                .estadisticaUrl(entity.getEstadisticaUrl())
                .estadisticaUrl(entity.getEstadisticaUrl())
                .estadisticaCron(entity.getEstadisticaCron())
                .activa(entity.isActiva())
                .build();
    }

    // ACCIONS

    public static class RefreshAction implements ActionExecutor<EntornAppParamAction, Object> {
        private final EntornAppRepository entornAppRepository;
        private final AppInfoHelper appInfoHelper;

        public RefreshAction(EntornAppRepository entornAppRepository, AppInfoHelper appInfoHelper) {
            this.entornAppRepository = entornAppRepository;
            this.appInfoHelper = appInfoHelper;
        }
        @Override
        public String[] getSupportedActionCodes() {
            return new String[] { "refresh" };
        }

        @Transactional
        @Override
        public Object exec(String code, EntornAppParamAction params) throws ActionExecutionException {

            try {
                log.info("Executant procés per l'entornApp {}",params.getEntornAppId());

                // Refrescar informació de entorn-app
                appInfoHelper.refreshAppInfo(params.getEntornAppId());

            } catch (Exception e) {
                log.error("Error en l'execució del procés de refresc de la informació per l'entornApp {}", params.getEntornAppId(), e);
            }
            return null;
        }
    }

    public static class ReprogramarAction implements ActionExecutor<EntornAppParamAction, Object> {
        private final EntornAppRepository entornAppRepository;
        private final ConfiguracioSchedulerService schedulerService;

        public ReprogramarAction(EntornAppRepository entornAppRepository, ConfiguracioSchedulerService schedulerService) {
            this.entornAppRepository = entornAppRepository;
            this.schedulerService = schedulerService;
        }
        @Override
        public String[] getSupportedActionCodes() {
            return new String[] { "reprogramar" };
        }

        @Override
        public Object exec(String code, EntornAppParamAction params) throws ActionExecutionException {

            EntornAppEntity entornApp = entornAppRepository.findById(params.getEntornAppId())
                    .orElseThrow(() -> new ActionExecutionException(EntornApp.class, params.getEntornAppId(), code, "EntornApp actiu no trobat"));

            schedulerService.programarTasca(entornApp);
            return null;
        }
    }

    @Getter
    @Setter
    public static class EntornAppParamAction implements Serializable {
        private Long entornAppId;
    }

}
