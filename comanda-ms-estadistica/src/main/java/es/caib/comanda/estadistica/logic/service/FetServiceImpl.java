package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.estadistica.logic.intf.model.Fet;
import es.caib.comanda.estadistica.logic.intf.service.FetService;
import es.caib.comanda.estadistica.persist.entity.FetEntity;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FetServiceImpl extends BaseReadonlyResourceService<Fet, Long, FetEntity> implements FetService {

    @Value("${es.caib.comanda.keycloak.username:#{null}}")
    private String keycloakUsername;
    @Value("${es.caib.comanda.keycloak.password:#{null}}")
    private String keycloakPassword;

    @Autowired
    private AppServiceClient appServiceClient;
    @Autowired
    private KeycloakHelper keycloakHelper;
    @Autowired
    private EstadisticaHelper estadisticaHelper;
    @Autowired
    private EntornAppServiceClient entornAppServiceClient;

    @Override
    public void getEstadisticaInfo() {
        log.debug("Iniciant consulta periòdica de estadístiques");
        List<App> apps = appFindByActivaTrue();
        apps.forEach(a -> {
            if (a.getEntornApps().isEmpty())
                return;

            a.getEntornApps().parallelStream().forEach(ea -> {
                if (ea.isActiva() && !Strings.isBlank(ea.getEstadisticaInfoUrl()) && !Strings.isBlank(ea.getEstadisticaUrl())) {
                    try {
                        estadisticaHelper.getEstadisticaInfo(ea, ea.getEstadisticaInfoUrl(), ea.getEstadisticaUrl());
                    } catch (Exception ex) {
                        log.error("No s'han pogut consultar les estadístiques de l'aplicació {}, entorn {}", a.getCodi(), ea.getEntorn().getNom(), ex);
                    }
                }
            });
        });
    }

    private EntornApp entornAppFindById(Long entornAppId) {
        EntityModel<EntornApp> entornApp = entornAppServiceClient.getOne(
                entornAppId,
                null,
                getAuthorizationHeader());
        if (entornApp != null) {
            return entornApp.getContent();
        }
        return null;
    }

    private List<App> appFindByActivaTrue() {
        PagedModel<EntityModel<App>> apps = appServiceClient.find(
                null,
                "activa:true",
                null,
                null,
                "UNPAGED",
                null,
                getAuthorizationHeader());
        return apps.getContent().stream().
                map(EntityModel::getContent).
                collect(Collectors.toList());
    }

    private String getAuthorizationHeader() {
        String accessToken = keycloakHelper.getAccessTokenWithUsernamePassword(
                keycloakUsername,
                keycloakPassword);
        return accessToken != null ? "Bearer " + accessToken : null;
    }

    @Override
    public void migrarDades(Long entornAppId) { //, LocalDate data) {
        try {
            log.info("Migració de dades manual de ahir per entornAppId: {}", entornAppId);
            EntornApp entornApp = entornAppFindById(entornAppId);
            estadisticaHelper.getEstadisticaInfo(entornApp, entornApp.getEstadisticaInfoUrl(), entornApp.getEstadisticaUrl());
        } catch (Exception e) {
            log.error("Error en la migració de dades", e);
            throw e;
        }
    }

    @Override
    public List<Fet> getEstadistiquesPeriode(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi) {
//            NivellAgrupacio nivellAgrupacio) {

        return estadisticaHelper.getEstadistiquesPeriode(
                entornAppId,
                dataInici,
                dataFi);
    }

    @Override
    public List<Fet> getEstadistiquesPeriodeAmbDimensions(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre) {
//            NivellAgrupacio nivellAgrupacio) {

        return estadisticaHelper.getEstadistiquesPeriodeAmbDimensions(
                entornAppId,
                dataInici,
                dataFi,
                dimensionsFiltre);
    }

}
