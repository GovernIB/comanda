package es.caib.comanda.api.helper;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.AvisServiceClient;
import es.caib.comanda.client.EntornServiceClient;
import es.caib.comanda.client.TascaServiceClient;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.Avis;
import es.caib.comanda.client.model.Tasca;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiClientHelper {

    private final AppServiceClient appServiceClient;
    private final EntornServiceClient entornServiceClient;
    private final TascaServiceClient tascaServiceClient;
    private final AvisServiceClient avisServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Cacheable(value = "appByCodiCache", key = "#appCodi")
    public Optional<App> getAppByCodi(String appCodi) {
        return appServiceClient.find(
                "",
                "codi:'" + appCodi + "'",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader()
        ).getContent().stream().map(EntityModel::getContent).findFirst();
    }

    @Cacheable(value = "entornByCodiCache", key = "#entornCodi")
    public Optional<Entorn> getEntornByCodi(String entornCodi) {
        return entornServiceClient.find(
                "",
                "codi:'" + entornCodi + "'",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader()
        ).getContent().stream().map(EntityModel::getContent).findFirst();
    }

    public Optional<Tasca> getTasca(String identificador, Long appId, Long entornId) {
        PagedModel<EntityModel<Tasca>> tasques = tascaServiceClient.find(
                "",
                "appId:'" + appId + "' and entornId:'" + entornId + "' and identificador:'" + identificador + "'",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return tasques.getContent().stream()
                .map(EntityModel::getContent)
                .findFirst();
    }

    public List<Tasca> getTasques(Set<String> identificadors, Long appId, Long entornId) {
        PagedModel<EntityModel<Tasca>> tasques = tascaServiceClient.find(
                "",
                "appId:'" + appId + "' and entornId:'" + entornId + "' and identificador in [" + identificadors.stream().collect(Collectors.joining(", ")) + "]",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return tasques.getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toUnmodifiableList());
    }

    public PagedModel<EntityModel<Tasca>> getTasques(String quickFilter, String filter, String[] namedQueries, String[] perspectives, String page, Integer size) {
        return tascaServiceClient.find(
                quickFilter,
                filter,
                namedQueries,
                perspectives,
                page,
                size,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
//        return tasques.getContent().stream()
//                .map(EntityModel::getContent)
//                .collect(Collectors.toUnmodifiableList());
    }

    public Optional<Avis> getAvis(String identificador, Long appId, Long entornId) {
        PagedModel<EntityModel<Avis>> avisos = avisServiceClient.find(
                "",
                "appId:'" + appId + "' and entornId:'" + entornId + "' and identificador:'" + identificador + "'",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return avisos.getContent().stream()
                .map(EntityModel::getContent)
                .findFirst();
    }

    public List<Avis> getAvisos(Set<String> identificadors, Long appId, Long entornId) {
        PagedModel<EntityModel<Avis>> avisos = avisServiceClient.find(
                "",
                "appId:'" + appId + "' and entornId:'" + entornId + "' and identificador in [" + identificadors.stream().collect(Collectors.joining(", ")) + "]",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return avisos.getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toUnmodifiableList());
    }

    public PagedModel<EntityModel<Avis>> getAvisos(String quickFilter, String filter, String[] namedQueries, String[] perspectives, String page, Integer size) {
        return avisServiceClient.find(
                quickFilter,
                filter,
                namedQueries,
                perspectives,
                page,
                size,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
    }

}
